package ks55team02.admin.inquiry.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.inquiry.mapper.AdminInquiryMapper;
import ks55team02.admin.inquiry.service.AdminInquiryService;
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
        log.info("서비스: 신규 답변 등록 요청 - inqryId: {}, ansCn: {}, answrUserNo: {}", inqryId, ansCn, answrUserNo);

        // 1. 새로운 답변 ID 생성
        Integer maxAnsIdNum = adminInquiryMapper.getMaxAnsIdNumber();
        int nextAnsIdNum = (maxAnsIdNum != null) ? maxAnsIdNum + 1 : 1;
        String ansId = "ans_" + nextAnsIdNum;

        // 2. Answer 객체 생성 및 데이터 설정
        Answer answer = new Answer();
        answer.setAnsId(ansId);
        answer.setInqryId(inqryId);
        answer.setAnswrUserNo(answrUserNo); // 답변 작성자 (닉네임 또는 ID)
        answer.setAnsCn(ansCn);
        answer.setAnsTm(LocalDateTime.now()); // 답변 시간
        answer.setAnswrTypeCd("관리자 답변"); // 기본값 설정 (필요에 따라 변경)
        answer.setRlsYn(true); // 공개 여부 (기본값 설정)
        answer.setActvtnYn(true); // 활성화 여부 (기본값 설정)
        // lastMdfcnDt는 처음 등록 시에는 null이거나 ansTm과 동일하게 설정될 수 있음
        answer.setLastMdfcnDt(null); // 초기 등록이므로 수정 시간은 null 또는 ansTm

        // 3. 답변 저장
        int result = adminInquiryMapper.insertAnswer(answer);

        if (result > 0) {
            log.info("서비스: 답변 등록 성공 - 답변 ID: {}", ansId);
            // 4. 문의 처리 상태 '완료'로 업데이트
            updateInquiryProcessStatus(inqryId, "완료");

            // 5. 답변 이력 기록 (등록 이력)
            // InquiryAnswerHistory history = new InquiryAnswerHistory(...);
            // insertInquiryAnswerHistory(history); // 이력 기록 로직 필요 시 추가

            return answer;
        } else {
            log.error("서비스: 답변 등록 실패 - 문의 ID: {}", inqryId);
            return null;
        }
    }

    @Override
    public Answer updateAnswer(String ansId, String inqryId, String ansCn, String answrUserNo) {
        log.info("서비스: 답변 수정 요청 - ansId: {}, inqryId: {}, ansCn: {}, answrUserNo (로그인된 관리자 닉네임/ID): {}", ansId, inqryId, ansCn, answrUserNo);

        // 1. 기존 답변 정보 조회
        Answer existingAnswer = adminInquiryMapper.getAnswerById(ansId);
        if (existingAnswer == null) {
            log.warn("서비스: 수정할 답변을 찾을 수 없습니다 - 답변 ID: {}", ansId);
            return null;
        }

        // 2. 답변 내용 및 수정 시간 업데이트
        existingAnswer.setAnsCn(ansCn);
        // 기존 작성자 (answrUserNo)는 유지하고, 마지막 수정자 정보만 업데이트
        // 이 로직이 작동하려면 Answer 도메인에 lastMdfcnUserNo 또는 lastMdfcnUserNcnm 필드가 추가되어야 합니다.
        // 예를 들어: existingAnswer.setLastMdfcnUserNo(answrUserNo);
        // 만약 기존 answrUserNo 필드를 원작성자로 간주하고 변경하지 않으려면 아래 라인을 제거하거나 주석 처리합니다.
        // existingAnswer.setAnswrUserNo(answrUserNo); // 이 라인을 제거하여 원작성자를 유지합니다.
        existingAnswer.setLastMdfcnDt(LocalDateTime.now()); // 마지막 수정 일시

        // 3. 답변 업데이트 저장
        int result = adminInquiryMapper.updateAnswer(existingAnswer);

        if (result > 0) {
            // 4. 문의 처리 상태 '완료'로 업데이트 (이미 완료 상태여도 다시 업데이트)
            updateInquiryProcessStatus(inqryId, "완료");
            
            // 5. 답변 이력 기록 (수정 이력)
            // InquiryAnswerHistory history = new InquiryAnswerHistory(...);
            // insertInquiryAnswerHistory(history); // 이력 기록 로직 필요 시 추가

            // 업데이트된 정보를 다시 조회하여 반환 (필요한 경우)
            // 또는 existingAnswer 객체를 그대로 반환해도 무방합니다.
            return adminInquiryMapper.getAnswerById(ansId); // 업데이트된 최신 정보를 가져와 반환
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
    }

	@Override
	public void insertInquiryAnswerHistory(InquiryAnswerHistory history) {
		// TODO Auto-generated method stub
		
	}
}