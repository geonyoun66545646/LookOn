package ks55team02.admin.inquiry.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // @Transactional 임포트

import ks55team02.admin.inquiry.mapper.AdminInquiryMapper; // AdminInquiryMapper 임포트
import ks55team02.admin.inquiry.service.AdminInquiryService; // AdminInquiryService 임포트
import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class AdminInquiryServiceImpl implements AdminInquiryService {

    private final AdminInquiryMapper adminInquiryMapper; // AdminInquiryMapper 주입
    // private final AnswerMapper answerMapper; // Answer 관련 기능을 AnswerMapper에서 가져올 경우 주입

    // 관리자 전체 문의 개수 조회
    @Override
    public int getAdminInquiryCnt(Inquiry inquiry) {
        log.info("서비스: getAdminInquiryCnt 호출 - 검색 조건: {}", inquiry);
        return adminInquiryMapper.getAdminInquiryCnt(inquiry);
    }

    // 관리자 전체 문의 목록 조회
    @Override
    public List<Inquiry> getAdminInquiryList(Inquiry inquiry, int limitStart, int pageSize) {
        log.info("서비스: getAdminInquiryList 호출 - 페이지네이션/검색 조건: {}, limitStart: {}, pageSize: {}", inquiry, limitStart, pageSize);
        return adminInquiryMapper.getAdminInquiryList(inquiry, limitStart, pageSize);
    }

    // 문의 상세 조회 (Admin 용도)
    @Override
    public Inquiry getInquiryDetailById(String inqryId) {
        log.info("서비스: getInquiryDetailById 호출 - 문의 ID: {}", inqryId);
        return adminInquiryMapper.getInquiryById(inqryId);
    }

    // 답변 등록
    @Override
    public Answer registerAnswer(String inqryId, String ansCn, String answrUserNo) {
        log.info("서비스: 답변 등록 시도 - 문의 ID: {}, 답변 내용: {}", inqryId, ansCn);

        // 1. 새로운 ans_id 생성
        Integer maxAnsIdNumber = adminInquiryMapper.getMaxAnsIdNumber(); // AdminInquiryMapper에 정의된 getMaxAnsIdNumber 사용
        String newAnsId = "ans_" + (maxAnsIdNumber != null ? maxAnsIdNumber + 1 : 1);

        // 2. Answer 객체 생성 및 데이터 설정
        Answer newAnswer = Answer.builder()
                .ansId(newAnsId)
                .inqryId(inqryId)
                .answrUserNo(answrUserNo) // Admin 계정 ID
                .ansCn(ansCn)
                .answrTypeCd("AD_ANS") // 관리자 답변 코드 (필요 시 정의)
                .rlsYn(true) // 기본적으로 공개
                .actvtnYn(true) // 기본적으로 활성화
                .ansTm(LocalDateTime.now()) // 현재 시간
                .lastMdfcnDt(LocalDateTime.now()) // 현재 시간
                .build();

        // 3. 답변 저장
        int result = adminInquiryMapper.insertAnswer(newAnswer); // AdminInquiryMapper에 정의된 insertAnswer 사용

        if (result > 0) {
            // 4. 문의 처리 상태 '완료'로 업데이트
            updateInquiryProcessStatus(inqryId, "완료"); // 현재 문의 상태를 '완료'로 변경
            
            // 5. 답변 이력 기록
            // InquiryAnswerHistory history = new InquiryAnswerHistory(...);
            // insertInquiryAnswerHistory(history); // 이력 기록 로직 필요 시 추가
            
            return newAnswer;
        } else {
            log.error("서비스: 답변 등록 실패 - 문의 ID: {}", inqryId);
            return null;
        }
    }

    // 답변 수정
    @Override
    public Answer updateAnswer(String ansId, String inqryId, String ansCn, String answrUserNo) {
        log.info("서비스: 답변 수정 시도 - 답변 ID: {}, 문의 ID: {}, 수정 내용: {}", ansId, inqryId, ansCn);

        // 1. 기존 답변 조회 (업데이트 후 최신 정보 반환을 위해 필요)
        Answer existingAnswer = adminInquiryMapper.getAnswerById(ansId); // AdminInquiryMapper에 정의된 getAnswerById 사용
        if (existingAnswer == null) {
            log.error("서비스: 수정할 답변을 찾을 수 없음 - 답변 ID: {}", ansId);
            return null;
        }

        // 2. Answer 객체 업데이트
        existingAnswer.setAnsCn(ansCn);
        existingAnswer.setAnswrUserNo(answrUserNo); // 수정하는 관리자 ID
        existingAnswer.setLastMdfcnDt(LocalDateTime.now());

        // 3. 답변 업데이트 저장
        int result = adminInquiryMapper.updateAnswer(existingAnswer); // AdminInquiryMapper에 정의된 updateAnswer 사용

        if (result > 0) {
            // 4. 문의 처리 상태 '완료'로 업데이트 (이미 완료 상태여도 다시 업데이트)
            updateInquiryProcessStatus(inqryId, "완료");
            
            // 5. 답변 이력 기록 (수정 이력)
            // InquiryAnswerHistory history = new InquiryAnswerHistory(...);
            // insertInquiryAnswerHistory(history); // 이력 기록 로직 필요 시 추가

            return existingAnswer;
        } else {
            log.error("서비스: 답변 수정 실패 - 답변 ID: {}", ansId);
            return null;
        }
    }

    // 문의 처리 상태 업데이트
    @Override
    public void updateInquiryProcessStatus(String inqryId, String prcsStts) {
        log.info("서비스: 문의 처리 상태 업데이트 - 문의 ID: {}, 상태: {}", inqryId, prcsStts);
        adminInquiryMapper.updateInquiryProcessStatus(inqryId, prcsStts);
        // 이력 기록 로직 (필요 시)
        // String oldStatus = adminInquiryMapper.getInquiryProcessStatus(inqryId);
        // InquiryAnswerHistory history = new InquiryAnswerHistory(..., oldStatus, prcsStts, "처리상태 변경");
        // insertInquiryAnswerHistory(history);
    }
    
    // 문의 답변 이력 기록
    @Override
    public void insertInquiryAnswerHistory(InquiryAnswerHistory history) {
        adminInquiryMapper.insertInquiryAnswerHistory(history);
    }
}