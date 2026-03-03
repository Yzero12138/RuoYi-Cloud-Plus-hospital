package org.dromara.datacenter.hospitalqc.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import lombok.RequiredArgsConstructor;
import org.dromara.common.core.domain.R;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDashboardQueryBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDashboardFilterVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDashboardOverviewVo;
import org.dromara.datacenter.hospitalqc.service.IHospitalQcDashboardService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard controller.
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/hospital-qc/dashboard")
public class HospitalQcDashboardController {

    private final IHospitalQcDashboardService dashboardService;

    @SaCheckPermission("data-center:hospital-qc:dashboard:list")
    @GetMapping("/options")
    public R<HospitalQcDashboardFilterVo> options() {
        return R.ok(dashboardService.selectFilterOptions());
    }

    @SaCheckPermission("data-center:hospital-qc:dashboard:list")
    @PostMapping("/overview")
    public R<HospitalQcDashboardOverviewVo> overview(@RequestBody HospitalQcDashboardQueryBo bo) {
        return R.ok(dashboardService.dashboardOverview(bo));
    }
}

