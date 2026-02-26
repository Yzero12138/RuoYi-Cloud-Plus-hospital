package org.dromara.datacenter.nephrology.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.utils.MapstructUtils;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.datacenter.nephrology.domain.NephrologyLedgerItem;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerItemBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerItemVo;
import org.dromara.datacenter.nephrology.mapper.NephrologyLedgerItemMapper;
import org.dromara.datacenter.nephrology.service.INephrologyLedgerMaintainService;
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
 * Ledger maintenance service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class NephrologyLedgerMaintainServiceImpl implements INephrologyLedgerMaintainService {

    private final NephrologyLedgerItemMapper baseMapper;

    @Override
    public List<NephrologyLedgerItemVo> selectTreeList(NephrologyLedgerItemBo bo) {
        List<NephrologyLedgerItemVo> list = baseMapper.selectVoList(buildQueryWrapper(bo));
        return buildTree(list);
    }

    @Override
    public NephrologyLedgerItemVo selectById(Long id) {
        return baseMapper.selectVoOne(Wrappers.<NephrologyLedgerItem>lambdaQuery()
            .eq(NephrologyLedgerItem::getId, id)
            .eq(NephrologyLedgerItem::getIsDeleted, 0));
    }

    @Override
    public Boolean insertByBo(NephrologyLedgerItemBo bo) {
        NephrologyLedgerItem add = MapstructUtils.convert(bo, NephrologyLedgerItem.class);
        if (add.getParentId() == null) {
            add.setParentId(0L);
        }
        if (add.getSortOrder() == null) {
            add.setSortOrder(1);
        }
        if (add.getStatus() == null) {
            add.setStatus(0);
        }
        add.setIsDeleted(0);
        boolean flag = baseMapper.insert(add) > 0;
        if (flag) {
            bo.setId(add.getId());
        }
        return flag;
    }

    @Override
    public Boolean updateByBo(NephrologyLedgerItemBo bo) {
        NephrologyLedgerItem update = MapstructUtils.convert(bo, NephrologyLedgerItem.class);
        return baseMapper.updateById(update) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteWithChildrenByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        List<NephrologyLedgerItem> allList = baseMapper.selectList(Wrappers.<NephrologyLedgerItem>lambdaQuery()
            .select(NephrologyLedgerItem::getId, NephrologyLedgerItem::getParentId)
            .eq(NephrologyLedgerItem::getIsDeleted, 0));

        Map<Long, List<Long>> childrenMap = new HashMap<>();
        for (NephrologyLedgerItem item : allList) {
            childrenMap.computeIfAbsent(item.getParentId(), key -> new ArrayList<>()).add(item.getId());
        }

        Set<Long> allDeleteIds = new HashSet<>(ids);
        collectChildren(ids, childrenMap, allDeleteIds);

        if (allDeleteIds.isEmpty()) {
            return true;
        }

        return baseMapper.update(new NephrologyLedgerItem(), new LambdaUpdateWrapper<NephrologyLedgerItem>()
            .set(NephrologyLedgerItem::getIsDeleted, 1)
            .in(NephrologyLedgerItem::getId, allDeleteIds)
            .eq(NephrologyLedgerItem::getIsDeleted, 0)) > 0;
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

    private LambdaQueryWrapper<NephrologyLedgerItem> buildQueryWrapper(NephrologyLedgerItemBo bo) {
        LambdaQueryWrapper<NephrologyLedgerItem> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, NephrologyLedgerItem::getId, bo.getId());
        lqw.eq(StringUtils.isNotBlank(bo.getLedgerCode()), NephrologyLedgerItem::getLedgerCode, bo.getLedgerCode());
        lqw.like(StringUtils.isNotBlank(bo.getLedgerName()), NephrologyLedgerItem::getLedgerName, bo.getLedgerName());
        lqw.eq(StringUtils.isNotBlank(bo.getNodeType()), NephrologyLedgerItem::getNodeType, bo.getNodeType());
        lqw.eq(bo.getStatus() != null, NephrologyLedgerItem::getStatus, bo.getStatus());
        lqw.eq(NephrologyLedgerItem::getIsDeleted, 0);
        lqw.orderByAsc(NephrologyLedgerItem::getSortOrder);
        lqw.orderByAsc(NephrologyLedgerItem::getId);
        return lqw;
    }

    private List<NephrologyLedgerItemVo> buildTree(List<NephrologyLedgerItemVo> list) {
        if (list == null || list.isEmpty()) {
            return List.of();
        }
        Map<Long, NephrologyLedgerItemVo> nodeMap = new HashMap<>();
        for (NephrologyLedgerItemVo item : list) {
            nodeMap.put(item.getId(), item);
            item.setChildren(new ArrayList<>());
        }

        List<NephrologyLedgerItemVo> roots = new ArrayList<>();
        for (NephrologyLedgerItemVo item : list) {
            Long parentId = Objects.requireNonNullElse(item.getParentId(), 0L);
            NephrologyLedgerItemVo parent = nodeMap.get(parentId);
            if (parent == null || parentId == 0L) {
                roots.add(item);
            } else {
                parent.getChildren().add(item);
            }
        }

        Comparator<NephrologyLedgerItemVo> comparator = Comparator
            .comparing((NephrologyLedgerItemVo it) -> Objects.requireNonNullElse(it.getSortOrder(), 0))
            .thenComparing(NephrologyLedgerItemVo::getId);
        sortTree(roots, comparator);
        return roots;
    }

    private void sortTree(List<NephrologyLedgerItemVo> list, Comparator<NephrologyLedgerItemVo> comparator) {
        list.sort(comparator);
        for (NephrologyLedgerItemVo item : list) {
            if (item.getChildren() != null && !item.getChildren().isEmpty()) {
                sortTree(item.getChildren(), comparator);
            }
        }
    }
}
