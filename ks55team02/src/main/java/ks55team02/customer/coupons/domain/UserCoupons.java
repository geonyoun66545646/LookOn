package ks55team02.customer.coupons.domain;

import java.time.LocalDateTime;
import org.springframework.data.relational.core.mapping.Column;
import lombok.Data;

@Data
public class UserCoupons {
	
	private String userCpnId;
	private String userNo;
	private String pblcnCpnId;

	// 이 필드는 다른 팀원의 코드이므로 그대로 둡니다.
	private boolean useYn;

	private LocalDateTime cpnGiveDt;
	private LocalDateTime indivExpryDt;
	private LocalDateTime useDt;
	private LocalDateTime cpnMdfcnDt;
	private String issuRsnSrcCn;

	private Coupons coupon;

	// 이 필드도 다른 팀원의 코드이므로 그대로 둡니다.
	@Column(value = "use_yn")
	private boolean used;

	// ===================================================================
	// ★★★ 이 부분을 직접 추가하세요 ★★★
	// Mybatis가 'isUsed' 속성에 대한 setter로 'setIsUsed'를 찾기 때문에,
	// 해당 메소드를 수동으로 추가하여 오류를 해결합니다.
	public void setIsUsed(boolean used) {
		this.used = used;
	}
	// ===================================================================
	
}