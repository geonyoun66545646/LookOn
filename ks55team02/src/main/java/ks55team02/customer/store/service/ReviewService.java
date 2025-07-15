package ks55team02.customer.store.service;

import java.util.List;

import ks55team02.common.domain.store.ProductReview;

public interface ReviewService {

 /**
  * 특정 상품 코드에 해당하는 모든 리뷰를 조회합니다.
  *
  * @param productCode 상품 코드
  * @return 해당 상품의 리뷰 목록
  */
 List<ProductReview> getReviewsByProductCode(String productCode);

 // TODO: 리뷰 작성, 수정, 삭제, 좋아요/취소 등의 메서드를 여기에 추가할 수 있습니다.
}