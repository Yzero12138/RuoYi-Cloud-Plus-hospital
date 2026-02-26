package org.dromara.datacenter.nephrology.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.common.mybatis.core.page.PageQuery;
import org.dromara.common.mybatis.core.page.TableDataInfo;
import org.dromara.common.web.core.BaseController;
import org.dromara.datacenter.nephrology.domain.bo.LabReportBo;
import org.dromara.datacenter.nephrology.domain.vo.LabReportVo;
import org.dromara.datacenter.nephrology.service.ILabReportService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lab report controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/nephrology/lab-report")
public class LabReportController extends BaseController {

    private final ILabReportService labReportService;

    /**
     * List lab reports.
     */
    @SaCheckPermission("data-center:nephrology:lab-report:list")
    @GetMapping("/list")
    public TableDataInfo<LabReportVo> list(LabReportBo bo, PageQuery pageQuery) {
        return labReportService.selectPageLabReportList(bo, pageQuery);
    }

    /**
     * Get report detail.
     */
    @SaCheckPermission("data-center:nephrology:lab-report:query")
    @GetMapping("/{id}")
    public R<LabReportVo> getInfo(@PathVariable Long id) {
        return R.ok(labReportService.selectLabReportById(id));
    }
}
