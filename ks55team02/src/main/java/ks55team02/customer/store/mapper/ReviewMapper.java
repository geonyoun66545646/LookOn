package ks55team02.customer.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
  * 사용자가 특정 주문에서 특정 상품을 구매했는지 확인하고,
  * 리뷰 작성이 가능한 주문 상세 항목 번호(ordr_dtl_artcl_no)를 반환합니다.
  * @param userNo 현재 로그인한 사용자 번호
  * @param ordNo 폼으로 받은 주문 번호
  * @param gdsNo 폼으로 받은 상품 번호
  * @return 주문 상세 항목 번호 (결과 없으면 null)
  */
 String findReviewableOrderItem(@Param("userNo") String userNo,
                                @Param("ordNo") String ordNo,
                                @Param("gdsNo") String gdsNo);

 /**
  * 특정 주문 상세 항목에 대해 이미 리뷰가 존재하는지 확인합니다.
  * @param ordrDtlArtclNo 주문 상세 항목 번호
  * @return 존재하면 1, 아니면 0
  */
 int countReviewByOrderItem(String ordrDtlArtclNo);

 /**
  * ProductReview 객체를 받아 DB에 새로운 리뷰를 저장합니다.
  * @param review 저장할 리뷰 정보 객체
  * @return INSERT된 행의 수
  */
 int insertReview(ProductReview review);
 
 
 Integer findMaxReviewIdNumber();
 
 
}


 // TODO: 리뷰 등록, 수정, 삭제, 좋아요/취소 관련 메서드를 여기에 추가할 수 있습니다.
 // int insertReview(ProductReview review);
 // int updateReview(ProductReview review);
 // int deleteReview(String reviewId);
 // int insertReviewLike(ReviewLike reviewLike);
 // int deleteReviewLike(String reviewId, String likeUserNo);
 // ProductReview selectReviewById(String reviewId);
 // int updateReviewHelpfulCount(String reviewId, int increment);
