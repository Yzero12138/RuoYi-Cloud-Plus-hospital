package org.dromara.datacenter.hospitalqc.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerQueryBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerQueryTestBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcLedgerQueryVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcOptionVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcSqlTestResultVo;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcLedgerQueryService;
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
 * Ledger SQL query config controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hospital-qc/ledger-query")
public class HospitalQcLedgerQueryController {

    private final IHospitalQcLedgerQueryService ledgerQueryService;

    @SaCheckPermission("data-center:hospital-qc:ledger-query:list")
    @GetMapping("/list")
    public TableDataInfo<HospitalQcLedgerQueryVo> list(HospitalQcLedgerQueryBo bo, PageQuery pageQuery) {
        return ledgerQueryService.selectPageList(bo, pageQuery);
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-query:list")
    @GetMapping("/options")
    public R<List<HospitalQcOptionVo>> options() {
        return R.ok(ledgerQueryService.selectOptions());
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-query:query")
    @GetMapping("/{id}")
    public R<HospitalQcLedgerQueryVo> getInfo(@PathVariable Long id) {
        return R.ok(ledgerQueryService.selectById(id));
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-query:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HospitalQcLedgerQueryBo bo) {
        return ledgerQueryService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-query:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HospitalQcLedgerQueryBo bo) {
        return ledgerQueryService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-query:test")
    @PostMapping("/test-sql")
    public R<HospitalQcSqlTestResultVo> testSql(@RequestBody HospitalQcLedgerQueryTestBo bo) {
        return R.ok(ledgerQueryService.testSql(bo));
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-query:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return ledgerQueryService.deleteByIds(Arrays.asList(ids)) ? R.ok() : R.fail();
    }
}

