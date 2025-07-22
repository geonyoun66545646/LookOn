package ks55team02.customer.store.service.impl;

import ks55team02.common.domain.store.ProductReview;
import ks55team02.common.domain.store.ReviewImage;
import ks55team02.common.domain.store.ReviewLike;
import ks55team02.common.domain.store.StoreImage;
import ks55team02.customer.store.domain.ReviewAddDto;
import ks55team02.customer.store.mapper.ReviewMapper;
import ks55team02.customer.store.service.ReviewService;
import ks55team02.orderproduct.domain.OrderDTO;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 리뷰 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;
    private final FilesUtils filesUtils;

    /* =================================================================== */
    /*                         '좋아요(도움이 돼요)' 관련                    */
    /* =================================================================== */

    /**
     * ✅ [최종 수정] 리뷰 '좋아요' 상태를 토글(추가/수정)합니다.
     * @param reviewId 리뷰 ID
     * @param userNo 사용자 번호
     * @return 작업 후의 '좋아요' 상태 (true: 좋아요, false: 취소)
     */
    @Override
    public boolean toggleReviewLike(String reviewId, String userNo) {
        
        // 1. UPSERT 쿼리에 필요한 DTO를 준비합니다.
        //    (최초 INSERT 시에만 사용될 like_id를 생성)
        Integer maxIdNum = reviewMapper.findMaxLikeIdNumber();
        int nextIdNum = (maxIdNum == null) ? 1 : maxIdNum + 1;
        String newLikeId = "like_" + nextIdNum;
        
        ReviewLike reviewLike = new ReviewLike();
        reviewLike.setLikeId(newLikeId);
        reviewLike.setReviewId(reviewId);
        reviewLike.setLikeUserNo(userNo);
        
        // 2. DB에 UPSERT 쿼리를 실행합니다.
        reviewMapper.upsertLike(reviewLike);
        log.info("리뷰 좋아요 UPSERT 실행 완료: reviewId={}, userNo={}", reviewId, userNo);
        
        // 3. 작업 후의 최종 상태를 DB에서 다시 조회하여 반환합니다.
        Boolean currentStatus = reviewMapper.findLikeStatusByUser(reviewId, userNo);
        
        return currentStatus != null && currentStatus;
    }

    /**
     * 특정 리뷰의 총 '좋아요' 개수를 조회합니다.
     */
    @Override
    public int countReviewLikes(String reviewId) {
        return reviewMapper.countLikesByReviewId(reviewId);
    }


    /* =================================================================== */
    /*                         리뷰 목록 조회 (페이지네이션)                   */
    /* =================================================================== */

    /**
     * 특정 상품의 리뷰 목록을 페이징하여 조회합니다. (사용자 '좋아요' 여부 포함)
     */
    @Override
    public List<ProductReview> getReviewListPageByGdsNo(String gdsNo, int currentPage, int pageSize, String currentUserNo) {
        
        int offset = (currentPage - 1) * pageSize;
        
        List<String> reviewIdList = reviewMapper.findReviewIdsByGdsNo(gdsNo, pageSize, offset);
        
        if (reviewIdList == null || reviewIdList.isEmpty()) {
            return Collections.emptyList();
        }
        
        Map<String, Object> params = new HashMap<>();
        params.put("reviewIdList", reviewIdList);
        params.put("currentUserNo", currentUserNo);
        
        return reviewMapper.findReviewsByIds(params);
    }

    /**
     * 특정 상품의 게시된 리뷰 총 개수를 조회합니다.
     */
    @Override
    public long getReviewCountByGdsNo(String gdsNo) {
        return reviewMapper.getReviewCountByGdsNo(gdsNo);
    }

    /* =================================================================== */
    /*                            리뷰 작성 관련                           */
    /* =================================================================== */

    /**
     * 새로운 리뷰를 등록합니다.
     */
    @Override
    public void addReview(ReviewAddDto reviewAddDto, String currentUserNo) {
        
        ProductReview productReview = new ProductReview();
        productReview.setGdsNo(reviewAddDto.getGdsNo());
        productReview.setOrdrNo(reviewAddDto.getOrdrNo());
        productReview.setEvlScr(reviewAddDto.getEvlScr());
        productReview.setReviewCn(reviewAddDto.getReviewCn());
        
        String ordrDtlArtclNo = reviewMapper.findReviewableOrderItem(
                currentUserNo, productReview.getOrdrNo(), productReview.getGdsNo()
        );
        if (ordrDtlArtclNo == null) {
            throw new IllegalArgumentException("리뷰를 작성할 권한이 없습니다. (구매 내역 불일치)");
        }
        
        int reviewCount = reviewMapper.countReviewByOrderItem(ordrDtlArtclNo);
        if (reviewCount > 0) {
            throw new IllegalStateException("이미 리뷰를 작성한 상품입니다.");
        }
        
        Integer maxIdNum = reviewMapper.findMaxReviewIdNumber();
        int nextIdNum = (maxIdNum == null) ? 1 : maxIdNum + 1;
        String newReviewId = "review_" + nextIdNum;

        productReview.setReviewId(newReviewId);
        productReview.setOrdrDtlArtclNo(ordrDtlArtclNo);
        productReview.setPrchsrUserNo(currentUserNo);

        reviewMapper.insertReview(productReview);
        log.info("리뷰 기본 정보 저장 성공. reviewId: {}", newReviewId);

        List<MultipartFile> reviewImages = reviewAddDto.getReviewImages();
        if (reviewImages != null && !reviewImages.isEmpty() && !reviewImages.get(0).isEmpty()) {
            List<StoreImage> storeImagesToInsert = new ArrayList<>();
            List<ReviewImage> reviewImagesToInsert = new ArrayList<>();
            int order = 0;

            List<FileDetail> fileDetails = filesUtils.saveFiles(reviewImages.toArray(new MultipartFile[0]), "review_images");

            for (FileDetail fileDetail : fileDetails) {
                String imgId = "img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER)
                                     + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                StoreImage storeImage = StoreImage.builder()
                        .imgId(imgId)
                        .imgFileNm(fileDetail.getOriginalFileName())
                        .imgAddr(fileDetail.getSavedPath())
                        .imgFileSz(fileDetail.getFileSize())
                        .imgTypeCd(fileDetail.getFileExtension())
                        .regYmd(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                        .delYn(false)
                        .build();
                storeImagesToInsert.add(storeImage);

                String reviewImgId = "review_img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER)
                                          + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
                ReviewImage reviewImage = ReviewImage.builder()
                        .reviewImgId(reviewImgId)
                        .reviewId(newReviewId)
                        .imgId(imgId)
                        .ord(order++)
                        .build();
                reviewImagesToInsert.add(reviewImage);
            }

            if (!storeImagesToInsert.isEmpty()) {
                reviewMapper.addStoreImages(storeImagesToInsert);
                reviewMapper.addReviewImages(reviewImagesToInsert);
                log.info("{}개의 리뷰 이미지 정보 저장 성공.", storeImagesToInsert.size());
            }
        }
    }

    /**
     * 리뷰 작성이 가능한 최근 주문 정보를 조회합니다.
     */
    @Override
    public OrderDTO findReviewableOrder(String userNo, String gdsNo) {
        return reviewMapper.findReviewableOrder(userNo, gdsNo);
    }
}