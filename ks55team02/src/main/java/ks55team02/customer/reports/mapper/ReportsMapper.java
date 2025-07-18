package ks55team02.customer.reports.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.customer.reports.domain.ReportAttachment;
import ks55team02.customer.reports.domain.Reports;
import ks55team02.customer.reports.domain.ReportsReasons;

@Mapper
public interface ReportsMapper {

	// 신고 접수 페이지: 활성화된 신고 사유 목록 조회
	// (예: 신고 대상이 '회원'일 때와 '게시글'일 때 다른 사유를 보여주기 위함)
	List<ReportsReasons> getActiveReportReasonList(String targetType);

	// 신고 접수 처리
	int addReport(Reports report);
	
	// 가장 마지막 신고 id를 받아오는 메서드 
	String getLatestReportId();
	
	// 신고 대상 유형 목록 조회
	List<String> getReportTargetTypeList();
	
	void insertReportAttachment(ReportAttachment attachment);
}