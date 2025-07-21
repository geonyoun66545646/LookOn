package ks55team02.customer.reports.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.customer.reports.domain.ReportAttachment;
import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;

@Mapper
public interface ReportsMapper {

	// 활성화된 신고 사유 목록 조회
	List<ReportsReasons> getActiveReportReasonList(String targetType);

	// 신고 접수 처리
	int addReport(Reports report);

	// 가장 마지막 신고 id를 받아오는 메서드
	String getLatestReportId();

	// 신고 대상 유형 목록 조회
	List<String> getReportTargetTypeList();

	// 첨부파일 정보 저장
	void insertReportAttachment(ReportAttachment attachment);

	/**
	 * [수정] 검색 및 페이지네이션 조건에 맞는 '내 신고 내역 목록'을 조회
	 * 
	 * @param userNo        신고자 고유 번호
	 * @param searchKeyword 검색어
	 * @param pageSize      한 페이지에 보여줄 개수 (LIMIT)
	 * @param offset        건너뛸 개수 (OFFSET)
	 * @return 페이징 처리된 목록
	 */
	List<Map<String, Object>> getMyReportList(@Param("userNo") String userNo,
			@Param("searchKeyword") String searchKeyword, @Param("pageSize") int pageSize, @Param("offset") int offset);

	// [수정] 신고의 '기본' 정보만 조회
	Map<String, Object> getReportDetail(String dclrId);

	// [추가] 특정 신고에 소속된 '첨부파일 목록'만 조회
	List<ReportAttachment> getAttachmentsByDclrId(String dclrId);

	/**
	 * [신규] 검색 조건에 맞는 내 신고 내역의 '전체 개수'를 조회
	 * 
	 * @param userNo        신고자 고유 번호
	 * @param searchKeyword 검색어
	 * @return 조건에 맞는 데이터의 총 개수
	 */
	long getTotalReportCount(@Param("userNo") String userNo, @Param("searchKeyword") String searchKeyword);

}