package ks55team02.admin.adminpage.useradmin.coupons.mapper;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.common.domain.SearchCriteria; // import 추가
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map; // import 추가

@Mapper
public interface AdminCouponsMapper {
    // 모든 쿠폰 데이터 조회
    List<AdminCoupons> getCouponsList();
}