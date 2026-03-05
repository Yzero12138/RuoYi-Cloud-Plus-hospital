package org.dromara.datacenter.hospitalqc.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDetailQueryBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcReportQueryBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcReportRowVo;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcDashboardService;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcDetailService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Report controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hospital-qc/report")
public class HospitalQcReportController {

    private final IHospitalQcDashboardService dashboardService;
    private final IHospitalQcDetailService detailService;

    @SaCheckPermission("data-center:hospital-qc:report:list")
    @PostMapping("/summary")
    public R<List<HospitalQcReportRowVo>> summary(@RequestBody HospitalQcReportQueryBo bo) {
        return R.ok(dashboardService.reportSummary(bo));
    }

    @SaCheckPermission("data-center:hospital-qc:report:detail")
    @PostMapping("/detail/page")
    public R<Page<Map<String, Object>>> detailPage(@RequestBody HospitalQcDetailQueryBo bo) {
        return R.ok(detailService.queryDetailPage(bo));
    }

    @SaCheckPermission("data-center:hospital-qc:report:detail")
    @GetMapping("/detail/columns")
    public R<List<String>> detailColumns(@RequestParam String ledgerCode) {
        return R.ok(detailService.queryDetailColumns(ledgerCode));
    }
}
