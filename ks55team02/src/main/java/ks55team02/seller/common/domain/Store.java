package ks55team02.seller.common.domain;

import java.time.LocalDateTime;

import org.apache.ibatis.type.Alias;

import ks55team02.common.domain.store.StoreImage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Alias("SellerStoreDomain") 
public class Store {

    private String 				storeId; // 상점 ID
    private String 				aplyId; // 신청 ID
    private String 				storeConm; // 상점 상호명
    private String 				storeIntroCn; // 상점 소개
    private String 				storeLogoImg; // 상점 로고 이미지 파일
    private String 				storeStts; // 상점 상태
    private LocalDateTime 		infoMdfcnDt; // 정보 수정일시
    private LocalDateTime 		inactvtnDt; // 비활성화 일시
    private Boolean 			delPrcrYn; // 폐쇄 여부 (tinyint(1)은 보통 boolean으로 매핑됩니다)
    
    private StoreImage logo;
    
}