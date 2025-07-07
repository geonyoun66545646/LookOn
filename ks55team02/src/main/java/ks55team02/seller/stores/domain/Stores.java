package ks55team02.seller.stores.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class Stores {
    // 상점(브랜드) ID
    private String storeId;          
    // 신청자 ID
    private String aplyId;           
    // 상점(브랜드) 회사명
    private String storeConm;        
    // 상점 소개 내용
    private String storeIntroCn;     
    // 상점 로고 이미지
    private String storeLogoImg;     
    // 상점 상태
    private String storeStts;        
    // 정보 수정일
    private LocalDateTime infoMdfcnDt;
    // 비활성화 일시
    private LocalDateTime inactvtnDt;  
    // 삭제 처리 여부
    private boolean delPrcrYn;       
}