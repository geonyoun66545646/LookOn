package ks55team02.admin.adminpage.storeadmin.appAdmin.domain;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class AppAdmin {
	private String 			aplyId;             // 신청 ID (aply_id)
	private String 			aplyUserNo;         // 신청자 (aply_user_no)
	private String 			storeNm;            // 상점명 (store_nm)
	private String 			brnoImgId;          // 사업자 등록증 이미지 ID (brno_img_id)
	private String 			cmmDclrImgId;       // 통신 판매업 신고증 이미지 ID (cmm_dclr_img_id)
	private String 			selGdsProofImgId;   // 판매 상품 관련 증빙 이미지 ID (sel_gds_proof_img_id)
	private String 			rrnoCardCopyImgId;  // 주민등록 증 사본 이미지 ID (rrno_card_copy_img_id)
	private String 			bankbookCopyImgId;  // 통장 사본 이미지 ID (bankbook_copy_img_id)
	private String 			etcDocImgId;        // 기타서류 (etc_doc_img_id)
	private String 			plcyId;             // 수수료 비율 ID (plcy_id)
	private LocalDateTime 	ctrtAplyYmd; 		// 계약 신청일 (ctrt_aply_ymd) - DDL은 DATE, Java는 LocalDateTime
	private LocalDateTime 	ctrtBgngYmd; 		// 계약 시작일 (ctrt_bgng_ymd) - DDL은 DATE, Java는 LocalDateTime
	private int 			ctrtTermVal;        // 계약 기간 (ctrt_term_val)
	private String 			rvwMngrNo;          // 검토(승인) 관리자 (rvw_mngr_no)
	private String 			mngrNm;             // 매니저명 (mngr_nm)
	private String 			mngrEmlAddr;        // 매니저 이메일 (mngr_eml_addr)
	private String 			mngrTelNo;          // 매니저 연락처 (mngr_tel_no)
	private String 			bplcAddr;           // 사업장 주소 (bplc_addr)
	private String 			aplyStts;           // 신청(승인) 상태 (aply_stts)
	private String 			aprvRjctRsn;        // 승인 반려 사유 (aprv_rjct_rsn)
	private String 			UserNm;
}
