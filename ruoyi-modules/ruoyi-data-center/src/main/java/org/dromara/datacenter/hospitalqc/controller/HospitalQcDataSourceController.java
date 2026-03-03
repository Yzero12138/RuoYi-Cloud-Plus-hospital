package org.dromara.datacenter.hospitalqc.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceStatusBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDataSourceTestBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcConnectionTestVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDataSourceVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcOptionVo;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcDataSourceService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Datasource config controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hospital-qc/datasource")
public class HospitalQcDataSourceController {

    private final IHospitalQcDataSourceService dataSourceService;

    @SaCheckPermission("data-center:hospital-qc:datasource:list")
    @GetMapping("/list")
    public TableDataInfo<HospitalQcDataSourceVo> list(HospitalQcDataSourceBo bo, PageQuery pageQuery) {
        return dataSourceService.selectPageList(bo, pageQuery);
    }

    @SaCheckPermission("data-center:hospital-qc:datasource:list")
    @GetMapping("/options")
    public R<List<HospitalQcOptionVo>> options() {
        return R.ok(dataSourceService.selectEnabledOptions());
    }

    @SaCheckPermission("data-center:hospital-qc:datasource:query")
    @GetMapping("/{id}")
    public R<HospitalQcDataSourceVo> getInfo(@PathVariable Long id) {
        return R.ok(dataSourceService.selectById(id));
    }

    @SaCheckPermission("data-center:hospital-qc:datasource:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HospitalQcDataSourceBo bo) {
        return dataSourceService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @SaCheckPermission("data-center:hospital-qc:datasource:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HospitalQcDataSourceBo bo) {
        return dataSourceService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @SaCheckPermission("data-center:hospital-qc:datasource:edit")
    @PutMapping("/change-status")
    public R<Void> changeStatus(@Validated @RequestBody HospitalQcDataSourceStatusBo bo) {
        return dataSourceService.updateStatus(bo) ? R.ok() : R.fail();
    }

    @SaCheckPermission("data-center:hospital-qc:datasource:test")
    @PostMapping("/test")
    public R<HospitalQcConnectionTestVo> test(@RequestBody HospitalQcDataSourceTestBo bo) {
        return R.ok(dataSourceService.testConnection(bo));
    }

    @SaCheckPermission("data-center:hospital-qc:datasource:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return dataSourceService.deleteByIds(Arrays.asList(ids)) ? R.ok() : R.fail();
    }
}

