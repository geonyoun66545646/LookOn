package ks55team02.tossApi.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentHistory {
	
	private String stlmHstryId;     // 결제 이력 ID (payments_history 테이블의 PK)
    private String stlmId;          // 결제 ID (FK, payments 테이블을 참조)
    private LocalDateTime hstryCrtDt; // 이력 생성 일시
    private LocalDateTime hstryMdfcnDt; // 이력 수정 일시
    
}
