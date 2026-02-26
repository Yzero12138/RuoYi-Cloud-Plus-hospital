package org.dromara.datacenter.nephrology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.datacenter.nephrology.domain.bo.HdPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.HdPatientVo;
import org.dromara.datacenter.nephrology.service.IHdPatientService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hemodialysis patient controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/nephrology/hd-patient")
public class HdPatientController extends BaseController {

    private final IHdPatientService hdPatientService;

    /**
     * List hemodialysis patients.
     */
    @SaCheckPermission("data-center:nephrology:hd-patient:list")
    @GetMapping("/list")
    public TableDataInfo<HdPatientVo> list(HdPatientBo bo, PageQuery pageQuery) {
        return hdPatientService.selectPageHdPatientList(bo, pageQuery);
    }

    /**
     * Get patient detail.
     */
    @SaCheckPermission("data-center:nephrology:hd-patient:query")
    @GetMapping("/{id}")
    public R<HdPatientVo> getInfo(@PathVariable Long id) {
        return R.ok(hdPatientService.selectHdPatientById(id));
    }
}
