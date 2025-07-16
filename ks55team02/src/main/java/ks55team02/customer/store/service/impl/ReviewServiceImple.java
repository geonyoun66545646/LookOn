package ks55team02.customer.store.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import ks55team02.common.domain.store.ProductReview;
import ks55team02.common.domain.store.ReviewImage;
import ks55team02.customer.store.mapper.ReviewMapper;
import ks55team02.customer.store.service.ReviewService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewServiceImple implements ReviewService {

 private final ReviewMapper reviewMapper;

 /**
  * 특정 상품 코드에 해당하는 모든 리뷰를 조회합니다.
  * 리뷰와 함께 해당 리뷰에 첨부된 이미지 정보도 함께 조회합니다.
  *
  * @param productCode 상품 코드 (gdsNo)
  * @return 해당 상품의 리뷰 목록 (이미지 포함)
  */
 @Override
 public List<ProductReview> getReviewsByProductCode(String productCode) {
     List<ProductReview> reviews = reviewMapper.selectReviewsByProductCode(productCode);

     // 각 리뷰에 대한 이미지 정보 조회 및 설정 (필요에 따라 최적화 가능)
     if (reviews != null && !reviews.isEmpty()) {
         for (ProductReview review : reviews) {
             List<ReviewImage> images = reviewMapper.selectReviewImagesByReviewId(review.getReviewId());
             review.setReviewImages(images);
         }
     }
     return reviews;
 }

 // TODO: 리뷰 작성, 수정, 삭제, 좋아요/취소 등의 메서드 구현
 // @Override
 // public int addReview(ProductReview review) { ... }
 // @Override
 // public int modifyReview(ProductReview review) { ... }
 // @Override
 // public int removeReview(String reviewId) { ... }
 // @Override
 // public int toggleReviewLike(String reviewId, String userNo) { ... }
}