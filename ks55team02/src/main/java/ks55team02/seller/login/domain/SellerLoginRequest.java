package ks55team02.seller.login.domain;

import lombok.Data;

@Data
public class SellerLoginRequest {

	private String sellerId; // 판매자 로그인 폼의 name="sellerId"
    private String sellerPw; // 판매자 로그인 폼의 name="sellerPw"
}
