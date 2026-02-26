package org.dromara.datacenter.nephrology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.datacenter.nephrology.domain.bo.HemodialysisComplicationPatientBo;
import org.dromara.datacenter.nephrology.domain.vo.HemodialysisComplicationPatientVo;
import org.dromara.datacenter.nephrology.service.IHemodialysisComplicationPatientService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hemodialysis complication patient controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/nephrology/hemodialysis-complication-patient")
public class HemodialysisComplicationPatientController extends BaseController {

    private final IHemodialysisComplicationPatientService complicationPatientService;

    /**
     * Numerator list for indicator 132.
     */
    @SaCheckPermission("data-center:nephrology:complication:numerator:list")
    @GetMapping("/list")
    public TableDataInfo<HemodialysisComplicationPatientVo> list(HemodialysisComplicationPatientBo bo, PageQuery pageQuery) {
        return complicationPatientService.selectPageList(bo, pageQuery);
    }

    /**
     * Detail.
     */
    @SaCheckPermission("data-center:nephrology:complication:numerator:query")
    @GetMapping("/{id}")
    public R<HemodialysisComplicationPatientVo> getInfo(@PathVariable Long id) {
        return R.ok(complicationPatientService.selectById(id));
    }
}
