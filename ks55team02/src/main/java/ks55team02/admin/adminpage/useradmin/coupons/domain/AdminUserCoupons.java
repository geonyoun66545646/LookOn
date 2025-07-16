package ks55team02.admin.adminpage.useradmin.coupons.domain;

import lombok.Data;

@Data
public class AdminUserCoupons {
    // user_coupons 테이블 정보
    private String userCpnId;
    private Boolean useYn; // 소문자 boolean -> 대문자 Boolean
    private String cpnGiveDt; // [수정] issueDt -> cpnGiveDt
    private String useDt;

    // users 테이블 정보 (JOIN)
    private String userNo;
    private String userLgnId; // [수정] userId -> userLgnId
    private String userNm;    // [수정] userName -> userNm

    // coupons 테이블 정보 (JOIN)
    private String pblcnCpnId;
    private String cpnNm;
    
 
}