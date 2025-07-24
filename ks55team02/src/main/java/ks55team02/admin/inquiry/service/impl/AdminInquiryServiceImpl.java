package ks55team02.admin.inquiry.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.inquiry.mapper.AdminInquiryMapper;
import ks55team02.admin.inquiry.service.AdminInquiryService;
import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AdminInquiryServiceImpl implements AdminInquiryService {

    private final AdminInquiryMapper adminInquiryMapper;

    @Override
    public int getAdminInquiryCnt(Inquiry inquiry) {
        log.info("서비스: getAdminInquiryCnt 호출 - 검색 조건: {}", inquiry);
        return adminInquiryMapper.getAdminInquiryCnt(inquiry);
    }

    @Override
    public List<Inquiry> getAdminInquiryList(Inquiry inquiry, int limitStart, int pageSize) {
        log.info("서비스: getAdminInquiryList 호출 - 검색 조건: {}, limitStart: {}, pageSize: {}", inquiry, limitStart, pageSize);
        return adminInquiryMapper.getAdminInquiryList(inquiry, limitStart, pageSize);
    }

    @Override
    public Inquiry getInquiryDetailById(String inqryId) {
        log.info("서비스: 문의 상세 조회 요청 - 문의 ID: {}", inqryId);
        return adminInquiryMapper.getInquiryById(inqryId);
    }

    @Override
    public Answer registerAnswer(String inqryId, String ansCn, String answrUserNo) {
        log.info("서비스: 신규 답변 등록 요청 - inqryId: {}, answrUserNo: {}", inqryId, answrUserNo);
        Integer maxAnsIdNum = adminInquiryMapper.getMaxAnsIdNumber();
        int nextAnsIdNum = (maxAnsIdNum != null) ? maxAnsIdNum + 1 : 1;
        String ansId = "ans_" + nextAnsIdNum;

        Answer answer = new Answer();
        answer.setAnsId(ansId);
        answer.setInqryId(inqryId);
        answer.setAnswrUserNo(answrUserNo);
        answer.setAnsCn(ansCn);
        answer.setAnsTm(LocalDateTime.now());
        answer.setAnswrTypeCd("관리자 답변");
        answer.setRlsYn(true);
        answer.setActvtnYn(true);

        int result = adminInquiryMapper.insertAnswer(answer);
        if (result > 0) {
            log.info("서비스: 답변 등록 성공 - 답변 ID: {}", ansId);
            // 답변 등록 시, 상태를 '완료(COMPLETED)'로 자동 변경
            updateInquiryStatus(List.of(inqryId), "COMPLETED", answrUserNo);
            return answer;
        }
        return null;
    }
    
    // [참고] updateAnswer 메소드가 없어서 추가합니다. 만약 이미 있다면 이 부분은 무시하세요.
    @Override
    public Answer updateAnswer(String ansId, String inqryId, String ansCn, String answrUserNo) {
        log.info("서비스: 답변 수정 요청 - ansId: {}, inqryId: {}, answrUserNo: {}", ansId, inqryId, answrUserNo);
        Answer existingAnswer = adminInquiryMapper.getAnswerById(ansId);
        if (existingAnswer == null) return null;
        
        existingAnswer.setAnsCn(ansCn);
        existingAnswer.setLastMdfcnDt(LocalDateTime.now());
        
        int result = adminInquiryMapper.updateAnswer(existingAnswer);
        if (result > 0) {
            // 답변 수정 시에도 상태를 '완료(COMPLETED)'로 변경
            updateInquiryStatus(List.of(inqryId), "COMPLETED", answrUserNo);
            return adminInquiryMapper.getAnswerById(ansId);
        }
        return null;
    }

    @Override
    public void insertInquiryAnswerHistory(InquiryAnswerHistory history) {
        // TODO: 구현 필요
    }

    // [핵심 수정] 중복된 메소드를 제거하고, 올바른 버전 하나만 남깁니다.
    @Override
    public void updateInquiryStatus(List<String> inquiryIds, String newStatus, String adminUserNo) {
        if (inquiryIds == null || inquiryIds.isEmpty() || newStatus == null || newStatus.isEmpty()) {
            log.warn("상태 변경 요청 파라미터가 유효하지 않습니다.");
            return;
        }
        log.info("서비스: 문의 상태 일괄 변경 요청 - IDs: {}, 변경 상태: {}, 처리자: {}", inquiryIds, newStatus, adminUserNo);

        // View에서 영문 코드(DELETED, COMPLETED 등)를 직접 보내므로, 한/영 변환 로직(switch)이 필요 없습니다.
        
        Map<String, Object> params = new HashMap<>();
        params.put("inquiryIds", inquiryIds);
        params.put("newStatus", newStatus); // 변환 없이 그대로 사용
        params.put("adminUserNo", adminUserNo);
        
        adminInquiryMapper.updateInquiryStatus(params); 
        log.info("서비스: 문의 상태 변경 DB 업데이트 완료");
    }
}