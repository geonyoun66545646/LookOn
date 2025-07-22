package ks55team02.admin.adminpage.inquiryadmin.reports.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReport;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportDetail;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.AdminReportHistory;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.ReportProcessRequest;
import ks55team02.admin.adminpage.inquiryadmin.reports.domain.UserSanction;
import ks55team02.admin.common.domain.SearchCriteria;

@Mapper
public interface AdminReportsMapper {

	/**
	 * report_history 테이블의 새로운 PK (hstry_id)를 생성하기 위한 메소드  
	 * 
	 * @return 생성된 새로운 이력 ID (예: hstry_001)
	 */
	String getNextReportHistoryId();

	/**
	 * 특정 신고의 처리 상태를 업데이트하는 쿼리  
	 * 
	 * @param request 업데이트할 정보 (dclrId, newStatus)
	 * @return 영향을 받은 행의 수 (보통 1)
	 */
	int updateReportStatus(ReportProcessRequest request);

	/**
	 * 신고 처리 이력을 새로 삽입하는 쿼리  
	 * 
	 * @param request 삽입할 정보 (dclrId, prcsCn, newStatus, adminId 등)
	 * @return 영향을 받은 행의 수 (보통 1)
	 */
	int insertReportHistory(ReportProcessRequest request);

	/**
	 * 조건에 맞는 전체 신고 데이터의 개수를 조회  
	 * 
	 * @param searchCriteria 검색 조건
	 * @return 전체 데이터 개수 (int)
	 */
	int getAdminTotalReportCount(SearchCriteria searchCriteria);

	/**
	 * 조건에 맞는 신고 목록을 페이징하여 조회  
	 * 
	 * @param paramMap 검색 조건(searchCriteria)과 페이징 정보(pagination)가 모두 담긴 맵
	 * @return 페이징 처리된 신고 목록
	 */
	List<AdminReport> getAdminReportList(Map<String, Object> paramMap);

	/**
	 * 신고 아이디(dclrId)를 기반으로 특정 신고의 상세 정보를 조회하는 쿼리  
	 * 
	 * @param dclrId 조회할 신고의 PK
	 * @return AdminReportDetail (신고 상세 정보 DTO)
	 */
	AdminReportDetail getAdminReportDetailById(String dclrId);

	/**
	 * 신고 아이디(dclrId)를 기반으로 해당 신고의 모든 처리 이력 목록을 조회하는 쿼리  
	 * 
	 * @param dclrId 조회할 신고의 PK
	 * @return List<AdminReportHistory> (신고 처리 이력 DTO 리스트)
	 */
	List<AdminReportHistory> getAdminReportHistoryListById(String dclrId);

	// [기존 추가된 메소드] 1. 신고 ID로 신고 대상자의 userNo를 조회하는 임무
	String getTargetUserNoByReportId(String dclrId);

	// [기존 추가된 메소드] 2. 특정 사용자의 상태를 업데이트하는 임무
	int updateUserStatus(@Param("userNo") String userNo, @Param("status") String status);

	// [기존 추가된 메소드] 3. user_sanctions 테이블에 제재 기록을 삽입하는 임무
	int insertSanction(UserSanction sanction);

	/**
	 * [새로 추가] 특정 사용자의 가장 최신 제재 기록을 조회하는 메소드입니다. 이 메소드는 사용자가 로그인 시 제재 상태를 확인하여 안내
	 * 페이지로 리다이렉트할 때 사용됩니다.
	 * 
	 * @param userNo 제재 기록을 조회할 사용자 번호
	 * @return UserSanction 객체 (가장 최신 기록) 또는 기록이 없을 경우 null
	 */
	UserSanction getLatestSanctionByUserNo(String userNo); // [새로 추가]

	/**
	 * user_sanctions 테이블의 새로운 PK (sanction_id)를 생성하기 위한 메소드
	 *
	 * @return 생성된 새로운 제재 ID (예: SANCT_00001)
	 */
	String getNextSanctionId(); // 이 부분을 추가하세요.

	// [새로 추가] 게시글/댓글/상품 소유자 조회 메소드
	/**
	 * 특정 게시글의 작성자 userNo를 조회합니다.
	 * 
	 * @param postSn 게시글 순번
	 * @return 작성자 userNo
	 */
	String getPostOwnerUserNo(String postSn);

	/**
	 * 특정 댓글의 작성자 userNo를 조회합니다.
	 * 
	 * @param commentSn 댓글 순번
	 * @return 작성자 userNo
	 */
	String getCommentOwnerUserNo(String commentSn);

	/**
	 * 특정 상품의 판매자 userNo를 조회합니다. (store_id를 통해 user_no 조회)
	 * 
	 * @param gdsNo 상품 번호
	 * @return 판매자 userNo
	 */
	String getProductOwnerUserNo(String gdsNo);

	// [새로 추가] 게시글/댓글/상품 상태 변경/삭제 메소드
	/**
	 * 게시글의 상태를 변경합니다 (삭제 조치, 숨김 등).
	 * 
	 * @param postSn 게시글 순번
	 * @param status 변경할 상태 코드 (예: 'DELETED', 'HIDDEN')
	 * @return 영향받은 행의 수
	 */
	int updatePostStatus(@Param("postSn") String postSn, @Param("status") String status);

	/**
	 * 댓글을 삭제합니다.
	 * 
	 * @param commentSn 댓글 순번
	 * @return 영향받은 행의 수
	 */
	int deleteComment(Map<String, Object> paramMap);

	/**
	 * 상품의 노출 여부를 변경합니다 (삭제 조치).
	 * 
	 * @param gdsNo      상품 번호
	 * @param exposureYn 노출 여부 (false: 노출 안함, true: 노출)
	 * @return 영향받은 행의 수
	 */
	int updateProductExposureYn(@Param("gdsNo") String gdsNo, @Param("exposureYn") boolean exposureYn);
}