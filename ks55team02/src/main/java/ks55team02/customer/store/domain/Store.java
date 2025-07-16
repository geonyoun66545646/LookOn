package ks55team02.customer.store.domain;

import java.time.LocalDateTime;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Alias("CustomerStoreDomain")
public class Store {
    private String storeId; // 상점 ID (Primary Key)
    private String aplyId; // 신청 ID (외래키: store_application 테이블 참조)
    private String storeConm; // 상점 상호명
    private String storeIntroCn; // 상점 소개
    private String storeLogoImg; // 상점 로고 이미지 파일 (외래키: store_images 테이블 참조)
    private String storeStts; // 상점 상태
    private LocalDateTime infoMdfcnDt; // 정보 수정일시
    private LocalDateTime inactvtnDt; // 비활성화 일시
    private Boolean delPrcrYn; // 폐쇄 여부 (true: 폐쇄, false: 미폐쇄)
}