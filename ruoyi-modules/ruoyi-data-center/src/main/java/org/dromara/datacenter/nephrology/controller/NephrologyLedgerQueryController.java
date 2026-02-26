package org.dromara.datacenter.nephrology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.core.validate.AddGroup;
import org.dromara.common.core.validate.EditGroup;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerQueryBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerQueryVo;
import org.dromara.datacenter.nephrology.service.INephrologyLedgerQueryService;
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
 * Ledger query configuration controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/nephrology/ledger-query")
public class NephrologyLedgerQueryController {

    private final INephrologyLedgerQueryService ledgerQueryService;

    /**
     * Page list.
     */
    @SaCheckPermission("data-center:nephrology:ledger-query:list")
    @GetMapping("/list")
    public TableDataInfo<NephrologyLedgerQueryVo> list(NephrologyLedgerQueryBo bo, PageQuery pageQuery) {
        return ledgerQueryService.selectPageList(bo, pageQuery);
    }

    /**
     * Options.
     */
    @SaCheckPermission("data-center:nephrology:ledger-query:list")
    @GetMapping("/options")
    public R<List<NephrologyLedgerQueryVo>> options() {
        return R.ok(ledgerQueryService.selectOptions());
    }

    /**
     * Detail.
     */
    @SaCheckPermission("data-center:nephrology:ledger-query:query")
    @GetMapping("/{id}")
    public R<NephrologyLedgerQueryVo> getInfo(@PathVariable Long id) {
        return R.ok(ledgerQueryService.selectById(id));
    }

    /**
     * Add.
     */
    @SaCheckPermission("data-center:nephrology:ledger-query:add")
    @PostMapping
    public R<Void> add(@Validated(AddGroup.class) @RequestBody NephrologyLedgerQueryBo bo) {
        return ledgerQueryService.insertByBo(bo) ? R.ok() : R.fail();
    }

    /**
     * Edit.
     */
    @SaCheckPermission("data-center:nephrology:ledger-query:edit")
    @PutMapping
    public R<Void> edit(@Validated(EditGroup.class) @RequestBody NephrologyLedgerQueryBo bo) {
        return ledgerQueryService.updateByBo(bo) ? R.ok() : R.fail();
    }

    /**
     * Remove.
     */
    @SaCheckPermission("data-center:nephrology:ledger-query:remove")
    @DeleteMapping("/{ids}")
    public R<Void> remove(@PathVariable Long[] ids) {
        return ledgerQueryService.deleteByIds(Arrays.asList(ids)) ? R.ok() : R.fail();
    }
}

