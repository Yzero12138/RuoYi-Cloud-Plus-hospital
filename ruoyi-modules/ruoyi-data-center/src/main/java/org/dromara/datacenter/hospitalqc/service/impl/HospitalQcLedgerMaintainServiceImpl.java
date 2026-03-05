package org.dromara.datacenter.hospitalqc.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.datacenter.hospitalqc.constants.HospitalQcConstants;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerItem;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcLedgerQuery;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerItemBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcLedgerItemVo;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcLedgerItemMapper;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcLedgerQueryMapper;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcLedgerMaintainService;
import org.dromara.datacenter.hospitalqc.util.HospitalQcDeptPermissionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Ledger tree maintenance service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
@Slf4j
public class HospitalQcLedgerMaintainServiceImpl implements IHospitalQcLedgerMaintainService {

    private final HospitalQcLedgerItemMapper baseMapper;
    private final HospitalQcLedgerQueryMapper queryMapper;

    @Override
    public List<HospitalQcLedgerItemVo> selectTreeList(HospitalQcLedgerItemBo bo) {
        // Apply department permission filter
        Set<Long> allowedDeptIds = HospitalQcDeptPermissionUtils.getAllowedDeptIds();
        if (!allowedDeptIds.isEmpty()) {
            // Non-admin user: filter by allowed departments
            if (bo.getDeptId() != null) {
                // Validate requested department
                if (!allowedDeptIds.contains(bo.getDeptId())) {
                    throw new ServiceException("没有权限访问该科室的台账数据");
                }
            } else {
                // Use first allowed department as default for non-admin
                bo.setDeptId(allowedDeptIds.iterator().next());
            }
        }
        // Admin with empty allowedDeptIds can access all, no filter needed

        List<HospitalQcLedgerItemVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        if (list == null || list.isEmpty()) {
            log.warn("Hospital QC ledger tree is empty for deptId={}. Please check table hospital_qc_ledger_item data.", bo.getDeptId());
        }
        return buildTree(list);
    }

