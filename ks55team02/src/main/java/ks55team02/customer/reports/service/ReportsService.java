package ks55team02.customer.reports.service;

import java.util.List;
import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;

public interface ReportsService {

    // 활성화된 신고 사유 목록 조회
    List<ReportsReasons> getActiveReportReasonList(String targetType);

    // 신고 접수 처리
    void addReport(Reports report);
    
    List<String> getReportTargetTypeList();

}