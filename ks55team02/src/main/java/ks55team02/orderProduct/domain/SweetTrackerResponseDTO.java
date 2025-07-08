package ks55team02.orderProduct.domain;

import lombok.Data;

@Data
public class SweetTrackerResponseDTO {
	private String code;
    private String message;
    private SweetTrackerDataDTO data; // 실제 배송 정보는 'data' 필드에 담김
}
