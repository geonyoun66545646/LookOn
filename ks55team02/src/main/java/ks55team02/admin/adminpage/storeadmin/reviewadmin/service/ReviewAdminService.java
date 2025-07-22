package ks55team02.admin.adminpage.storeadmin.reviewadmin.service;

import java.util.List;

import ks55team02.common.domain.store.ProductReview; // 임포트 경로 변경

public interface ReviewAdminService {

    int getReviewCount(ProductReview review);

    List<ProductReview> getReviewList(ProductReview review, int limitStart, int pageSize);

    /**
     * 선택된 리뷰들의 상태를 변경하고, 관련 날짜 및 처리자 정보를 업데이트합니다.
     * @param reviewIds 상태를 변경할 리뷰 ID 리스트
     * @param newStatus 변경할 새로운 상태 (0: 삭제/숨김, 1: 활성) - Integer 유지
     * @param loggedInUserLgnId 현재 로그인된 사용자 ID
     */
    void updateReviewStatus(List<String> reviewIds, Integer newStatus, String loggedInUserLgnId);
}
