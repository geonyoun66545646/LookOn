package ks55team02.seller.inquiry.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.common.domain.inquiry.Inquiry;

@Mapper
public interface SellerInquiryMapper {
	
	List<Inquiry> getSellerInquiryList(@Param("currentStoreId") String StoreId);

}
