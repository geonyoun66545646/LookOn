package ks55team02.customer.inquiry.service;

import java.util.List;
import java.util.Map; // Map 사용을 위해 import

import org.springframework.web.multipart.MultipartFile;

import ks55team02.common.domain.inquiry.Answer;
import ks55team02.common.domain.inquiry.Inquiry;
import ks55team02.common.domain.store.Store;
import ks55team02.customer.inquiry.domain.InquiryOption;
import ks55team02.customer.inquiry.domain.InquiryTargetOption;




public interface InquiryService {

    /**
     * 새로운 문의를 등록하고, 첨부된 이미지가 있다면 함께 저장합니다.
     * @param inquiry 등록할 Inquiry 객체
     * @param attachedFiles 첨부된 파일 배열
     */
    void addInquiry(Inquiry inquiry, MultipartFile[] attachedFiles, String currentUserId); // MultipartFile[] 버전만 유지
    
    /**
     * 문의 대상 옵션 (상점, 관리자) 리스트를 조회합니다.
     * @return 문의 대상 옵션 리스트
     */
    List<InquiryTargetOption> getInquiryTargetOptions(); // 추가

    /**
     * 문의 유형 옵션 리스트를 조회합니다.
     * @return 문의 유형 옵션 리스트
     */
    List<InquiryOption> getAdminInquiryOptions();
    
    /**
     * 상점 목록을 조회합니다.
     * @return 상점 DTO 리스트
     */
    List<Store> getStoreList(); // 추가 (Store DTO가 필요합니다)


    /**
     * 모든 문의 리스트를 조회합니다. (페이징 없는 버전, 필요시 유지)
     * @return Inquiry 객체 리스트
     */
    List<Inquiry> getInquiryList();

    /**
     * 문의 상세, 문의 ID로 정보를 조회합니다.
     * @param inquiryId 문의 ID
     * @return 조회된 Inquiry 객체
     */
    Inquiry getInquiryById(String inquiryId,  String currentUserId);
    
    /**
     * 페이징 처리된 문의 목록을 조회합니다.
     * @param currentPage 현재 페이지 번호
     * @param pageSize 페이지 크기
     * @return 문의 목록 및 페이징 정보를 담은 Map
     */
    Map<String, Object> getInquiryList(int currentPage, int pageSize); // 페이징 메서드 시그니처 유지
    
    Answer getAnswerByInquiryId(String answer);
    
    
}
