package ks55team02.customer.store.service.impl;

import java.io.IOException; // 파일 처리 시 필요할 수 있으므로 추가
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // MultipartFile import 추가

import ks55team02.common.domain.store.ProductReview;
// import ks55team02.common.domain.store.ReviewImage; // 지금 당장 사용하지 않음
import ks55team02.customer.store.domain.ReviewAddDto; // DTO 클래스명 확인
import ks55team02.customer.store.mapper.ReviewMapper;
import ks55team02.customer.store.service.ReviewService;
import ks55team02.orderproduct.domain.OrderDTO;
// import ks55team02.seller.common.domain.Order; // 사용하지 않으므로 제거 또는 주석 처리
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 사용을 위해 추가

@Slf4j // 로그 사용을 위한 어노테이션
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService { // 클래스명 관례에 맞게 수정 (ReviewServiceImple -> ReviewServiceImpl)

    private final ReviewMapper reviewMapper;
    
    // TODO: 파일 처리 로직을 담당할 FileService를 나중에 주입해야 합니다.
    // private final FileService fileService;

    /**
     * 특정 상품 코드에 해당하는 모든 리뷰와 이미지를 조회합니다.
     */
    @Override
    public List<ProductReview> getReviewsByProductCode(String productCode) {
        return reviewMapper.selectReviewsByProductCode(productCode);
    }
    
    /**
     * 사용자가 작성한 리뷰를 등록합니다. 텍스트 정보와 파일 정보를 함께 처리합니다.
     */
    @Override
    public void addReview(ReviewAddDto reviewAddDto, String currentUserNo) {
        
        // 1. DTO에서 Domain 객체로 데이터 옮겨 담기
        ProductReview productReview = new ProductReview();
        productReview.setGdsNo(reviewAddDto.getGdsNo());
        productReview.setOrdrNo(reviewAddDto.getOrdrNo());
        productReview.setEvlScr(reviewAddDto.getEvlScr());
        productReview.setReviewCn(reviewAddDto.getReviewCn());
        
        // 2. 리뷰 작성 자격 검증 (구매 이력 확인)
        // (참고: findReviewableOrder 메서드를 활용하여 이 로직을 더 단순화할 수도 있습니다.)
        String ordrDtlArtclNo = reviewMapper.findReviewableOrderItem(
                currentUserNo,
                productReview.getOrdrNo(),
                productReview.getGdsNo()
        );
        if (ordrDtlArtclNo == null) {
            throw new IllegalArgumentException("리뷰를 작성할 권한이 없습니다. (구매 내역 불일치)");
        }
        
        // 3. 중복 리뷰 작성 검증
        int reviewCount = reviewMapper.countReviewByOrderItem(ordrDtlArtclNo);
        if (reviewCount > 0) {
            throw new IllegalStateException("이미 리뷰를 작성한 상품입니다.");
        }
        
        // 4. 새로운 리뷰 ID 생성
        Integer maxIdNum = reviewMapper.findMaxReviewIdNumber();
        int nextIdNum = (maxIdNum == null) ? 1 : maxIdNum + 1;
        String newReviewId = "review_" + nextIdNum;

        // 5. 서버에서 생성/검증한 값들 설정
        productReview.setReviewId(newReviewId);
        productReview.setOrdrDtlArtclNo(ordrDtlArtclNo);
        productReview.setPrchsrUserNo(currentUserNo);
        productReview.setReviewStts(true); // '게시됨' 상태
        
        // 6. 리뷰 기본 정보(텍스트)를 DB에 INSERT
        reviewMapper.insertReview(productReview);
        log.info("리뷰 기본 정보 저장 성공. reviewId: {}", newReviewId);

        // 7. 첨부된 파일 처리 로직
        List<MultipartFile> reviewImages = reviewAddDto.getReviewImages();
        if (reviewImages != null && !reviewImages.isEmpty()) {
            log.info("{}개의 이미지 파일 처리를 시작합니다.", reviewImages.size());
            // TODO: 여기에 실제 파일 처리 로직을 구현해야 합니다.
            // 1. 파일을 서버 특정 위치에 저장 (ex: FileService.saveFile())
            // 2. 저장된 파일 정보를 `store_images` 테이블에 INSERT
            // 3. 생성된 reviewId와 imageId를 `review_images` 테이블에 INSERT (매핑)
            // 이 로직은 반복문 안에서 처리되어야 합니다.
            // try-catch로 감싸서 파일 처리 중 예외가 발생해도 롤백되도록 해야 합니다.
        }
        
    } // addReview 메서드 끝

    /**
     * 특정 사용자가 특정 상품에 대해 리뷰를 작성할 수 있는지 확인하고,
     * 가능하다면 대상 주문 정보를 반환합니다.
     */
    @Override
    public OrderDTO findReviewableOrder(String userNo, String gdsNo) {
        return reviewMapper.findReviewableOrder(userNo, gdsNo);
    }
} // ReviewServiceImpl 클래스 끝