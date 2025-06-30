package ks55team02.customer.inquiry.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.inquiry.domain.Inquiry;
import ks55team02.customer.inquiry.mapper.InquiryMapper;
import ks55team02.customer.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class InquiryServiceImpl implements InquiryService {

	private final InquiryMapper inquiryMapper;

	// 질문 목록
	@Override
	public List<Inquiry> getInquiryList() {
		List<Inquiry> inquiryList = inquiryMapper.getInquiryList();
		return inquiryList;
	}
	// 질문 세부
	@Override
	public Inquiry getInquiryById(String inquiryId) {
		// TODO Auto-generated method stub
		return inquiryMapper.getInquiryById(inquiryId);
	}

	// 질문 등록
	@Override
	public void addInquiry(Inquiry inquiry) {
		String maxInquiryId = inquiryMapper.getMaxInquiryId();
		int nextIdNum;

		if (maxInquiryId == null || maxInquiryId.isEmpty()) {
			nextIdNum = 1;
		} else {
			nextIdNum = Integer.parseInt(maxInquiryId) + 1;
		}

		String newInquiryId = String.format("inq_%d", nextIdNum);
		inquiry.setInqryId(newInquiryId);

		log.info("새로 생성된 문의 ID: {}", newInquiryId);

		if (inquiry.getWrtrId() == null || inquiry.getWrtrId().isEmpty()) {
			inquiry.setWrtrId("user_no_1000"); // 임시 테스트용 ID
		}

		inquiry.setPrcsStts("접수");

		inquiry.setDelActvtnYn(false); // 기본적으로 삭제 비활성화

		int result = inquiryMapper.addInquiry(inquiry);

		if(result > 0) {
			log.info("문의 등록 성공: 문의 ID = {}, 제목 = {}", inquiry.getInqryId(), inquiry.getInqryTtl());
		} else {
			log.warn("문의 등록 실패 (영향 받은 행 없음): 문의 ID = {}, 제목 = {}", inquiry.getInqryId(), inquiry.getInqryTtl());
			throw new RuntimeException("문의 등록 중 알 수 없는 오류가 발생했습니다.");
		}
	}
}