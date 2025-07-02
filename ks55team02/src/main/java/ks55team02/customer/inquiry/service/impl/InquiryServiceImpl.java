package ks55team02.customer.inquiry.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays; // Arrays.asList를 위해 import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.inquiry.domain.Inquiry;
import ks55team02.customer.inquiry.domain.InquiryImage;
import ks55team02.customer.inquiry.domain.InquiryOption;
import ks55team02.customer.inquiry.mapper.InquiryImageMapper;
import ks55team02.customer.inquiry.mapper.InquiryMapper;
import ks55team02.customer.inquiry.service.InquiryService;
import ks55team02.customer.store.domain.StoreImage;
import ks55team02.customer.store.mapper.StoreImageMapper;
import ks55team02.util.FileDetail;
import ks55team02.util.FilesUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class InquiryServiceImpl implements InquiryService {

	private final InquiryMapper inquiryMapper;
	private final InquiryImageMapper inquiryImageMapper;
	private final StoreImageMapper storeImageMapper;
	private final FilesUtils filesUtils;
	
	// 질문 목록 (페이징 없는 버전)
	@Override
	public List<Inquiry> getInquiryList() {
		List<Inquiry> inquiryList = inquiryMapper.getInquiryList();
		return inquiryList;
	}

	// 질문 세부
	@Override
	public Inquiry getInquiryById(String inquiryId) {
		// 1. 기본 문의 정보 조회 (이미지 정보 포함된 메서드 사용)
		Inquiry inquiry = inquiryMapper.getInquiryByIdWithImages(inquiryId);
		return inquiry;
	}

	// 질문 등록
	@Override
	public void addInquiry(Inquiry inquiry, MultipartFile[] attachedFiles) {
		// 1. 문의 ID 생성 및 기본값 설정
		String maxInquiryIdNum = inquiryMapper.getMaxInquiryId();
		int nextIdNum;

		if (maxInquiryIdNum == null || maxInquiryIdNum.isEmpty()) {
			nextIdNum = 1;
		} else {
			nextIdNum = Integer.parseInt(maxInquiryIdNum) + 1;
		}

		String newInquiryId = String.format("inq_%d", nextIdNum);
		inquiry.setInqryId(newInquiryId);

		log.info("새로 생성된 문의 ID: {}", newInquiryId);

		// 기본값 설정 (컨트롤러에서 설정되지 않은 경우)
		if (inquiry.getWrtrId() == null || inquiry.getWrtrId().isEmpty()) {
			inquiry.setWrtrId("user_no_1000"); // 임시 테스트용 ID (실제로는 로그인 사용자 ID 사용)
		}
		if (inquiry.getPrcsStts() == null || inquiry.getPrcsStts().isEmpty()) {
			inquiry.setPrcsStts("접수"); // 기본 처리 상태: 접수
		}
		if (inquiry.getRegYmd() == null) {
		    inquiry.setRegYmd(LocalDateTime.now(ZoneId.of("Asia/Seoul")));
		}
		// prvtYn은 HTML 폼에서 체크박스 미체크 시 false로 넘어오므로 별도 처리 필요 없음 (Boolean 타입)

		// 2. 문의 데이터 저장 (inquiries 테이블)
		int result = inquiryMapper.addInquiry(inquiry);

		if(result > 0) {
			log.info("문의 등록 성공: 문의 ID = {}, 제목 = {}", inquiry.getInqryId(), inquiry.getInqryTtl());
			
			// 3. 첨부 파일 처리 (store_images 및 inquiry_images 테이블)
			if (attachedFiles != null && attachedFiles.length > 0) {
				List<StoreImage> storeImagesToInsert = new ArrayList<>();
				List<InquiryImage> inquiryImagesToInsert = new ArrayList<>();
				int order = 0;

				// FilesUtils를 사용하여 파일 저장 및 FileDetail 리스트 얻기
				List<FileDetail> fileDetails = filesUtils.saveFiles(attachedFiles, "inquiry_images"); 

				for (FileDetail fileDetail : fileDetails) {
					// 3-1. StoreImage 객체 생성 및 리스트에 추가 (store_images 테이블용)
					String imgId = "img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER)
                                   + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
					StoreImage storeImage = StoreImage.builder()
							.imgId(imgId)
							.imgFileNm(fileDetail.getOriginalFileName())
							.imgAddr(fileDetail.getSavedPath())
							.imgFileSz(fileDetail.getFileSize())
							.imgTypeCd(fileDetail.getFileExtension())
							.regYmd(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
							.delYn(false)
							.build();
					storeImagesToInsert.add(storeImage);

					// 3-2. InquiryImage 객체 생성 및 리스트에 추가 (inquiry_images 테이블용)
					String inqryImgId = "inqry_img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER)
                                        + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
					InquiryImage inquiryImage = InquiryImage.builder()
							.inqryImgId(inqryImgId)
							.inqryId(newInquiryId)
							.imgId(imgId)
							.ord(order++)
							.build();
					inquiryImagesToInsert.add(inquiryImage);
				}

				// 3-3. StoreImage 데이터 저장 (배치 삽입)
				if (!storeImagesToInsert.isEmpty()) {
					storeImageMapper.addStoreImages(storeImagesToInsert); // addStoreImages (복수형) 사용
				}

				// 3-4. InquiryImage 데이터 저장 (배치 삽입)
				if (!inquiryImagesToInsert.isEmpty()) {
					inquiryImageMapper.addInquiryImages(inquiryImagesToInsert); // 수정된 부분: addInquiryImage -> addInquiryImages
				}
			}
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
		if (startRow < 0) {
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

    @Override
    public List<InquiryOption> getInquiryTypeOptions() {
        // InquiryOption enum의 모든 상수를 리스트로 반환합니다.
        return Arrays.asList(InquiryOption.values());
    }
}
