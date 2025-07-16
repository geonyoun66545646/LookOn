package ks55team02.seller.login.domain;

import lombok.Data;

@Data
public class SellerInfo {

	private String userNo;
    private String userLgnId;
    private String userPswd; // 암호화된 비밀번호
    private String userNm;
    private String mbrGrdCd;
}
