package ks55team02.customer.reports.service.impl;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;
import ks55team02.customer.reports.mapper.ReportsMapper;
import ks55team02.customer.reports.service.ReportsService;

@Service
@Transactional
public class ReportsServiceImpl implements ReportsService {

	private final ReportsMapper reportsMapper;

	// 생성자 주입 (Constructor Injection)
	public ReportsServiceImpl(ReportsMapper reportsMapper) {
		this.reportsMapper = reportsMapper;
	}

	@Override
	public List<String> getReportTargetTypeList() {
		return reportsMapper.getReportTargetTypeList();
	}

	@Override
	public List<ReportsReasons> getActiveReportReasonList(String targetType) {
		// 별도의 비즈니스 로직 없이 Mapper 호출만으로 충분
		return reportsMapper.getActiveReportReasonList(targetType);
	}

	@Override
	public void addReport(Reports report) {
		// [핵심 비즈니스 로직]
		// 1. 새로운 신고 ID 생성 (RPT_ID_XXX 형식)
		String latestId = reportsMapper.getLatestReportId(); // DB에서 가장 마지막 ID 가져오기
		int newIdNum = 1;

		if (latestId != null && latestId.startsWith("RPT_ID_")) {
			// 마지막 ID가 존재하면, 숫자 부분만 잘라내서 1을 더함
			String numericPart = latestId.substring(7); // "RPT_ID_" 다음부터 자르기
			newIdNum = Integer.parseInt(numericPart) + 1;
		}

		// 새로운 ID를 3자리 숫자로 포맷팅 (예: 1 -> "001", 12 -> "012")
		String newReportId = String.format("RPT_ID_%03d", newIdNum);

		report.setDclrId(newReportId);

		// 2. Mapper를 통해 DB에 신고 정보 INSERT
		reportsMapper.addReport(report);
	}

}