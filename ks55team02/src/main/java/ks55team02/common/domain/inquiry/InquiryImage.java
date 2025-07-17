package ks55team02.common.domain.inquiry;

import ks55team02.common.domain.store.StoreImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    // 스토어 이미지
    private StoreImage storeImage;
    
    // storeImage 필드에 대한 Getter와 Setter 추가
    public StoreImage getStoreImage() { // <--- 이 부분 추가
        return storeImage;
    }

    public void setStoreImage(StoreImage storeImage) { // <--- 이 부분 추가
        this.storeImage = storeImage;
    }

    @Override
    public String toString() {
        return "InquiryImage{" +
               "inqryImgId='" + inqryImgId + '\'' +
               ", imgId='" + imgId + '\'' +
               ", inqryId='" + inqryId + '\'' +
               ", ord=" + ord +
               ", storeImage=" + storeImage + // toString에도 추가하면 디버깅에 용이
               '}';
    }
}

