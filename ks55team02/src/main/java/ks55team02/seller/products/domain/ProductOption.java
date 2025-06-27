package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // List를 사용하기 위해 임포트가 필요합니다.

@Data
public class ProductOption {
    private String optNo; // 옵션 번호 (PK)
    private String gdsNo; // 상품 번호 (FK)
    private String creatrNo; // 생성자 번호
    private String mdfrNo; // 수정자 번호
    private String delUserNo; // 삭제자 번호
    private String optNm; // 옵션명 (예: 색상, 사이즈, 성별)
    private String snglMtplChcSeCd; // 단일/복수 선택 구분 코드 (예: 'S' for Single, 'M' for Multiple)
    private int optIndctSn; // 옵션 표시 순서
    private LocalDateTime regDt; // 등록일시
    private LocalDateTime mdfcnDt; // 수정일시
    private LocalDateTime inactvtnDt; // 비활성화일시
    private boolean actvtnYn; // 활성화 여부

    // ⭐⭐⭐ 이 부분이 ProductOption.java에 있어야 합니다! ⭐⭐⭐
    private List<ProductOptionValue> optionValues; // 이 옵션(예: 색상)이 가질 수 있는 값들(예: 빨강, 파랑)의 리스트
}