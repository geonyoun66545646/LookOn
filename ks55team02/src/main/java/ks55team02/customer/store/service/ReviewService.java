package ks55team02.customer.store.service;

import java.io.IOException;
import java.util.List;

import ks55team02.common.domain.store.ProductReview;
import ks55team02.common.domain.store.ReviewImage;
import ks55team02.customer.store.domain.ReviewAddDto;
import ks55team02.orderproduct.domain.OrderDTO;
import ks55team02.seller.common.domain.Order;

public interface ReviewService {

 /**
  * 특정 상품 코드에 해당하는 모든 리뷰를 조회합니다.
  *
  * @param productCode 상품 코드
  * @return 해당 상품의 리뷰 목록
  */
 List<ProductReview> getReviewsByProductCode(String productCode);
 
 /**
  * 새로운 리뷰를 등록합니다.
  * 내부적으로 구매 이력, 중복 작성 여부를 검증합니다.
  * @param reviewForm 사용자가 폼에서 작성한 데이터
  * @param currentUserNo 현재 로그인한 사용자의 ID
  */
 void addReview(ReviewAddDto reviewAddDto, String currentUserNo) throws IOException;
 // ProductReview가 아닌 ReviewFormDTO를 받는 것이 더 명확합니다.
 
 // 리뷰 가능 주문내역 조회
 OrderDTO findReviewableOrder(String userNo, String gdsNo);
}

 // TODO: 리뷰 작성, 수정, 삭제, 좋아요/취소 등의 메서드를 여기에 추가할 수 있습니다.
