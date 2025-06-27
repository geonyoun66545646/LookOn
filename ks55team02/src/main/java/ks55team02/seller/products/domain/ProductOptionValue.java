package ks55team02.seller.products.domain; // 실제 패키지 경로에 맞게 수정

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProductOptionValue {
    private String optVlNo; // 옵션 값 번호 (PK)
    private String optNo; // 옵션 번호 (FK)
    private String creatrNo; // 생성자 번호
    private String mdfrNo; // 수정자 번호
    private String delUserNo; // 삭제자 번호
    private String vlNm; // 값 이름 (예: 빨강, M, 남성)
    private String optVlImgUrlAddr; // 옵션 값 이미지 URL 주소 (예: 색상 스와치 이미지)
    private String clrCd; // 색상 코드 (예: #FF0000)
    private LocalDateTime regDt; // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시
    private LocalDateTime inactvtnDt; // 비활성화일시
    private boolean actvtnYn; // 활성화 여부
}