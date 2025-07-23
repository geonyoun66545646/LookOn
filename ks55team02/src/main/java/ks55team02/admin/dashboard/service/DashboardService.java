package ks55team02.admin.dashboard.service;

import ks55team02.admin.dashboard.domain.Dashboard;

public interface DashboardService {

	/**
     * 대시보드에 필요한 모든 데이터를 조회하고 가공하여 반환하는 메서드
     * @return 대시보드 데이터를 담은 DashboardDTO 객체
     */
    Dashboard getDashboardData();
}
