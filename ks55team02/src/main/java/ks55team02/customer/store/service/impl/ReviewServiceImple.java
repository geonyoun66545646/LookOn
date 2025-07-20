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
    
    @Override
    public void addReview(ProductReview productReview, String currentUserNo) {
        
        // 1. 리뷰 작성 자격 검증 (구매 이력 확인)
        String ordrDtlArtclNo = reviewMapper.findReviewableOrderItem(
                currentUserNo,
                productReview.getOrdrNo(),
                productReview.getGdsNo()
        );
        
        if (ordrDtlArtclNo == null) {
            throw new IllegalArgumentException("리뷰를 작성할 권한이 없습니다. (구매 내역 불일치)");
        }
        
        // 2. 중복 리뷰 작성 검증
        int reviewCount = reviewMapper.countReviewByOrderItem(ordrDtlArtclNo);
        if (reviewCount > 0) {
            throw new IllegalStateException("이미 리뷰를 작성한 상품입니다.");
        }
        
        // 3. 새로운 리뷰 ID 생성 로직
        Integer maxIdNum = reviewMapper.findMaxReviewIdNumber();
        int nextIdNum = (maxIdNum == null) ? 1 : maxIdNum + 1; // maxIdNum이 null이면 (첫 리뷰) 1부터 시작
        String newReviewId = "review_" + nextIdNum;

        // 4. [수정됨] 컨트롤러에서 받은 productReview 객체에 서버에서 생성/검증한 값을 설정
        productReview.setReviewId(newReviewId);
        productReview.setOrdrDtlArtclNo(ordrDtlArtclNo);
        productReview.setPrchsrUserNo(currentUserNo);
        productReview.setReviewStts(true); // '게시됨' 상태를 true로 가정
        
        // 5. [수정됨] 사용자가 입력한 값과 서버가 추가한 값을 모두 가진 객체를 DB에 INSERT
        reviewMapper.insertReview(productReview);
    }
}