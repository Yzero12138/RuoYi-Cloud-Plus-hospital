package org.dromara.datacenter.nephrology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerCountBo;
import org.dromara.datacenter.nephrology.domain.bo.NephrologyLedgerDetailBo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerCountVo;
import org.dromara.datacenter.nephrology.domain.vo.NephrologyLedgerDetailVo;
import org.dromara.datacenter.nephrology.service.INephrologyLedgerService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Ledger query controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/nephrology/ledger")
public class NephrologyLedgerController {

    private final INephrologyLedgerService ledgerService;

    /**
     * Count tree query.
     */
    @SaCheckPermission("data-center:nephrology:ledger:list")
    @GetMapping("/list")
    public R<List<NephrologyLedgerCountVo>> list(NephrologyLedgerCountBo bo) {
        return R.ok(ledgerService.selectCountTree(bo));
    }

    /**
     * Detail page query.
     */
    @SaCheckPermission("data-center:nephrology:ledger:detail:list")
    @GetMapping("/detail/list")
    public TableDataInfo<NephrologyLedgerDetailVo> detailList(NephrologyLedgerDetailBo bo, PageQuery pageQuery) {
        return ledgerService.selectDetailPage(bo, pageQuery);
    }
}
