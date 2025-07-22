package ks55team02.customer.store.service;

import java.io.IOException;
import java.util.List;

import ks55team02.common.domain.store.ProductReview;
import ks55team02.customer.store.domain.ReviewAddDto;
import ks55team02.orderproduct.domain.OrderDTO;


public interface ReviewService {


 
 /**
  * 새로운 리뷰를 등록합니다.
  * 내부적으로 구매 이력, 중복 작성 여부를 검증합니다.
  * @param reviewForm 사용자가 폼에서 작성한 데이터
  * @param currentUserNo 현재 로그인한 사용자의 ID
  */
 void addReview(ReviewAddDto reviewAddDto, String currentUserNo) throws IOException;
 // ProductReview가 아닌 ReviewFormDTO를 받는 것이 더 명확합니다.
 
 // 리뷰 가능 주문내역 조회
 OrderDTO findReviewableOrder(String ordrNo, String userNo);

 // [신규] 특정 상품의 리뷰 총 개수 조회 (본문 제거)
 long getReviewCountByGdsNo(String gdsNo);

 // [신규] 특정 상품의 리뷰 목록을 페이징하여 조회 (본문 제거)
 List<ProductReview> getReviewListPageByGdsNo(String gdsNo, int currentPage, int pageSize);

}

 // TODO: 리뷰 작성, 수정, 삭제, 좋아요/취소 등의 메서드를 여기에 추가할 수 있습니다.
