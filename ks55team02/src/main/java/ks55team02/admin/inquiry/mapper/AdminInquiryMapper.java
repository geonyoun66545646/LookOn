package ks55team02.admin.inquiry.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory;

@Mapper
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

    // 답변 ID의 숫자 부분 중 최댓값을 조회하는 메서드
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

    // 문의 처리 상태 조회
    String getInquiryProcessStatus(String inqryId);

    // 문의 답변 이력 기록
    int insertInquiryAnswerHistory(InquiryAnswerHistory history);

    // 문의 답변 이력 ID의 최대값 조회
    Integer getMaxAnsHstryIdNumber();
    
    void updateInquiryStatus(Map<String, Object> params);

}