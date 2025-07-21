package ks55team02.customer.store.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // MultipartFile import 추가

import ks55team02.common.domain.store.ProductReview;
import ks55team02.common.domain.store.ReviewImage;
import ks55team02.common.domain.store.StoreImage;
import ks55team02.customer.store.domain.ReviewAddDto; // DTO 클래스명 확인
import ks55team02.customer.store.mapper.ReviewMapper;
import ks55team02.customer.store.service.ReviewService;
import ks55team02.orderproduct.domain.OrderDTO;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 로그 사용을 위해 추가

@Slf4j // 로그 사용을 위한 어노테이션
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImple implements ReviewService { // 클래스명 관례에 맞게 수정 (ReviewServiceImple -> ReviewServiceImpl)

    private final ReviewMapper reviewMapper;
    private final FilesUtils filesUtils;
    
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
        // ⭐⭐⭐ 7. 첨부된 파일 처리 로직 구현 시작 (참조 코드 기반) ⭐⭐⭐
        List<MultipartFile> reviewImages = reviewAddDto.getReviewImages();
        if (reviewImages != null && !reviewImages.isEmpty() && !reviewImages.get(0).isEmpty()) { // 파일이 실제로 있는지 확인
            List<StoreImage> storeImagesToInsert = new ArrayList<>();
            List<ReviewImage> reviewImagesToInsert = new ArrayList<>();
            int order = 0;

            // FilesUtils를 사용하여 파일 저장 및 FileDetail 리스트 얻기
            // "review_images"는 FilesUtils 내부에서 사용할 서브 경로 (예: /upload/review_images/)
            List<FileDetail> fileDetails = filesUtils.saveFiles(reviewImages.toArray(new MultipartFile[0]), "review_images");

            for (FileDetail fileDetail : fileDetails) {
                // 7-1. StoreImage 객체 생성 및 리스트에 추가 (store_images 테이블용)
                String imgId = "img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER)
                                     + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                StoreImage storeImage = StoreImage.builder()
                        .imgId(imgId)
                        .imgFileNm(fileDetail.getOriginalFileName()) // 원본 파일명
                        .imgAddr(fileDetail.getSavedPath()) // 저장된 경로 (예: /upload/review_images/UUID.확장자)
                        .imgFileSz(fileDetail.getFileSize())
                        .imgTypeCd(fileDetail.getFileExtension()) // 파일 확장자
                        .regYmd(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .delYn(false)
                        .build();
                storeImagesToInsert.add(storeImage);

                // 7-2. ReviewImage 객체 생성 및 리스트에 추가 (review_images 테이블용)
                String reviewImgId = "review_img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER)
                                          + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                ReviewImage reviewImage = ReviewImage.builder()
                        .reviewImgId(reviewImgId)
                        .reviewId(newReviewId) // 새로 생성된 리뷰 ID
                        .imgId(imgId) // 위에서 생성한 StoreImage의 imgId
                        .ord(order++) // 이미지 순서
                        .build();
                reviewImagesToInsert.add(reviewImage);
            }

            // 7-3. StoreImage 데이터 저장 (배치 삽입)
            if (!storeImagesToInsert.isEmpty()) {
                reviewMapper.addStoreImages(storeImagesToInsert);
                log.info("{}개의 StoreImage 정보 저장 성공.", storeImagesToInsert.size());
            }

            // 7-4. ReviewImage 데이터 저장 (배치 삽입)
            if (!reviewImagesToInsert.isEmpty()) {
                reviewMapper.addReviewImages(reviewImagesToInsert);
                log.info("{}개의 ReviewImage 정보 저장 성공.", reviewImagesToInsert.size());
            }
        } // <--- addReview 메서드 내 이미지 처리 if 블록의 닫는 중괄호
    } // <--- addReview 메서드의 닫는 중괄호
    @Override
    public OrderDTO findReviewableOrder(String userNo, String gdsNo) {
        return reviewMapper.findReviewableOrder(userNo, gdsNo);
    }
}