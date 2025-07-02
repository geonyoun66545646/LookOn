package ks55team02.customer.inquiry.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryImage {
    private String inqryImgId;  // 문의 이미지 ID
    private String inqryId;     // 문의 ID (FK)
    private String imgId;       // 이미지 ID (FK - store_images 테이블의 img_id)
    private Integer ord;        // 이미지 순서
    private String imgAddr;     // 추가: store_images 테이블의 이미지 실제 경로 (조인해서 가져올 필드)
}
