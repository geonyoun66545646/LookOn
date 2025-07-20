package ks55team02.orderproduct.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ShipmentDTO {
	private String dlvyNo; // 배송 번호 (Primary Key)
    private String ordrNo; // 주문 번호 (FK)
    private String stlmId; // 결제 ID (FK)
    private String dlvyCoNm; // 배송사 명칭
    private String waybilNo; // 운송장 번호 (Unique)
    private String dlvySttsCd; // 배송 상태 코드
    private LocalDateTime sndngDt; // 발송(출고) 일시
    private LocalDateTime dlvyCmptnDt; // 배송 완료 일시
    private LocalDateTime dlvyInfoCrtDt; // 배송 정보 생성 일시
    private LocalDateTime dlvyInfoMdfcnDt; // 배송 정보 수정 일시
}
