package ks55team02.admin.adminpage.storeadmin.reviewadmin.service;

import java.util.List;
import ks55team02.common.domain.store.ProductReview;

public interface ReviewAdminService {

    /**
     * 조건에 맞는 리뷰의 총 개수를 조회합니다.
     * @param review 검색 및 필터 조건
     * @return 총 리뷰 개수
     */
    int getReviewCount(ProductReview review);

    /**
     * [수정] 조건에 맞는 리뷰 목록을 페이징하여 조회합니다.
     * @param review 검색, 필터, 페이징 정보가 모두 담긴 객체
     * @return 리뷰 목록
     */
    List<ProductReview> getReviewList(ProductReview review);

    /**
     * [추가] 선택된 리뷰들의 상태를 일괄 변경합니다.
     * @param reviewIds 상태를 변경할 리뷰 ID 리스트
     * @param newStatus 변경할 새로운 상태 ('ACTIVE', 'HIDDEN', 'DELETED')
     * @param loggedInUserLgnId 현재 로그인된 관리자 ID
     */
    void updateReviewStatusBatch(List<String> reviewIds, String newStatus, String loggedInUserLgnId);
    
    // [삭제] 아래 메소드는 더 이상 사용하지 않으므로 삭제합니다.
    // void updateReviewStatus(List<String> reviewIds, Integer newStatus, String loggedInUserLgnId);
}