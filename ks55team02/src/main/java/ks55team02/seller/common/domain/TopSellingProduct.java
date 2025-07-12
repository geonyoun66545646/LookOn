package ks55team02.seller.common.domain;

import lombok.Data;

@Data
public class TopSellingProduct {
    private String gdsNo;           // 상품 번호 (추가된 필드)
    private Goods ranking;          // Goods 객체 필드
    private Long totalSoldQuantity; // 총 판매 수량
}