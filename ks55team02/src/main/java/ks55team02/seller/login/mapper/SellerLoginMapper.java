package ks55team02.seller.login.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.seller.login.domain.SellerInfo;

@Mapper
public interface SellerLoginMapper {

	/**
	 * 주어진 판매자 아이디(sellerId)를 사용하여 판매자 정보를 조회합니다.
	 *
	 * @param sellerId 조회할 판매자의 로그인 아이디
	 * @return         조회된 판매자 정보를 담은 {@link SellerInfo} 객체.
	 * 해당 sellerId에 해당하는 판매자가 없을 경우 null을 반환합니다.
	 */
	SellerInfo getSellerInfoById(String sellerId);
}
