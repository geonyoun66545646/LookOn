package ks55team02.admin.adminpage.useradmin.coupons.mapper;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.common.domain.SearchCriteria; // import 추가
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map; // import 추가

@Mapper
public interface AdminCouponsMapper {
	// [수정] 파라미터를 SearchCriteria로 변경
    List<AdminCoupons> getCouponsList(SearchCriteria searchCriteria);

    // [수정] 파라미터를 SearchCriteria로 변경
    int getCouponsCount(SearchCriteria searchCriteria);
    
 // [추가] 신규 쿠폰 등록
    int addCoupon(AdminCoupons adminCoupons);
    
 // [추가] 가장 마지막에 등록된 쿠폰 ID를 조회
    String getLastCouponId();
    
    // [추가] 쿠폰 정보 수정
    int updateCoupon(AdminCoupons adminCoupons);
    
 // [추가] ID로 특정 쿠폰 정보 조회
    AdminCoupons getCouponById(String pblcnCpnId);
    
    // [추가] 쿠폰 삭제 (비활성화 처리)
    int deleteCoupon(String pblcnCpnId);
    
    
    
    
    
}