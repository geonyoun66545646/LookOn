package ks55team02.seller.inquiry.service.impl;

import java.time.LocalDateTime;
import java.util.List;
// import java.util.UUID; // UUID는 더 이상 사용하지 않으므로 제거 또는 주석 처리

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
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
		return sellerInquiryMapper.getSellerInquiryList(inquiry,limitStart,pageSize);
	}
	
	@Override
	public Inquiry getSellerInquiryByStoreId(String inqryId) {
		return sellerInquiryMapper.getSellerInquiryByStoreId(inqryId);
	}

	@Override
	@Transactional
	public Answer registerAnswer(String inqryId, String ansCn, String answrUserNo) {
		// 현재 ans_ID 중 최대 숫자 부분을 가져옵니다.
		Integer maxAnsNum = answerMapper.getMaxAnsIdNumber();
		int nextAnsNum = (maxAnsNum != null) ? maxAnsNum + 1 : 1; // 최대값이 없으면 1부터 시작
		String ansId = "ans_" + nextAnsNum; // 새로운 ansId 생성

		Answer answer = new Answer();
		answer.setAnsId(ansId);
		answer.setInqryId(inqryId);
		answer.setAnswrUserNo(answrUserNo);
		answer.setAnsCn(ansCn);
		answer.setAnsTm(LocalDateTime.now());
		answer.setAnswrTypeCd("SELLER");
		answer.setRlsYn(true);
		answer.setActvtnYn(true);

		log.info("서비스: 답변 등록 시도 - {}", answer);
		int affectedRows = answerMapper.insertAnswer(answer);
		if (affectedRows > 0) {
			log.info("서비스: 답변 등록 성공 - 삽입된 행 수: {}, 답변 ID: {}", affectedRows, ansId);
			// 문의 처리 상태를 '완료'로 업데이트
			sellerInquiryMapper.updateInquiryProcessStatus(inqryId, "완료");
			log.info("서비스: 문의 처리 상태 '완료'로 업데이트 성공 - 문의 ID: {}", inqryId);
			return answerMapper.getAnswerById(ansId);
		} else {
			log.error("서비스: 답변 등록 실패 - 삽입된 행 수: 0, 답변 객체: {}", answer);
			throw new RuntimeException("답변 등록에 실패했습니다. 데이터베이스에 삽입되지 않았습니다.");
		}
	}

	@Override
	@Transactional
	public Answer updateAnswer(String ansId, String inqryId, String ansCn, String answrUserNo) {
		Answer answer = new Answer();
		answer.setAnsId(ansId);
		answer.setInqryId(inqryId);
		answer.setAnsCn(ansCn);
		answer.setAnswrUserNo(answrUserNo);
		answer.setLastMdfcnDt(LocalDateTime.now());

		log.info("서비스: 답변 수정 시도 - {}", answer);
		int affectedRows = answerMapper.updateAnswer(answer);
		if (affectedRows > 0) {
			log.info("서비스: 답변 수정 성공 - 업데이트된 행 수: {}, 답변 ID: {}", affectedRows, ansId);
			// 문의 처리 상태를 '완료'로 업데이트 (수정 시에도 완료 상태 유지 또는 다시 완료로 변경)
			sellerInquiryMapper.updateInquiryProcessStatus(inqryId, "완료");
			log.info("서비스: 문의 처리 상태 '완료'로 업데이트 성공 - 문의 ID: {}", inqryId);
			return answerMapper.getAnswerById(ansId);
		} else {
			log.error("서비스: 답변 수정 실패 - 업데이트된 행 수: 0, 답변 객체: {}", answer);
			throw new RuntimeException("답변 수정에 실패했습니다. 대상 답변이 없거나 업데이트되지 않았습니다.");
		}
	}

	@Override
	public String getSellerUserNoByStoreId(String storeId) {
		log.info("서비스: getSellerUserNoByStoreId 호출 - storeId: {}", storeId);
		return sellerInquiryMapper.getSellerUserNoByStoreId(storeId);
	}
	
	// 문의 처리 상태를 업데이트하는 메서드 구현 (SellerInquiryService 인터페이스에서 추가된 메서드)
	@Override
	public void updateInquiryProcessStatus(String inqryId, String prcsStts) {
	    sellerInquiryMapper.updateInquiryProcessStatus(inqryId, prcsStts);
	}
}