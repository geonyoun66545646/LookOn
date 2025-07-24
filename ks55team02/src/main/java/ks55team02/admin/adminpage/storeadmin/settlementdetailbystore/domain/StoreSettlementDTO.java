package ks55team02.admin.adminpage.storeadmin.settlementdetailbystore.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class StoreSettlementDTO {
	
	// ====================== 이 필드를 추가해주세요 ======================
    /**
     * 데이터베이스의 AUTO_INCREMENT PK 값을 담기 위한 필드.
     * 시스템 내부에서만 사용하며, ID 생성의 안전성을 보장합니다.
     */
    private Long id;
    // ===============================================================
	
	private String storeClclnId; 	// 스토어 정산 아이디 (PK)
    private String storeId;      	// 상점 ID (FK)
    private String plcyId;       	// 정책 ID (FK)
    private BigDecimal totSelAmt;   // 총 판매 금액
    private BigDecimal selFeeRt;    // 판매 수수료율(%)
    private BigDecimal clclnAmt;    // 정산 금액
    private String actnoId;      	// 계좌 ID (FK)
    private LocalDate clclnPrcsYmd; // 정산 처리일
    private String clclnSttsCd;  	// 정산 상태 코드
    
    // 추가: 정산 정보 생성/수정 일시는 테이블 스키마에 없으므로 일단 제외합니다.
    // 필요하다면 테이블 스키마에 추가 후 여기에 필드를 추가하세요.
    
}
