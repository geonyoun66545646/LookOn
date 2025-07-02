package ks55team02.tossApi.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PaymentHistoryDTO {
	
	private String stlmHstryId;     // 결제 이력 ID
    private String stlmId;          // 결제 ID (payments 테이블과 연결)
    private LocalDateTime hstryCrtDt = LocalDateTime.now(); // 이력 생성 일시
    private LocalDateTime hstryMdfcnDt = LocalDateTime.now(); // 이력 수정 일시

}
