package org.dromara.datacenter.nephrology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerItemBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerItemVo;
import org.dromara.datacenter.nephrology.service.INephrologyLedgerMaintainService;
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
 * Ledger maintenance controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/nephrology/ledger-maintain")
public class NephrologyLedgerMaintainController {

    private final INephrologyLedgerMaintainService ledgerMaintainService;

    /**
     * Tree list.
     */
    @SaCheckPermission("data-center:nephrology:ledger-maintain:list")
    @GetMapping("/list")
    public R<List<NephrologyLedgerItemVo>> list(NephrologyLedgerItemBo bo) {
        return R.ok(ledgerMaintainService.selectTreeList(bo));
    }

    /**
     * Detail.
     */
    @SaCheckPermission("data-center:nephrology:ledger-maintain:query")
    @GetMapping("/{id}")
    public R<NephrologyLedgerItemVo> getInfo(@PathVariable Long id) {
        return R.ok(ledgerMaintainService.selectById(id));
    }

    /**
     * Add.
     */
    @SaCheckPermission("data-center:nephrology:ledger-maintain:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody NephrologyLedgerItemBo bo) {
        return ledgerMaintainService.insertByBo(bo) ? R.ok() : R.fail();
    }

    /**
     * Edit.
     */
    @SaCheckPermission("data-center:nephrology:ledger-maintain:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody NephrologyLedgerItemBo bo) {
        return ledgerMaintainService.updateByBo(bo) ? R.ok() : R.fail();
    }

    /**
     * Remove with children.
     */
    @SaCheckPermission("data-center:nephrology:ledger-maintain:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return ledgerMaintainService.deleteWithChildrenByIds(Arrays.asList(ids)) ? R.ok() : R.fail();
    }
}
