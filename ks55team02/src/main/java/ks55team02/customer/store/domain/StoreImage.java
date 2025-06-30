package ks55team02.customer.store.domain;

import lombok.Data;
import java.time.LocalDateTime; // DDL의 datetime에 맞춰 LocalDateTime 사용

@Data
public class StoreImage {
    private String imgId;
    private String imgAddr;      // 이미지 경로
    private String imgFileNm;    // 이미지 파일명 (원본 파일명)
    private Integer imgFileSz;   // 이미지 파일 크기 (KB, DDL에 int로 되어있음)
    private String imgTypeCd;    // 이미지 유형 코드 (예: PNG, JPG)
    private LocalDateTime regYmd;
    private Boolean delYn;       
}