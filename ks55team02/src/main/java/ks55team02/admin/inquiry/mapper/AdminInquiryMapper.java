package ks55team02.admin.inquiry.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param; // @Param 임포트

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory;

@Mapper // MyBatis 매퍼임을 나타냄
public interface AdminInquiryMapper {

    // 전체 문의 개수 조회
    int getAdminInquiryCnt(Inquiry inquiry);

    // 전체 문의 목록 조회 (페이지네이션 및 검색/필터링 조건 포함)
    List<Inquiry> getAdminInquiryList(
        @Param("inquiry") Inquiry inquiry,
        @Param("limitStart") int limitStart,
        @Param("pageSize") int pageSize
    );

    // 문의 상세 조회 (ID 기준)
    Inquiry getInquiryById(String inqryId);

    // 답변 ID의 최대값 조회
    Integer getMaxAnsIdNumber();

    // 새로운 답변 등록
    int insertAnswer(Answer answer);

    // 답변 ID로 답변 조회
    Answer getAnswerById(String ansId);

    // 답변 수정
    int updateAnswer(Answer answer);

    // 문의 처리 상태 업데이트
    int updateInquiryProcessStatus(
        @Param("inqryId") String inqryId,
        @Param("prcsStts") String prcsStts
    );
    
    // 문의 답변 이력 기록
    int insertInquiryAnswerHistory(InquiryAnswerHistory history);

    // 추가적으로 문의 상태를 조회하는 메서드가 필요할 수도 있음 (serviceImpl 주석 참고)
    // String getInquiryProcessStatus(String inqryId);
}