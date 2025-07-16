package ks55team02.seller.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.seller.login.domain.SellerInfo;

@Mapper
public interface SellerLoginMapper {

	SellerInfo getSellerInfoById(String sellerId);
}
