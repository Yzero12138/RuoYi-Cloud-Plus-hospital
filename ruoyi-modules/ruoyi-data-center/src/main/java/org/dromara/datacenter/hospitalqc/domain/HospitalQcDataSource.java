package org.dromara.datacenter.hospitalqc.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dromara.common.mybatis.core.domain.BaseEntity;

/**
 * External datasource configuration entity.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("hospital_qc_datasource")
public class HospitalQcDataSource extends BaseEntity {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String sourceName;

    private String sourceType;

    private String host;

    private Integer port;

    private String databaseName;

    private String instanceName;

    private String username;

    private String passwordCipher;

    private Integer status;

    private Integer isDeleted;

    private String remark;
}

