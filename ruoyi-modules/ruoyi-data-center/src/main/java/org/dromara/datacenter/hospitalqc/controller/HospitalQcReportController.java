package org.dromara.datacenter.hospitalqc.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcReportQueryBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcReportRowVo;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcDashboardService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Report controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hospital-qc/report")
public class HospitalQcReportController {

    private final IHospitalQcDashboardService dashboardService;

    @SaCheckPermission("data-center:hospital-qc:report:list")
    @PostMapping("/summary")
    public R<List<HospitalQcReportRowVo>> summary(@RequestBody HospitalQcReportQueryBo bo) {
        return R.ok(dashboardService.reportSummary(bo));
    }
}

