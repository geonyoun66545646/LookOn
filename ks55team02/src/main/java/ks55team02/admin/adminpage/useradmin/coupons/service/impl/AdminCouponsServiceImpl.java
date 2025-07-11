package ks55team02.admin.adminpage.useradmin.coupons.service.impl;

import ks55team02.admin.adminpage.useradmin.coupons.domain.AdminCoupons;
import ks55team02.admin.adminpage.useradmin.coupons.mapper.AdminCouponsMapper;
import ks55team02.admin.adminpage.useradmin.coupons.service.AdminCouponsService;
import ks55team02.admin.common.domain.Pagination;
import ks55team02.admin.common.domain.SearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminCouponsServiceImpl implements AdminCouponsService {

    private final AdminCouponsMapper adminCouponsMapper;

    @Override
    public List<AdminCoupons> getCouponsList() {
        // Mapper를 통해 모든 쿠폰 데이터 조회
        return adminCouponsMapper.getCouponsList();
    }
}