package org.dromara.datacenter.hospitalqc.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.exception.ServiceException;
import org.dromara.common.core.utils.StringUtils;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.hospitalqc.config.HospitalQcProperties;
import org.dromara.datacenter.hospitalqc.constants.HospitalQcConstants;
import org.dromara.datacenter.hospitalqc.domain.HospitalQcDataSource;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceStatusBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceTestBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcConnectionTestVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDataSourceVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcOptionVo;
import org.dromara.datacenter.hospitalqc.enums.HospitalQcDataSourceType;
import org.dromara.datacenter.hospitalqc.mapper.HospitalQcDataSourceMapper;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcDataSourceService;
import org.dromara.datacenter.hospitalqc.util.HospitalQcJdbcExecutor;
import org.dromara.datacenter.hospitalqc.util.HospitalQcPasswordCrypto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Datasource config service implementation.
 */
@RequiredArgsConstructor
@Service
@DS("data-center")
public class HospitalQcDataSourceServiceImpl implements IHospitalQcDataSourceService {

    private final HospitalQcDataSourceMapper baseMapper;
    private final HospitalQcPasswordCrypto passwordCrypto;
    private final HospitalQcJdbcExecutor jdbcExecutor;
    private final HospitalQcProperties properties;

    @Override
    public TableDataInfo<HospitalQcDataSourceVo> selectPageList(HospitalQcDataSourceBo bo, PageQuery pageQuery) {
        LambdaQueryWrapper<HospitalQcDataSource> lqw = buildQueryWrapper(bo);
        Page<HospitalQcDataSource> page = baseMapper.selectPage(pageQuery.build(), lqw);
        List<HospitalQcDataSourceVo> rows = page.getRecords().stream().map(this::toVo).toList();
        return new TableDataInfo<>(rows, page.getTotal());
    }

    @Override
    public HospitalQcDataSourceVo selectById(Long id) {
        HospitalQcDataSource entity = baseMapper.selectOne(Wrappers.<HospitalQcDataSource>lambdaQuery()
            .eq(HospitalQcDataSource::getId, id)
            .eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .last("limit 1"));
        return entity == null ? null : toVo(entity);
    }

    @Override
    public List<HospitalQcOptionVo> selectEnabledOptions() {
        List<HospitalQcDataSource> list = baseMapper.selectList(Wrappers.<HospitalQcDataSource>lambdaQuery()
            .select(HospitalQcDataSource::getId, HospitalQcDataSource::getSourceName, HospitalQcDataSource::getSourceType)
            .eq(HospitalQcDataSource::getStatus, HospitalQcConstants.STATUS_NORMAL)
            .eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .orderByAsc(HospitalQcDataSource::getSourceName)
            .orderByAsc(HospitalQcDataSource::getId));
        List<HospitalQcOptionVo> options = new ArrayList<>();
        for (HospitalQcDataSource dataSource : list) {
            options.add(new HospitalQcOptionVo(
                String.valueOf(dataSource.getId()),
                dataSource.getSourceName() + " (" + dataSource.getSourceType() + ")"
            ));
        }
        return options;
    }

    @Override
    public Boolean insertByBo(HospitalQcDataSourceBo bo) {
        checkNameUnique(bo.getSourceName(), null);
        validateAndResolveType(bo.getSourceType(), bo.getHost(), bo.getPort(), bo.getDatabaseName(), bo.getInstanceName());

        HospitalQcDataSource add = new HospitalQcDataSource();
        fillBaseFields(add, bo);
        add.setPasswordCipher(passwordCrypto.encrypt(bo.getPassword()));
        add.setStatus(bo.getStatus() == null ? HospitalQcConstants.STATUS_NORMAL : bo.getStatus());
        add.setIsDeleted(HospitalQcConstants.LOGIC_NOT_DELETED);
        boolean success = baseMapper.insert(add) > 0;
        if (success) {
            bo.setId(add.getId());
        }
        return success;
    }

    @Override
    public Boolean updateByBo(HospitalQcDataSourceBo bo) {
        HospitalQcDataSource old = getEntity(bo.getId());
        checkNameUnique(bo.getSourceName(), bo.getId());
        validateAndResolveType(bo.getSourceType(), bo.getHost(), bo.getPort(), bo.getDatabaseName(), bo.getInstanceName());

        HospitalQcDataSource update = new HospitalQcDataSource();
        update.setId(bo.getId());
        fillBaseFields(update, bo);
        if (StringUtils.isNotBlank(bo.getPassword())) {
            update.setPasswordCipher(passwordCrypto.encrypt(bo.getPassword()));
        } else {
            update.setPasswordCipher(old.getPasswordCipher());
        }
        if (bo.getStatus() == null) {
            update.setStatus(old.getStatus());
        }
        return baseMapper.updateById(update) > 0;
    }

