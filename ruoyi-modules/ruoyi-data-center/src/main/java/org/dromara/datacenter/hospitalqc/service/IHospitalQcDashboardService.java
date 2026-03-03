package org.dromara.datacenter.hospitalqc.service;

import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcDashboardQueryBo;
import org.dromara.datacenter.hospitalqc.domain.bo.HospitalQcReportQueryBo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDashboardFilterVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcDashboardOverviewVo;
import org.dromara.datacenter.hospitalqc.domain.vo.HospitalQcReportRowVo;

import java.util.List;

/**
 * Dashboard and report query service.
 */
public interface IHospitalQcDashboardService {

    HospitalQcDashboardFilterVo selectFilterOptions();

    HospitalQcDashboardOverviewVo dashboardOverview(HospitalQcDashboardQueryBo bo);

    List<HospitalQcReportRowVo> reportSummary(HospitalQcReportQueryBo bo);
}

