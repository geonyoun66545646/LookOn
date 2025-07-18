package ks55team02.common.domain.store;

import java.time.LocalDate;

import lombok.Data;

@Data
public class AppStore {
    private String aplyId; // 신청 ID
    private String aplyUserNo; // 신청자
    private String storeNm; // 상점명
    private String brnoImgId; // 사업자 등록증 이미지 ID
    private String cmmDclrImgId; // 통신 판매업 신고증 이미지 ID
    private String selGdsProofImgId; // 판매 상품 관련 증빙 이미지 ID
    private String rrnoCardCopyImgId; // 주민등록 증 사본 이미지 ID
    private String bankbookCopyImgId; // 통장 사본 이미지 ID
    private String etcDocImgId; // 기타서류
    private String plcyId; // 수수료 비율 ID
    private LocalDate ctrtAplyYmd; // 계약 신청일
    private Integer ctrtTermVal; // 계약 기간
    private String rvwMngrNo; // 검토(승인) 관리자
    private String mngrNm; // 매니저명
    private String mngrEml; // 매니저 이메일
    private String mngrTelNo; // 매니저 연락처
    private String bplcAddr; // 사업장 주소 (메인 주소)
    private String aplyStts; // 신청(승인) 상태
    private String aprvRjctRsn; // 승인 반려 사유
    private String storeIntroCn; // 상점 소개
    private String brno; // 사업자등록번호 (새로 추가)
    private String bplcDaddr; // 사업장 상세 주소 (새로 추가)
    private String bplcPostCode; // 사업장 우편번호 (새로 추가)
    private String bankNm; // 은행명
    private Long 	actno; // 계좌번호
    private String dpstrNm; // 예금주명
}
