package ks55team02.admin.adminpage.useradmin.userlist.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import ks55team02.admin.common.domain.SearchCriteria;
import ks55team02.systems.crypto.annotation.EncryptedField;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 회원 목록 정보를 담는 도메인(DTO/VO) 클래스입니다.
 * DB의 'users' 테이블 컬럼들과 매핑되며,
 * 검색 조건 및 페이징 관련 정보를 함께 담는 역할을 수행합니다.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserList extends SearchCriteria {

	// ====================================================================================
	// DB 'users' 테이블 컬럼과 매핑되는 필드들
	// ====================================================================================
	private String userNo; // 회원 고유번호 (PK)
	private String mbrGrdCd; // 회원 등급 코드
	private String userLgnId; // 회원 로그인 ID
	
	@EncryptedField
	private String userNm; // 회원 이름
	
	private String userPswdEncptVal; // 비밀번호 암호화 값 (조회 시에는 사용되지 않음)
	private String genderSeCd; // 성별 구분 코드 ('m', 'f' 등)
	
	@EncryptedField
	private String emlAddr; // 이메일 주소
	
	@EncryptedField
	private String telno; // 전화번호
	
	private LocalDate userBrdt; // 회원 생년월일
	private String zipCd; // 우편번호
	
	@EncryptedField
	private String addr; // 주소
	
	@EncryptedField
	private String daddr; // 상세 주소
	
	private String userNcnm; // 닉네임
	private String userStatus; // 회원 상태 (예: '정상', '휴면', '탈퇴' 등)
	private LocalDateTime joinDt; // 가입일
	private LocalDateTime whdwlDt; // 탈퇴일
	private LocalDateTime lastInfoMdfcnDt; // 최종 정보 수정일
	private LocalDateTime lastLoginDt; // 최종 로그인 일시


	public List<String> getStatusList() {
        return super.getFilterConditions();
    }
    
    public void setStatusList(List<String> statusList) {
        super.setFilterConditions(statusList);
    }
    // ====================================================================================
 	// 편의 메서드 (데이터를 가공하여 반환)
 	// ====================================================================================
 	/**
 	 * 성별 구분 코드를 한글 텍스트로 변환하여 반환합니다.
 	 * @return '남' 또는 '여', 해당하는 코드가 없으면 '-' 반환.
 	 */   
    // 편의 메소드 
    public String getGenderText() {
        if ("m".equalsIgnoreCase(this.genderSeCd)) return "남";
        if ("f".equalsIgnoreCase(this.genderSeCd)) return "여";
        return "-";
    }

    /**
	 * 주소와 상세 주소를 결합하여 전체 주소를 반환합니다.
	 * @return 주소 + 상세 주소, 주소 정보가 없으면 '-' 반환.
	 */
    public String getFullAddress() {
        if (this.addr != null && !this.addr.isEmpty()) {
            return this.addr + (this.daddr != null ? " " + this.daddr : "");
        }
        return "-";
    }
    
    
}