    @Override
    public HospitalQcLedgerItemVo selectById(Long id) {
        HospitalQcLedgerItemVo vo = baseMapper.selectVoOne(Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .eq(HospitalQcLedgerItem::getId, id)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED));
        if (vo != null) {
            HospitalQcDeptPermissionUtils.assertDeptPermission(vo.getDeptId());
        }
        return vo;
    }

    @Override
    public Boolean insertByBo(HospitalQcLedgerItemBo bo) {
        normalizeAndValidate(bo, true);
        checkCodeUnique(bo.getLedgerCode(), null);

        // Apply department permission for non-admin
        Set<Long> allowedDeptIds = HospitalQcDeptPermissionUtils.getAllowedDeptIds();
        if (!allowedDeptIds.isEmpty()) {
            if (bo.getDeptId() == null) {
                bo.setDeptId(allowedDeptIds.iterator().next());
            } else if (!allowedDeptIds.contains(bo.getDeptId())) {
                throw new ServiceException("没有权限在该科室下创建台账");
            }
        }

        HospitalQcLedgerItem add = MapstructUtils.convert(bo, HospitalQcLedgerItem.class);
        add.setParentId(bo.getParentId() == null ? 0L : bo.getParentId());
        add.setSortOrder(bo.getSortOrder() == null ? 1 : bo.getSortOrder());
        add.setStatus(bo.getStatus() == null ? HospitalQcConstants.STATUS_NORMAL : bo.getStatus());
        add.setIsDeleted(HospitalQcConstants.LOGIC_NOT_DELETED);
        boolean success = baseMapper.insert(add) > 0;
        if (success) {
            bo.setId(add.getId());
        }
        return success;
    }

    @Override
    public Boolean updateByBo(HospitalQcLedgerItemBo bo) {
        HospitalQcLedgerItem existing = getEntity(bo.getId());
        // Check permission on existing record
        HospitalQcDeptPermissionUtils.assertDeptPermission(existing.getDeptId());

        normalizeAndValidate(bo, false);
        checkCodeUnique(bo.getLedgerCode(), bo.getId());

        // Apply department permission for non-admin
        Set<Long> allowedDeptIds = HospitalQcDeptPermissionUtils.getAllowedDeptIds();
        if (!allowedDeptIds.isEmpty()) {
            if (bo.getDeptId() == null) {
                bo.setDeptId(allowedDeptIds.iterator().next());
            } else if (!allowedDeptIds.contains(bo.getDeptId())) {
                throw new ServiceException("没有权限修改到该科室");
            }
        }

        HospitalQcLedgerItem update = MapstructUtils.convert(bo, HospitalQcLedgerItem.class);
        if (update.getParentId() == null) {
            update.setParentId(0L);
        }
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithChildrenByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        // Check permission on all items to be deleted
        for (Long id : ids) {
            HospitalQcLedgerItem item = getEntity(id);
            HospitalQcDeptPermissionUtils.assertDeptPermission(item.getDeptId());
        }

        List<HospitalQcLedgerItem> allList = baseMapper.selectList(Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .select(HospitalQcLedgerItem::getId, HospitalQcLedgerItem::getParentId)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED));

        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (HospitalQcLedgerItem item : allList) {
            childrenMap.computeIfAbsent(item.getParentId(), key -> new ArrayList<>()).add(item.getId());
        }

        Set<Long> allDeleteIds = new HashSet<>(ids);
        collectChildren(ids, childrenMap, allDeleteIds);

        return baseMapper.update(new HospitalQcLedgerItem(), new LambdaUpdateWrapper<HospitalQcLedgerItem>()
            .set(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_DELETED)
            .in(HospitalQcLedgerItem::getId, allDeleteIds)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)) > 0;
    }

    private void normalizeAndValidate(HospitalQcLedgerItemBo bo, boolean addMode) {
        if (!List.of(
            HospitalQcConstants.NODE_TYPE_INDICATOR,
            HospitalQcConstants.NODE_TYPE_NUMERATOR,
            HospitalQcConstants.NODE_TYPE_DENOMINATOR
        ).contains(bo.getNodeType())) {
            throw new ServiceException("节点类型仅支持 I/N/D");
        }

        if (HospitalQcConstants.NODE_TYPE_INDICATOR.equals(bo.getNodeType())) {
            bo.setQueryCode(HospitalQcConstants.QUERY_CODE_NONE);
        } else {
            if (StringUtils.isBlank(bo.getQueryCode()) || HospitalQcConstants.QUERY_CODE_NONE.equals(bo.getQueryCode())) {
                throw new ServiceException("分子/分母节点必须选择查询编码");
            }
            checkQueryCodeExists(bo.getQueryCode());
        }

        if (bo.getParentId() != null && bo.getParentId() > 0) {
            HospitalQcLedgerItem parent = getEntity(bo.getParentId());
            if (HospitalQcConstants.NODE_TYPE_INDICATOR.equals(bo.getNodeType())
                && !HospitalQcConstants.NODE_TYPE_INDICATOR.equals(parent.getNodeType())) {
                throw new ServiceException("指标节点只能挂载在根节点或指标节点下");
            }
            if (!HospitalQcConstants.NODE_TYPE_INDICATOR.equals(bo.getNodeType())
                && !HospitalQcConstants.NODE_TYPE_INDICATOR.equals(parent.getNodeType())) {
                throw new ServiceException("分子/分母节点必须挂在指标节点下");
            }
            if (!addMode && Objects.equals(bo.getId(), bo.getParentId())) {
                throw new ServiceException("父节点不能为自身");
            }
        }
    }

    private void checkQueryCodeExists(String queryCode) {
        HospitalQcLedgerQuery query = queryMapper.selectOne(Wrappers.<HospitalQcLedgerQuery>lambdaQuery()
            .select(HospitalQcLedgerQuery::getId)
            .eq(HospitalQcLedgerQuery::getQueryCode, queryCode)
            .eq(HospitalQcLedgerQuery::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcLedgerQuery::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .last("limit 1"));
        if (query == null) {
            throw new ServiceException("查询编码不存在或已停用: " + queryCode);
        }
    }

    private HospitalQcLedgerItem getEntity(Long id) {
        HospitalQcLedgerItem entity = baseMapper.selectOne(Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .eq(HospitalQcLedgerItem::getId, id)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .last("limit 1"));
        if (entity == null) {
            throw new ServiceException("台账节点不存在");
        }
        return entity;
    }

    private void checkCodeUnique(String code, Long excludeId) {
        HospitalQcLedgerItem exists = baseMapper.selectOne(Wrappers.<HospitalQcLedgerItem>lambdaQuery()
            .select(HospitalQcLedgerItem::getId)
            .eq(HospitalQcLedgerItem::getLedgerCode, code)
            .eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .ne(excludeId != null, HospitalQcLedgerItem::getId, excludeId)
            .last("limit 1"));
        if (exists != null) {
            throw new ServiceException("台账编码已存在");
        }
    }

    private void collectChildren(Collection<Long> parentIds, Map<Long, List<Long>> childrenMap, Set<Long> collector) {
        List<Long> childIds = new ArrayList<>();
        for (Long parentId : parentIds) {
            List<Long> ids = childrenMap.get(parentId);
            if (ids != null && !ids.isEmpty()) {
                childIds.addAll(ids);
            }
        }
        if (childIds.isEmpty()) {
            return;
        }
        collector.addAll(childIds);
        collectChildren(childIds, childrenMap, collector);
    }

    private LambdaQueryWrapper<HospitalQcLedgerItem> buildQueryWrapper(HospitalQcLedgerItemBo bo) {
        LambdaQueryWrapper<HospitalQcLedgerItem> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, HospitalQcLedgerItem::getId, bo.getId());
        lqw.eq(bo.getDeptId() != null, HospitalQcLedgerItem::getDeptId, bo.getDeptId());
        lqw.eq(StringUtils.isNotBlank(bo.getLedgerCode()), HospitalQcLedgerItem::getLedgerCode, bo.getLedgerCode());
        lqw.like(StringUtils.isNotBlank(bo.getLedgerName()), HospitalQcLedgerItem::getLedgerName, bo.getLedgerName());
        lqw.eq(StringUtils.isNotBlank(bo.getNodeType()), HospitalQcLedgerItem::getNodeType, bo.getNodeType());
        lqw.eq(bo.getStatus() != null, HospitalQcLedgerItem::getStatus, bo.getStatus());
        lqw.eq(HospitalQcLedgerItem::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED);
        lqw.orderByAsc(HospitalQcLedgerItem::getSortOrder);
        lqw.orderByAsc(HospitalQcLedgerItem::getId);
        return lqw;
    }

    private List<HospitalQcLedgerItemVo> buildTree(List<HospitalQcLedgerItemVo> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        Map<Long, HospitalQcLedgerItemVo> nodeMap = new HashMap<>();
        for (HospitalQcLedgerItemVo item : list) {
            nodeMap.put(item.getId(), item);
            item.setChildren(new ArrayList<>());
        }

        List<HospitalQcLedgerItemVo> roots = new ArrayList<>();
        for (HospitalQcLedgerItemVo item : list) {
            Long parentId = Objects.requireNonNullElse(item.getParentId(), 0L);
            HospitalQcLedgerItemVo parent = nodeMap.get(parentId);
            if (parent == null || parentId == 0L) {
                roots.add(item);
            } else {
                parent.getChildren().add(item);
            }
        }

        Comparator<HospitalQcLedgerItemVo> comparator = Comparator
            .comparing((HospitalQcLedgerItemVo it) -> Objects.requireNonNullElse(it.getSortOrder(), 0))
            .thenComparing(HospitalQcLedgerItemVo::getId);
        sortTree(roots, comparator);
        return roots;
    }

    private void sortTree(List<HospitalQcLedgerItemVo> list, Comparator<HospitalQcLedgerItemVo> comparator) {
        list.sort(comparator);
        for (HospitalQcLedgerItemVo item : list) {
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                sortTree(item.getChildren(), comparator);
            }
        }
    }
}
