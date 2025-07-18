package ks55team02.common.domain.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime; // DDL의 datetime에 맞춰 LocalDateTime 사용

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreImage {
    private String 			imgId;
    private String 			imgAddr;      // 이미지 경로
    private String 			imgFileNm;    // 이미지 파일명 (원본 파일명)
    private long 			imgFileSz;   // 이미지 파일 크기 (KB, DDL에 int로 되어있음)
    private String 			imgTypeCd;    // 이미지 유형 코드 (예: PNG, JPG)
    private LocalDateTime 	regYmd;
    private Boolean 		delYn;       
}