    @Override
    public Boolean updateStatus(HospitalQcDataSourceStatusBo bo) {
        getEntity(bo.getId());
        return baseMapper.update(new HospitalQcDataSource(), new LambdaUpdateWrapper<HospitalQcDataSource>()
            .set(HospitalQcDataSource::getStatus, bo.getStatus())
            .eq(HospitalQcDataSource::getId, bo.getId())
            .eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)) > 0;
    }

    @Override
    public Boolean deleteByIds(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return baseMapper.update(new HospitalQcDataSource(), new LambdaUpdateWrapper<HospitalQcDataSource>()
            .set(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_DELETED)
            .in(HospitalQcDataSource::getId, ids)
            .eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)) > 0;
    }

    @Override
    public HospitalQcConnectionTestVo testConnection(HospitalQcDataSourceTestBo bo) {
        HospitalQcDataSource dataSource = mergeTestDataSource(bo);
        long begin = System.currentTimeMillis();
        HospitalQcConnectionTestVo result = new HospitalQcConnectionTestVo();
        try {
            String password = resolveTestPassword(bo, dataSource);
            jdbcExecutor.executeInSession(dataSource, password, resolveSqlTimeout(), session -> null);
            result.setSuccess(true);
            result.setElapsedMs(System.currentTimeMillis() - begin);
            result.setMessage("连接成功");
            return result;
        } catch (Exception ex) {
            result.setSuccess(false);
            result.setElapsedMs(System.currentTimeMillis() - begin);
            result.setMessage(resolveConnectionErrorMessage(ex));
            return result;
        }
    }

    private HospitalQcDataSource mergeTestDataSource(HospitalQcDataSourceTestBo bo) {
        if (bo.getId() == null) {
            if (StringUtils.isAnyBlank(bo.getSourceType(), bo.getHost(), bo.getUsername())) {
                throw new ServiceException("测试连接需要填写类型、主机、用户名");
            }
            validateAndResolveType(bo.getSourceType(), bo.getHost(), bo.getPort(), bo.getDatabaseName(), bo.getInstanceName());
            HospitalQcDataSource temp = new HospitalQcDataSource();
            temp.setSourceType(bo.getSourceType());
            temp.setHost(bo.getHost());
            temp.setPort(bo.getPort());
            temp.setDatabaseName(bo.getDatabaseName());
            temp.setInstanceName(bo.getInstanceName());
            temp.setUsername(bo.getUsername());
            return temp;
        }
        HospitalQcDataSource entity = getEntity(bo.getId());
        if (StringUtils.isNotBlank(bo.getSourceType())) {
            entity.setSourceType(bo.getSourceType());
        }
        if (StringUtils.isNotBlank(bo.getHost())) {
            entity.setHost(bo.getHost());
        }
        if (bo.getPort() != null) {
            entity.setPort(bo.getPort());
        }
        if (StringUtils.isNotBlank(bo.getDatabaseName())) {
            entity.setDatabaseName(bo.getDatabaseName());
        }
        if (StringUtils.isNotBlank(bo.getInstanceName())) {
            entity.setInstanceName(bo.getInstanceName());
        }
        if (StringUtils.isNotBlank(bo.getUsername())) {
            entity.setUsername(bo.getUsername());
        }
        validateAndResolveType(entity.getSourceType(), entity.getHost(), entity.getPort(), entity.getDatabaseName(), entity.getInstanceName());
        return entity;
    }

    private String resolveTestPassword(HospitalQcDataSourceTestBo bo, HospitalQcDataSource dataSource) {
        if (StringUtils.isNotBlank(bo.getPassword())) {
            return bo.getPassword();
        }
        if (StringUtils.isNotBlank(dataSource.getPasswordCipher())) {
            return passwordCrypto.decrypt(dataSource.getPasswordCipher());
        }
        throw new ServiceException("测试连接需要提供密码");
    }

    private void validateAndResolveType(String sourceType,
                                        String host,
                                        Integer port,
                                        String databaseName,
                                        String instanceName) {
        HospitalQcDataSourceType type = HospitalQcDataSourceType.fromType(sourceType);
        type.buildJdbcUrl(host, port, databaseName, instanceName, properties.getSqlserverTlsMode());
    }

    private void checkNameUnique(String sourceName, Long excludeId) {
        HospitalQcDataSource exists = baseMapper.selectOne(Wrappers.<HospitalQcDataSource>lambdaQuery()
            .select(HospitalQcDataSource::getId)
            .eq(HospitalQcDataSource::getSourceName, sourceName)
            .eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .ne(excludeId != null, HospitalQcDataSource::getId, excludeId)
            .last("limit 1"));
        if (exists != null) {
            throw new ServiceException("数据源名称已存在");
        }
    }

    private HospitalQcDataSource getEntity(Long id) {
        HospitalQcDataSource entity = baseMapper.selectOne(Wrappers.<HospitalQcDataSource>lambdaQuery()
            .eq(HospitalQcDataSource::getId, id)
            .eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED)
            .last("limit 1"));
        if (entity == null) {
            throw new ServiceException("数据源不存在");
        }
        return entity;
    }

    private LambdaQueryWrapper<HospitalQcDataSource> buildQueryWrapper(HospitalQcDataSourceBo bo) {
        LambdaQueryWrapper<HospitalQcDataSource> lqw = Wrappers.lambdaQuery();
        lqw.eq(bo.getId() != null, HospitalQcDataSource::getId, bo.getId());
        lqw.like(StringUtils.isNotBlank(bo.getSourceName()), HospitalQcDataSource::getSourceName, bo.getSourceName());
        lqw.eq(StringUtils.isNotBlank(bo.getSourceType()), HospitalQcDataSource::getSourceType, bo.getSourceType());
        lqw.like(StringUtils.isNotBlank(bo.getHost()), HospitalQcDataSource::getHost, bo.getHost());
        lqw.eq(bo.getStatus() != null, HospitalQcDataSource::getStatus, bo.getStatus());
        lqw.eq(HospitalQcDataSource::getIsDeleted, HospitalQcConstants.LOGIC_NOT_DELETED);
        lqw.orderByAsc(HospitalQcDataSource::getSourceName);
        lqw.orderByDesc(HospitalQcDataSource::getId);
        return lqw;
    }

    private void fillBaseFields(HospitalQcDataSource entity, HospitalQcDataSourceBo bo) {
        entity.setSourceName(bo.getSourceName());
        entity.setSourceType(bo.getSourceType().toUpperCase());
        entity.setHost(bo.getHost());
        entity.setPort(bo.getPort());
        entity.setDatabaseName(bo.getDatabaseName());
        entity.setInstanceName(bo.getInstanceName());
        entity.setUsername(bo.getUsername());
        entity.setRemark(bo.getRemark());
        if (bo.getStatus() != null) {
            entity.setStatus(bo.getStatus());
        }
    }

    private HospitalQcDataSourceVo toVo(HospitalQcDataSource entity) {
        HospitalQcDataSourceVo vo = new HospitalQcDataSourceVo();
        vo.setId(entity.getId());
        vo.setSourceName(entity.getSourceName());
        vo.setSourceType(entity.getSourceType());
        vo.setHost(entity.getHost());
        vo.setPort(entity.getPort());
        vo.setDatabaseName(entity.getDatabaseName());
        vo.setInstanceName(entity.getInstanceName());
        vo.setUsername(entity.getUsername());
        vo.setPasswordMasked(passwordCrypto.mask(entity.getPasswordCipher()));
        vo.setPasswordConfigured(StringUtils.isNotBlank(entity.getPasswordCipher()));
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateBy(entity.getCreateBy());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateBy(entity.getUpdateBy());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private int resolveSqlTimeout() {
        Integer timeout = properties.getSqlTimeoutSeconds();
        return timeout == null || timeout <= 0 ? HospitalQcConstants.DEFAULT_SQL_TIMEOUT_SECONDS : timeout;
    }

    private String resolveConnectionErrorMessage(Throwable throwable) {
        String message = throwable == null ? null : throwable.getMessage();
        String tlsMode = normalizeSqlServerTlsMode();
        if (StringUtils.isBlank(message)) {
            return "连接失败，请检查数据源配置或网络连通性。当前 sqlserverTlsMode=" + tlsMode;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("tls10") || lower.contains("tls 1.0")
            || lower.contains("tlsv1") || lower.contains("protocol version")) {
            if ("LEGACY_TLS10".equals(tlsMode)) {
                return "当前为 SQLServer 兼容模式 LEGACY_TLS10，但 JVM/驱动仍拒绝 TLS1.0。"
                    + "建议改为 LEGACY_NO_ENCRYPT（临时）或调整 JVM 安全策略后重试。"
                    + "当前 sqlserverTlsMode=" + tlsMode + "。原始错误: " + message;
            }
            return "SQLServer 当前仅支持 TLS1.0，请在服务器端启用 TLS1.2（推荐）后重试。"
                + "若暂时无法升级，可配置 hospital-qc.sqlserver-tls-mode=LEGACY_NO_ENCRYPT（临时兼容，明文传输）。"
                + "当前 sqlserverTlsMode=" + tlsMode + "。原始错误: " + message;
        }
        if (lower.contains("no appropriate protocol") || lower.contains("cipher suites are inappropriate")) {
            return "JDK 已禁用 TLS1.0 或弱加密套件，导致 SQLServer TLS1.0 握手失败。"
                + "已尝试 LEGACY_TLS10 兼容策略，请重启 ruoyi-data-center 后再测试；"
                + "若仍失败，优先改用 LEGACY_NO_ENCRYPT（前提 SQLServer 未强制加密）。"
                + "当前 sqlserverTlsMode=" + tlsMode + "。原始错误: " + message;
        }
        return "连接失败，当前 sqlserverTlsMode=" + tlsMode + "。错误: " + message;
    }

    private String normalizeSqlServerTlsMode() {
        return StringUtils.isBlank(properties.getSqlserverTlsMode())
            ? "TLS12" : properties.getSqlserverTlsMode().trim().toUpperCase(Locale.ROOT);
    }
}

