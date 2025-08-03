package ks55team02.seller.inquiry.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.inquiry.InquiryAnswerHistory; // 추가
import ks55team02.seller.inquiry.mapper.AnswerMapper;
import ks55team02.seller.inquiry.mapper.SellerInquiryMapper;
import ks55team02.seller.inquiry.service.SellerInquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class SellerInquiryServiceImpl implements SellerInquiryService{
	
	private final SellerInquiryMapper sellerInquiryMapper;
	private final AnswerMapper answerMapper;
	
	@Override
	public int getSellerInquiryCnt(Inquiry inquiry) {
		log.info("서비스: getSellerInquiryCnt 호출 - 검색 조건: {}", inquiry);
		return sellerInquiryMapper.getSellerInquiryCnt(inquiry);
	}
	@Override
	public List<Inquiry> getSellerInquiryList(Inquiry inquiry, int limitStart, int pageSize) {
		log.info("서비스: getSellerInquiryList 호출 - 페이지네이션/검색 조건: {}, limitStart: {}, pageSize: {}", inquiry, limitStart, pageSize);
		return sellerInquiryMapper.getSellerInquiryList(inquiry, limitStart, pageSize);
	}

	@Override
	public Inquiry getSellerInquiryByStoreId(String inqryId) {
		log.info("서비스: getSellerInquiryByStoreId 호출 - 문의 ID: {}", inqryId);
		return sellerInquiryMapper.getSellerInquiryByStoreId(inqryId);
	}

	@Override
	public String getSellerUserNoByStoreId(String storeId) {
		log.info("서비스: getSellerUserNoByStoreId 호출 - 상점 ID: {}", storeId);
		return sellerInquiryMapper.getSellerUserNoByStoreId(storeId);
	}
	
	@Override
	@Transactional
	public Answer registerAnswer(String inqryId, String ansCn, String answrUserNo) {
	    log.info("서비스: registerAnswer 호출 - 문의 ID: {}, 답변 내용: {}, 답변자 번호: {}", inqryId, ansCn, answrUserNo);

	    String prcsStts = sellerInquiryMapper.getInquiryProcessStatus(inqryId); // 현재 처리 상태 조회
	    log.info("서비스: 문의 ID {}의 현재 처리 상태: {}", inqryId, prcsStts);

	    Answer answer = new Answer();
	    // 답변 ID 생성 로직 (예: ans_1, ans_2...)
	    Integer maxAnsIdNum = answerMapper.getMaxAnsIdNumber();
	    int nextAnsIdNum = (maxAnsIdNum != null) ? maxAnsIdNum + 1 : 1;
	    String ansId = "ans_" + nextAnsIdNum;
	    answer.setAnsId(ansId);
	    answer.setInqryId(inqryId);
	    answer.setAnsCn(ansCn);
	    answer.setAnswrUserNo(answrUserNo);
	    answer.setAnsTm(LocalDateTime.now());
	    answer.setLastMdfcnDt(LocalDateTime.now());
	    answer.setRlsYn(true); // 기본적으로 공개
	    answer.setActvtnYn(true); // 기본적으로 활성화

	    int result = answerMapper.insertAnswer(answer);
	    if (result > 0) {
	        log.info("서비스: 답변 등록 성공 - 답변 ID: {}", ansId);

	        // 문의 처리 상태 '완료'로 업데이트
	        sellerInquiryMapper.updateInquiryProcessStatus(inqryId, "COMPLETED");
	        log.info("서비스: 문의 처리 상태 '완료'로 업데이트 성공 - 문의 ID: {}", inqryId);

	        // 문의 답변 이력 기록
	        Integer maxAnsHstryNum = sellerInquiryMapper.getMaxAnsHstryIdNumber();
	        int nextAnsHstryNum = (maxAnsHstryNum != null) ? maxAnsHstryNum + 1 : 1;
	        String ansHstryId = "ans_hstry_" + nextAnsHstryNum;

	        InquiryAnswerHistory history = new InquiryAnswerHistory();
	        history.setAnsHstryId(ansHstryId);
	        history.setAnswerId(ansId);
	        history.setInqryId(inqryId);
	        history.setPrcsUserNo(answrUserNo); // 처리 사용자 번호는 답변자 번호와 동일
	        history.setChgBfrStts(prcsStts); // 변경 전 상태
	        history.setChgAftrStts("COMPLETED"); // 변경 후 상태
	        history.setChgCn("새로운 답변 등록으로 인한 상태 변경"); // 변경 내용
	        // chgDt는 DB에서 NOW()로 자동 설정됩니다.

	        sellerInquiryMapper.insertInquiryAnswerHistory(history);
	        log.info("서비스: 문의 답변 이력 기록 성공 - 이력 ID: {}", ansHstryId);

	        return answer;
	    } else {
	        log.error("서비스: 답변 등록 실패 - 삽입된 행 수: 0");
	        throw new RuntimeException("답변 등록 실패.");
	    }
	}
	
	@Override
	@Transactional
	public Answer updateAnswer(String ansId, String inqryId, String ansCn, String answrUserNo) {
		log.info("서비스: updateAnswer 호출 - 답변 ID: {}, 문의 ID: {}, 답변 내용: {}, 답변자 번호: {}", ansId, inqryId, ansCn, answrUserNo);

		String chgBfrStts = sellerInquiryMapper.getInquiryProcessStatus(inqryId); // 변경 전 상태 조회
		log.info("서비스: 문의 ID {}의 변경 전 처리 상태: {}", inqryId, chgBfrStts);

		Answer answer = new Answer();
		answer.setAnsId(ansId);
		answer.setInqryId(inqryId);
		answer.setAnsCn(ansCn);
		answer.setAnswrUserNo(answrUserNo);
		answer.setLastMdfcnDt(LocalDateTime.now());

		int result = answerMapper.updateAnswer(answer);
		if (result > 0) {
			log.info("서비스: 답변 수정 성공 - 답변 ID: {}", ansId);

			// 문의 처리 상태 '완료'로 업데이트
			sellerInquiryMapper.updateInquiryProcessStatus(inqryId, "COMPLETED");
			log.info("서비스: 문의 처리 상태 '완료'로 업데이트 성공 - 문의 ID: {}", inqryId);

			// 문의 답변 이력 기록
			Integer maxAnsHstryNum = sellerInquiryMapper.getMaxAnsHstryIdNumber();
			int nextAnsHstryNum = (maxAnsHstryNum != null) ? maxAnsHstryNum + 1 : 1;
			String ansHstryId = "ans_hstry_" + nextAnsHstryNum;

			InquiryAnswerHistory history = new InquiryAnswerHistory();
			history.setAnsHstryId(ansHstryId);
			history.setAnswerId(ansId);
			history.setInqryId(inqryId);
			history.setPrcsUserNo(answrUserNo); // 처리 사용자 번호는 답변자 번호와 동일
			history.setChgBfrStts(chgBfrStts); // 변경 전 상태
			history.setChgAftrStts("COMPLETED"); // 변경 후 상태
			history.setChgCn("답변 수정으로 인한 상태 변경"); // 변경 내용
			// chgDt는 DB에서 NOW()로 자동 설정됩니다.

			sellerInquiryMapper.insertInquiryAnswerHistory(history);
			log.info("서비스: 문의 답변 이력 기록 성공 - 이력 ID: {}", ansHstryId);

			return answerMapper.getAnswerById(ansId);
		} else {
			log.error("서비스: 답변 수정 실패 - 업데이트된 행 수: 0. 답변 ID: {}", ansId);
			throw new RuntimeException("답변 수정 실패.");
		}
	}

	// ⭐ 추가: 로그인 ID로 상점 ID 조회 구현
	@Override
	public String getStoreIdByUserLgnId(String userLgnId) {
		log.info("서비스: getStoreIdByUserLgnId 호출 - 로그인 ID: {}", userLgnId);
		return sellerInquiryMapper.getStoreIdByUserLgnId(userLgnId);
	}
	
}