package ks55team02.seller.products.domain; // 실제 패키지 경로에 맞게 수정

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StatusOptionMapping {
    private String gdsSttsNo; // 상품 상태 번호 (FK)
    private String optVlNo; // 옵션 값 번호 (FK)
    private String creatrNo; // 생성자 번호
    private String delUserNo; // 삭제자 번호
    private LocalDateTime crtYmd; // 생성일 (년월일)
    private LocalDateTime inactvtnDt; // 비활성화일시
    private boolean actvtnYn; // 활성화 여부
}