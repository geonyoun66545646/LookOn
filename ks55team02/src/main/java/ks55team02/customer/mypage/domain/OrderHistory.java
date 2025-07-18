package ks55team02.customer.mypage.domain;

import java.time.LocalDate;

import lombok.Data;

@Data
public class OrderHistory {

	private LocalDate orderDate;    // 주문일
    private String orderNumber;     // 주문번호
    private String productName;     // 상품명
    private int quantity;           // 수량
    private int price;              // 상품금액
}
