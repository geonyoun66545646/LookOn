package ks55team02.tossApi.domain;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class TossConfirmResponseDto {
	
	private String paymentKey;
    private String orderId;
    private String orderName;
    private String status;
    private String method;
    private BigDecimal totalAmount;
    private String requestedAt;
    private String approvedAt;
    private Card card; // 카드 정보는 중첩된 JSON 객체이므로, 내부 클래스로 처리합니다.

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @NoArgsConstructor
    public static class Card { // static 내부 클래스로 정의
        private String company; // 카드사 이름
    }
    
}
