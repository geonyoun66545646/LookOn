package ks55team02.common.domain.store;

import lombok.Data;

@Data
public class ReviewImage {

    private String reviewImgId; // 리뷰 이미지 ID (review_img_id)
    private String reviewId;    // 리뷰 ID (review_id) - 어떤 리뷰에 속하는 이미지인지
    private String imgId;       // 이미지 ID (img_id) - 실제 이미지 파일 정보 (store_images 테이블의 PK)
    private Integer ord;        // 이미지 순서 (ord)
    
    private StoreImage storeImage; // 스토어 이미지 매핑 
}
