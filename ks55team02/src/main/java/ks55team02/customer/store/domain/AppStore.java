package ks55team02.customer.store.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class AppStore {
	private String 				aplyId;         		// 신청 ID
	private String 				aplyUserNo;      		// 신청 사용자 번호
	private String 				storeNm;         		// 상점 명
	private String 				brnoImgId;      		// 사업자등록증 이미지 ID
	private String 				cmmDclrImgId;    		// 통신판매업신고증 이미지 ID
	private String 				selGdsProofImgId;		// 판매상품 증빙 이미지 ID
	private String 				rrnoCardCopyImgId;		// 주민등록증 사본 이미지 ID
	private String 				bankbookCopyImgId;		// 통장 사본 이미지 ID
	private String 				etcDocImgId;     		// 기타 서류 이미지 ID
	private String 				plcyId;          		// 정책 ID
	private LocalDateTime 		ctrtAplyYmd;     		// 계약 신청일
	private LocalDateTime 		ctrtBgngYmd;     		// 계약 시작일
	private Integer 			ctrtTermVal;     		// 계약 기간 값
	private String 				rvwMngrNo;       		// 검토 매니저 번호
	private String 				mngrNm;          		// 매니저 명
	private String 				mngrEml;         		// 매니저 이메일
	private String 				mngrTelNo;       		// 매니저 연락처
	private String 				bplcAddr;        		// 사업장 주소
	private String 				aplyStts;        		// 신청 상태
	private String 				aprvRjctRsn;     		// 승인 거절 사유

}
