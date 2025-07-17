package ks55team02.admin.adminpage.useradmin.coupons.service;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCouponsSearch;

import java.util.List;
import java.util.Map;

public interface AdminCouponsService {

    Map<String, Object> getCouponsList(AdminCouponsSearch search);
    void addCoupon(AdminCoupons adminCoupons);
    void updateCoupon(AdminCoupons adminCoupons);
    AdminCoupons getCouponById(String pblcnCpnId);
    void deleteCoupon(String pblcnCpnId);
    
    // 일괄 삭제 메소드 선언
    void batchDeleteCoupons(List<String> pblcnCpnIdList);
    
    // [신규] 선택 활성화 메소드 시그니처 추가
    void batchActivateCoupons(List<String> pblcnCpnIdList);
    
}