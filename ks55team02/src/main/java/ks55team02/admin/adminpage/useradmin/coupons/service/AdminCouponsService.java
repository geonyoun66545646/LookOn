package ks55team02.admin.adminpage.useradmin.coupons.service;

import java.util.List;
import java.util.Map;
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.common.domain.SearchCriteria;

public interface AdminCouponsService {

    // 쿠폰 목록 조회
    List<AdminCoupons> getCouponsList();
    
 // [추가] 신규 쿠폰 등록
    void addCoupon(AdminCoupons adminCoupons);
    
 // [추가] 쿠폰 정보 수정
    void updateCoupon(AdminCoupons adminCoupons);
    
    /**
     * ======================================================
     * [추가] ID로 특정 쿠폰 정보 조회
     * ======================================================
     */
    AdminCoupons getCouponById(String pblcnCpnId);
    
    // [추가] 쿠폰 삭제
    void deleteCoupon(String pblcnCpnId);


    
    
}