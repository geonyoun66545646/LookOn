package ks55team02.customer.inquiry.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// import org.springframework.beans.factory.annotation.Autowired; // 이 import는 이제 필요 없습니다.
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.inquiry.domain.Inquiry;
import ks55team02.customer.inquiry.domain.InquiryImage; // InquiryImage 도메인 import 추가
import ks55team02.customer.inquiry.mapper.InquiryImageMapper;
import ks55team02.customer.inquiry.mapper.InquiryMapper;
import ks55team02.customer.inquiry.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;


@Service
@Transactional
@RequiredArgsConstructor // 생성자 주입은 Lombok이 처리합니다.
@Log4j2
public class InquiryServiceImpl implements InquiryService {

	private final InquiryMapper inquiryMapper;
	private final InquiryImageMapper inquiryImageMapper; // 이 매퍼가 정확히 주입되고 있는지 확인


	// 질문 목록
	@Override
	public List<Inquiry> getInquiryList() {
		List<Inquiry> inquiryList = inquiryMapper.getInquiryList();
		return inquiryList;
	}

	// 질문 세부
	@Override
	public Inquiry getInquiryById(String inquiryId) {
		// 1. 기본 문의 정보 조회
		Inquiry inquiry = inquiryMapper.getInquiryById(inquiryId);

		// 2. 문의 정보가 있을 경우에만 이미지 목록 조회하여 설정
		if (inquiry != null) {
			List<InquiryImage> inquiryImages = inquiryImageMapper.getInquiryImagesByInquiryId(inquiryId);
			inquiry.setInquiryImages(inquiryImages); // Inquiry 객체에 이미지 목록 설정
		}
		return inquiry;
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
	
	@Override
	public Map<String, Object> getInquiryList(int currentPage, int pageSize) {
		// 1. 전체 문의 개수 조회
				int totalRows = inquiryMapper.getTotalInquiryCount();
				log.info("총 문의 개수: {}", totalRows);

				// 2. 시작 인덱스 계산
				int startRow = (currentPage - 1) * pageSize;
				if (startRow < 0) { // 혹시 모를 음수 방지
				    startRow = 0;
				}
				log.info("시작 행 (startRow): {}", startRow);

				// 3. 페이징된 문의 목록 조회
				List<Inquiry> inquiryList = inquiryMapper.getInquiryListPaging(startRow, pageSize);
				log.info("조회된 문의 목록 항목 수: {}", inquiryList.size());


				// 4. 반환할 데이터 Map 생성
				Map<String, Object> resultMap = new HashMap<>();
				resultMap.put("inquiryList", inquiryList);
				resultMap.put("totalRows", totalRows);
				resultMap.put("currentPage", currentPage);
				resultMap.put("pageSize", pageSize);

				return resultMap;
			}
	}