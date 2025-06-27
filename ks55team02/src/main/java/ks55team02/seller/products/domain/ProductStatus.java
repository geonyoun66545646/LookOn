package ks55team02.seller.products.domain; // 실제 패키지 경로에 맞게 수정

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductStatus {
    private String gdsSttsNo; // 상품 상태 번호 (PK)
    private String gdsNo; // 상품 번호 (FK)
    private String creatrNo; // 생성자 번호
    private String mdfrNo; // 수정자 번호
    private String delUserNo; // 삭제자 번호
    private int selPsbltyQntty; // 판매가능 수량
    private LocalDateTime regDt; // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시
    private LocalDateTime inactvtnDt; // 비활성화일시
    private boolean sldoutYn; // 품절 여부 (0:아님, 1:품절)
    private boolean actvtnYn; // 활성화 여부
}