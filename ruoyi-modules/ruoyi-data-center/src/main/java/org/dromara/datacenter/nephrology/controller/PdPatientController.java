package org.dromara.datacenter.nephrology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.datacenter.nephrology.domain.bo.PdPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.PdPatientVo;
import org.dromara.datacenter.nephrology.service.IPdPatientService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Peritoneal dialysis patient controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/nephrology/pd-patient")
public class PdPatientController extends BaseController {

    private final IPdPatientService pdPatientService;

    /**
     * List peritoneal dialysis patients.
     */
    @SaCheckPermission("data-center:nephrology:pd-patient:list")
    @GetMapping("/list")
    public TableDataInfo<PdPatientVo> list(PdPatientBo bo, PageQuery pageQuery) {
        return pdPatientService.selectPagePdPatientList(bo, pageQuery);
    }

    /**
     * Get patient detail.
     */
    @SaCheckPermission("data-center:nephrology:pd-patient:query")
    @GetMapping("/{id}")
    public R<PdPatientVo> getInfo(@PathVariable Long id) {
        return R.ok(pdPatientService.selectPdPatientById(id));
    }
}
