package ks55team02.tossApi.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // 모든 필드를 인자로 받는 생성자 자동 생성
public class TossConfirmRequestDto {
	
	private String paymentKey; // 토스가 발급한 결제 키
    private String orderId;    // 우리 시스템의 주문 ID
    private Long amount;       // 결제 금액
}
