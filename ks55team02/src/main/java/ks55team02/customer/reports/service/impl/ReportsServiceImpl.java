package ks55team02.customer.reports.service.impl;

// --- [ 1. 수정된 Import 목록 ] ---
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map; // Map 사용을 위해 import 추가
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.reports.domain.ReportAttachment;
import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;
import ks55team02.customer.reports.mapper.ReportsMapper;
import ks55team02.customer.reports.service.ReportsService;
import ks55team02.util.CustomerPagination;

@Service
@Transactional
public class ReportsServiceImpl implements ReportsService {

	// --- [ 2. 추가된 멤버 변수 ] ---
	private static final Logger log = LoggerFactory.getLogger(ReportsServiceImpl.class);
	@Value("${file.path}")
	private String fileRealPath;

	private final ReportsMapper reportsMapper;

	public ReportsServiceImpl(ReportsMapper reportsMapper) {
		this.reportsMapper = reportsMapper;
	}

	@Override
	public List<String> getReportTargetTypeList() {
		return reportsMapper.getReportTargetTypeList();
	}

	@Override
	public List<ReportsReasons> getActiveReportReasonList(String targetType) {
		return reportsMapper.getActiveReportReasonList(targetType);
	}

	@Override
	public void addReport(Reports report, List<MultipartFile> evidenceFiles) throws IOException {

		String latestId = reportsMapper.getLatestReportId();
		int newIdNum = 1;
		if (latestId != null && latestId.startsWith("RPT_ID_")) {
			String numericPart = latestId.substring(7);
			newIdNum = Integer.parseInt(numericPart) + 1;
		}
		String newReportId = String.format("RPT_ID_%03d", newIdNum);
		report.setDclrId(newReportId);

		// ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
		// ★ [추가] 빈 문자열 ID를 DB에 INSERT하기 전에 NULL로 변환합니다. ★
		// ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★
		if (report.getDclrTrgtUserNo() != null && report.getDclrTrgtUserNo().isEmpty()) {
			report.setDclrTrgtUserNo(null);
		}
		if (report.getDclrTrgtContsId() != null && report.getDclrTrgtContsId().isEmpty()) {
			report.setDclrTrgtContsId(null);
		}
		// ★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★★

		reportsMapper.addReport(report);

		if (evidenceFiles != null && !evidenceFiles.isEmpty()) {
			for (MultipartFile file : evidenceFiles) {
				if (file.isEmpty())
					continue;

				String saveDirectory = fileRealPath + "/attachment/reports";
				String today = new SimpleDateFormat("yyyyMMdd").format(new Date());
				String dateDirectory = saveDirectory + "/" + today;

				File dir = new File(dateDirectory);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				String originalFilename = file.getOriginalFilename();
				String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

				Path filePath = Paths.get(dateDirectory, uniqueFilename);
				file.transferTo(filePath);

				ReportAttachment attachment = new ReportAttachment();
				attachment.setFileId(UUID.randomUUID().toString());
				attachment.setDclrId(newReportId);
				attachment.setOriginalFilename(originalFilename);
				attachment.setFilePath("/attachment/reports/" + today + "/" + uniqueFilename);
				attachment.setFileSize(file.getSize());

				reportsMapper.insertReportAttachment(attachment);
			}
		}
	}

	/**
	 * [수정] 검색 및 페이징 로직이 포함된 getMyReportList 메소드
	 */
	@Override
	public CustomerPagination<Map<String, Object>> getMyReportList(String userNo, String searchKeyword,
			int currentPage) {

		// 1. 페이지네이션에 필요한 상수 정의
		final int pageSize = 10; // 한 페이지에 보여줄 아이템 개수
		final int blockSize = 5; // 페이지네이션 블록에 보여줄 페이지 번호 개수

		// 2. 검색 조건에 맞는 전체 아이템 개수 조회
		long totalCount = reportsMapper.getTotalReportCount(userNo, searchKeyword);
		log.info("Total report count for userNo {}: {}", userNo, totalCount);

		// 3. 전체 아이템 개수를 기반으로 CustomerPagination 객체 생성
		// (이 시점에는 데이터 목록(list)은 비어있습니다.)
		CustomerPagination<Map<String, Object>> pagination = new CustomerPagination<>(new ArrayList<>(), totalCount,
				currentPage, pageSize, blockSize);

		// 4. 현재 페이지에 해당하는 데이터 목록 조회
		// - LIMIT : 한 페이지에 보여줄 개수 (pageSize)
		// - OFFSET: 건너뛸 아이템 개수 ( (현재페이지-1) * 페이지사이즈 )
		int offset = (pagination.getCurrentPage() - 1) * pageSize;
		List<Map<String, Object>> reportList = reportsMapper.getMyReportList(userNo, searchKeyword, pageSize, offset);

		// 5. 조회된 데이터 목록을 pagination 객체에 설정
		pagination.setList(reportList);

		// 6. 모든 정보가 담긴 pagination 객체를 반환
		return pagination;
	}

	// --- getReportDetail 메소드 (수정 없음) ---
	@Override
	public Map<String, Object> getReportDetail(String dclrId) {
		// 1. 신고 기본 정보를 조회합니다.
		Map<String, Object> reportDetail = reportsMapper.getReportDetail(dclrId);

		// 2. 만약 조회된 신고가 없다면, null을 반환하여 로직을 중단합니다.
		if (reportDetail == null) {
			return null;
		}

		// 3. 해당 신고의 첨부파일 목록을 조회합니다.
		List<ReportAttachment> attachments = reportsMapper.getAttachmentsByDclrId(dclrId);

		// 4. 조회된 첨부파일 목록을 기본 정보 Map에 'attachments'라는 key로 추가합니다.
		reportDetail.put("attachments", attachments);

		// 5. 완벽하게 조합된 데이터를 Controller로 반환합니다.
		return reportDetail;
	}
}