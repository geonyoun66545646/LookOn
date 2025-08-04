package ks55team02.seller.review.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.common.domain.store.ProductReview;

@Mapper
public interface ReviewSellerMapper {

	  /**
     * 조건에 맞는 리뷰의 총 개수를 조회합니다.
     * @param review 검색 및 필터 조건 DTO
     * @return 총 리뷰 개수
     */
    int getSellerReviewCount(ProductReview review);

    /**
     * [수정] 조건에 맞는 리뷰 목록을 페이징하여 조회합니다.
     * ProductReview 객체 안에 모든 검색/필터/페이징 정보가 담겨 있습니다.
     * @param review 검색, 필터, 페이징 정보 DTO
     * @return 리뷰 목록
     */
    List<ProductReview> getSellerReviewList(ProductReview review);
}
