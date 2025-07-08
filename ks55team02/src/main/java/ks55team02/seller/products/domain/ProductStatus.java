// ProductStatus.java
package ks55team02.seller.products.domain;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List; // ⭐ List 임포트

@Data
public class ProductStatus {
    private String gdsSttsNo;
    private String gdsNo;
    private String creatrNo;
    private String mdfrNo;
    private String delUserNo;
    private int selPsbltyQntty;
    private LocalDateTime regDt;
    private LocalDateTime mdfcnDt;
    private LocalDateTime inactvtnDt;
    private boolean sldoutYn;
    private boolean actvtnYn;

    // ⭐ 추가: 이 재고에 매핑된 옵션 값 목록
    private List<ProductOptionValue> mappedOptionValues;
}