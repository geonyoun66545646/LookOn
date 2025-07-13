package ks55team02.customer.inquiry.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays; // Arrays.asList를 위해 import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import ks55team02.customer.inquiry.domain.Inquiry;
import ks55team02.customer.inquiry.domain.InquiryImage;
import ks55team02.customer.inquiry.domain.InquiryOption;
import ks55team02.customer.inquiry.domain.InquiryTargetOption;
import ks55team02.customer.inquiry.mapper.InquiryImageMapper;
import ks55team02.customer.inquiry.mapper.InquiryMapper;
import ks55team02.customer.inquiry.service.InquiryService;
import ks55team02.customer.store.domain.Store;
import ks55team02.customer.store.domain.StoreImage;
import ks55team02.customer.store.mapper.CustomerStoreMapper;
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
   
    @Qualifier("customerStoreMapper")
    private final CustomerStoreMapper customerStoreMapper;

    // 질문 목록 (페이징 없는 버전)
    @Override
    public List<Inquiry> getInquiryList() {
        List<Inquiry> inquiryList = inquiryMapper.getInquiryList();
       
        return inquiryList;
    }
    

    // 질문 세부
    @Override
    public Inquiry getInquiryById(String inquiryId, String currentUserId) {
        // 1. 기본 문의 정보 조회 (이미지 정보 포함된 메서드 사용)
        Inquiry inquiry = inquiryMapper.getInquiryByIdWithImages(inquiryId);

        // 문의가 존재하지 않으면 null 반환
        if(inquiry == null) {
            log.warn("문의를 찾을 수 없습니다: {}", inquiryId); // 로그 메시지 포맷 수정
            return null;
        }
        // 비밀글 여부 확인 및 권한 체크
        if(inquiry.isPrvtYn()) {
            log.info("비밀글 입니다. ID:{}, 작성자 : {}. 현재 사용자 : {}", inquiryId, inquiry.getWrtrId(), currentUserId);
            // currentUserId가 널이거나 작성자 ID 또는 관리자 아이디와 일치하지 않으면 접근 불가
            if(currentUserId == null || !currentUserId.equals(inquiry.getWrtrId())) {
                //** 관리자 확인 로직 추가 할 부분 **
                log.warn("비밀글 열람 권한 없음: 문의 ID = {}, 시도 사용자 ID = {}", inquiryId, currentUserId);
                return null;
            }
            log.info("비밀글 열람 권한 확인됨: 문의 ID = {}, 사용자 ID = {}", inquiryId, currentUserId);
        } else {
            log.info("일반 문의입니다. ID: {}", inquiryId);
        }

        return inquiry;
    }

    // 질문 등록
    @Override
    public void addInquiry(Inquiry inquiry, MultipartFile[] attachedFiles, String currentUserId) {
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

        // 💡 **[임시 설정 해제 및 매개변수 사용]** wrtrId를 컨트롤러에서 전달받은 currentUserId로 설정합니다.
        // TODO: [로그인 기능 연동 시 교체 필수] currentUserId가 null이거나 비어있을 경우 예외를 발생시키거나 로그인 페이지로 리다이렉션하는 로직은
        // 컨트롤러에서 처리하는 것이 일반적입니다. 여기서는 이미 컨트롤러에서 처리되었음을 가정합니다.
        if(currentUserId == null || currentUserId.isEmpty()) { // 논리 OR 연산자 `||` 추가
            log.error("로그인된 사용자 ID가 없어 문의를 작성할 수 없습니다.");
            throw new IllegalArgumentException("로그인된 사용자 정보가 필요합니다.");
        }
        // 로그인된 사용자 ID 설정
        inquiry.setWrtrId(currentUserId);

        // 기본값 설정 (컨트롤러에서 설정되지 않은 경우)
        if (inquiry.getPrcsStts() == null || inquiry.getPrcsStts().isEmpty()) {
            inquiry.setPrcsStts("접수"); // 기본 처리 상태: 접수
        }
        if (inquiry.getRegYmd() == null) {
            inquiry.setRegYmd(LocalDateTime.now(ZoneId.of("Asia/Seoul"))); // 불필요한 공백 제거
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
                    String imgId = "img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER) // 줄 바꿈 정리
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
                    String inqryImgId = "inqry_img_" + LocalDateTime.now(ZoneId.of("Asia/Seoul")).format(FilesUtils.FILEIDX_DATE_FORMATTER) // 줄 바꿈 정리
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
    public List<InquiryOption> getAdminInquiryTypeOptions() {
        // InquiryOption enum의 모든 상수를 리스트로 반환합니다.
    	 return Arrays.asList(InquiryOption.PRODUCT, InquiryOption.DELIVERY, InquiryOption.SNS, InquiryOption.ETC);
    }
    
    @Override
    public List<InquiryTargetOption> getInquiryTargetOptions() { // 추가
        return Arrays.asList(InquiryTargetOption.values());
    }

    @Override
    public List<Store> getStoreList() {
        List<Store> storeList = customerStoreMapper.getAllStores(); // 실제 상점 목록 조회
        return customerStoreMapper.getAllStores();
    }
    
    
    
    
}