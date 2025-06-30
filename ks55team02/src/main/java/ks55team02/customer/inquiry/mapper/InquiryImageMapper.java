package ks55team02.customer.inquiry.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import ks55team02.customer.inquiry.domain.InquiryImage;

@Mapper
public interface InquiryImageMapper {
    // 문의-이미지 매핑 정보 저장
    int insertInquiryImage(InquiryImage inquiryImage);
    
    // 특정 문의에 연결된 이미지 매핑 정보 조회
    List<InquiryImage> getInquiryImagesByInquiryId(String inqryId);
    
}