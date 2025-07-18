package ks55team02.customer.reports.service;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;

public interface ReportsService {

	// 활성화된 신고 사유 목록 조회
	List<ReportsReasons> getActiveReportReasonList(String targetType);

	// 신고 접수 처리 (MultipartFile 파라미터 추가)
	// 파일을 다루는 작업은 IOException을 던질 수 있으므로, throws IOException을 추가하는 것이 좋습니다.
	void addReport(Reports report, List<MultipartFile> evidenceFiles) throws IOException;

	List<String> getReportTargetTypeList();

}