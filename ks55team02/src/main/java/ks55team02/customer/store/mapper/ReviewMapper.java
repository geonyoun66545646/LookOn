package ks55team02.customer.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.common.domain.store.ProductReview;
import ks55team02.common.domain.store.ReviewImage;

@Mapper
public interface ReviewMapper {

 /**
  * 특정 상품 코드에 해당하는 리뷰 목록을 조회합니다.
  *
  * @param gdsNo 상품 ID (productCode에 해당)
  * @return ProductReview 목록
  */
 List<ProductReview> selectReviewsByProductCode(String gdsNo);

 /**
  * 특정 리뷰 ID에 해당하는 이미지 목록을 조회합니다.
  *
  * @param reviewId 리뷰 ID
  * @return ReviewImage 목록
  */
 List<ReviewImage> selectReviewImagesByReviewId(String reviewId);

 // TODO: 리뷰 등록, 수정, 삭제, 좋아요/취소 관련 메서드를 여기에 추가할 수 있습니다.
 // int insertReview(ProductReview review);
 // int updateReview(ProductReview review);
 // int deleteReview(String reviewId);
 // int insertReviewLike(ReviewLike reviewLike);
 // int deleteReviewLike(String reviewId, String likeUserNo);
 // ProductReview selectReviewById(String reviewId);
 // int updateReviewHelpfulCount(String reviewId, int increment);
}