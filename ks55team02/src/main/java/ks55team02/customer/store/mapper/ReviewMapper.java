package ks55team02.customer.store.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.common.domain.store.ProductReview;
import ks55team02.common.domain.store.ReviewImage;
import ks55team02.common.domain.store.StoreImage;
import ks55team02.orderproduct.domain.OrderDTO;


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
                                @Param("ordrNo") String ordrNo,
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
 
 // 리뷰 가능 주문 조회
 OrderDTO findReviewableOrder(@Param("userNo") String userNo, 
		 						@Param("gdsNo") String gdsNo);
					 
 
 /**
  * 여러 StoreImage 정보를 DB에 배치 저장합니다.
  * @param storeImages 저장할 StoreImage 객체 리스트
  * @return INSERT된 행의 수
  */
 int addStoreImages(List<StoreImage> storeImages); // 배치 삽입 메서드 추가

 /**
  * 여러 ReviewImage 정보를 DB에 배치 저장합니다. (리뷰와 이미지 매핑)
  * @param reviewImages 저장할 ReviewImage 객체 리스트
  * @return INSERT된 행의 수
  */
 int addReviewImages(List<ReviewImage> reviewImages); // 배치 삽입 메서드 추가
 
}


 