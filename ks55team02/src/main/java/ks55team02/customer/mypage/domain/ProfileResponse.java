package ks55team02.customer.mypage.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ProfileResponse {

	private String userNo;
    private String userNcnm;
    private String prflImg;
    private String selfIntroCn;
    private LocalDateTime lastPrflMdfcnDt;
}
