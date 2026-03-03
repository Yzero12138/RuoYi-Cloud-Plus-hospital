package org.dromara.datacenter.hospitalqc.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcLedgerItemBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcLedgerItemVo;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcLedgerMaintainService;
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
 * Ledger tree maintenance controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hospital-qc/ledger-maintain")
public class HospitalQcLedgerMaintainController {

    private final IHospitalQcLedgerMaintainService ledgerMaintainService;

    @SaCheckPermission("data-center:hospital-qc:ledger-maintain:list")
    @GetMapping("/list")
    public R<List<HospitalQcLedgerItemVo>> list(HospitalQcLedgerItemBo bo) {
        return R.ok(ledgerMaintainService.selectTreeList(bo));
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-maintain:query")
    @GetMapping("/{id}")
    public R<HospitalQcLedgerItemVo> getInfo(@PathVariable Long id) {
        return R.ok(ledgerMaintainService.selectById(id));
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-maintain:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody HospitalQcLedgerItemBo bo) {
        return ledgerMaintainService.insertByBo(bo) ? R.ok() : R.fail();
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-maintain:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody HospitalQcLedgerItemBo bo) {
        return ledgerMaintainService.updateByBo(bo) ? R.ok() : R.fail();
    }

    @SaCheckPermission("data-center:hospital-qc:ledger-maintain:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return ledgerMaintainService.deleteWithChildrenByIds(Arrays.asList(ids)) ? R.ok() : R.fail();
    }
}

