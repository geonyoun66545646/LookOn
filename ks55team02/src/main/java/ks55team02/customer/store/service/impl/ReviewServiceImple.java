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
     // 복잡한 반복문 로직이 모두 사라지고, Mapper 호출 한 줄로 모든 작업이 완료됩니다.
     // reviewMapper.selectReviewsByProductCode 쿼리가 JOIN과 <collection> 매핑을 통해
     // ProductReview 객체 내의 reviewImages 리스트까지 모두 채워서 반환해 줍니다.
     return reviewMapper.selectReviewsByProductCode(productCode);
 }
}