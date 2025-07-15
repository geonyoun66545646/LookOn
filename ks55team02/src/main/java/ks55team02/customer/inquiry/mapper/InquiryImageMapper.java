package ks55team02.customer.inquiry.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.common.domain.inquiry.InquiryImage;

@Mapper
public interface InquiryImageMapper {

    /**
     * 새로운 문의 이미지 매핑 정보를 데이터베이스에 삽입합니다.
     * @param inquiryImage 삽입할 InquiryImage 객체
     * @return 삽입된 행의 수
     */
    int addInquiryImage(InquiryImage inquiryImage); // insert -> add 변경

    /**
     * 여러 문의 이미지 매핑 정보를 데이터베이스에 삽입합니다.
     * @param inquiryImageList 삽입할 InquiryImage 객체 리스트
     * @return 삽입된 행의 수
     */
    int addInquiryImages(List<InquiryImage> inquiryImageList); // insert -> add 변경

    /**
     * 문의 ID로 문의 이미지 매핑 리스트를 조회합니다.
     * @param inqryId 조회할 문의 ID
     * @return InquiryImage 객체 리스트
     */
    List<InquiryImage> getInquiryImagesByInquiryId(String inqryId);
}
