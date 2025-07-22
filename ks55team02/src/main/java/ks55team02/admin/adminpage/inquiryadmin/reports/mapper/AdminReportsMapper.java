package ks55team02.admin.adminpage.inquiryadmin.reports.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReport;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportDetail;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportHistory;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.ReportProcessRequest;
import ks55team02.admin.common.domain.SearchCriteria;

@Mapper
public interface AdminReportsMapper {
	
	/**
	 * report_history 테이블의 새로운 PK (hstry_id)를 생성하기 위한 메소드
	 * @return 생성된 새로운 이력 ID (예: hstry_001)
	 */
	String getNextReportHistoryId();
	
	   /**
     * 특정 신고의 처리 상태를 업데이트하는 쿼리
     * @param request 업데이트할 정보 (dclrId, newStatus)
     * @return 영향을 받은 행의 수 (보통 1)
     */
    int updateReportStatus(ReportProcessRequest request);

    /**
     * 신고 처리 이력을 새로 삽입하는 쿼리
     * @param request 삽입할 정보 (dclrId, prcsCn, newStatus, adminId 등)
     * @return 영향을 받은 행의 수 (보통 1)
     */
    int insertReportHistory(ReportProcessRequest request);


    /**
     * 조건에 맞는 전체 신고 데이터의 개수를 조회
     * @param searchCriteria 검색 조건
     * @return 전체 데이터 개수 (int)
     */
    int getAdminTotalReportCount(SearchCriteria searchCriteria);

    /**
     * 조건에 맞는 신고 목록을 페이징하여 조회
     * @param paramMap 검색 조건(searchCriteria)과 페이징 정보(pagination)가 모두 담긴 맵
     * @return 페이징 처리된 신고 목록
     */
    List<AdminReport> getAdminReportList(Map<String, Object> paramMap);
    
    /**
     * 신고 아이디(dclrId)를 기반으로 특정 신고의 상세 정보를 조회하는 쿼리
     * @param dclrId 조회할 신고의 PK
     * @return AdminReportDetail (신고 상세 정보 DTO)
     */
    AdminReportDetail getAdminReportDetailById(String dclrId);

    /**
     * 신고 아이디(dclrId)를 기반으로 해당 신고의 모든 처리 이력 목록을 조회하는 쿼리
     * @param dclrId 조회할 신고의 PK
     * @return List<AdminReportHistory> (신고 처리 이력 DTO 리스트)
     */
    List<AdminReportHistory> getAdminReportHistoryListById(String dclrId);

}