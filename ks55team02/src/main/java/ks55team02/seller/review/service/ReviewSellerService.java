package ks55team02.seller.review.service;

import java.util.List;

import ks55team02.common.domain.store.ProductReview;

public interface ReviewSellerService {

	 /**
     * 조건에 맞는 리뷰의 총 개수를 조회합니다.
     * @param review 검색 및 필터 조건
     * @return 총 리뷰 개수
     */
    int getSellerReviewCount(ProductReview review);

    /**
     * [수정] 조건에 맞는 리뷰 목록을 페이징하여 조회합니다.
     * @param review 검색, 필터, 페이징 정보가 모두 담긴 객체
     * @return 리뷰 목록
     */
    List<ProductReview> getSellerReviewList(ProductReview review);

}
