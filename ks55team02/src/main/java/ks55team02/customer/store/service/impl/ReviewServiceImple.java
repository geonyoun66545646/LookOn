package ks55team02.customer.store.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.common.domain.store.ProductReview;
import ks55team02.common.domain.store.ReviewImage;
import ks55team02.customer.store.mapper.ReviewMapper;
import ks55team02.customer.store.service.ReviewService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional // 데이터 조회 작업이지만, 일관성을 위해 붙이는 것을 권장합니다.
@RequiredArgsConstructor
public class ReviewServiceImple implements ReviewService { // 클래스명 수정

    private final ReviewMapper reviewMapper;

    /**
     * 특정 상품 코드에 해당하는 모든 리뷰와 각 리뷰에 포함된 이미지들을 한 번의 쿼리로 조회합니다.
     * <p>
     * MyBatis의 <collection> 매핑을 사용하므로, 서비스 로직에서 추가적인 DB 조회가 필요 없습니다.
     * N+1 문제가 해결된 최적화된 방식입니다.
     * </p>
     *
     * @param productCode 상품 코드 (gdsNo)
     * @return 해당 상품의 전체 리뷰 목록 (각 리뷰 객체는 연관된 이미지 목록을 포함하고 있음)
     */
    @Override
    public List<ProductReview> getReviewsByProductCode(String productCode) {
        // [수정] 복잡한 로직이 모두 사라지고, Mapper 호출 한 줄로 모든 작업이 완료됩니다.
        // reviewMapper.selectReviewsByProductCode 쿼리가 JOIN과 <collection> 매핑을 통해
        // ProductReview 객체 내의 reviewImages 리스트까지 모두 채워서 반환해 줍니다.
        return reviewMapper.selectReviewsByProductCode(productCode);
    }
}