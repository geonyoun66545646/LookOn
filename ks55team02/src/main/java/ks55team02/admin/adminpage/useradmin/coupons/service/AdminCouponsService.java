package ks55team02.admin.adminpage.useradmin.coupons.service;

import java.util.List;
import java.util.Map;
import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.common.domain.SearchCriteria;

public interface AdminCouponsService {

    // 쿠폰 목록 조회
    List<AdminCoupons> getCouponsList();

    
    
}