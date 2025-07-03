package ks55team02.customer.register.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserProfile {

	private String userNo;
    private String prflImg;
    private String selfIntroCn;
    private String pushNtfctnAgreYn;
    private String emlRcptnAgreYn;
    private LocalDateTime lastPrflMdfcnDt;
}
