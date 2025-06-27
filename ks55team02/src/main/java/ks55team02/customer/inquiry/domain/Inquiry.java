package ks55team02.customer.inquiry.domain;

import java.util.Date;

import lombok.Data;

// 필드선언
@Data
public class Inquiry {
	private String 	inqryId;
	private String 	inqryTypeCd;
	private String 	inqryTrgtTypeCd;
	private String 	wrtrId;
	private String 	inqryStoreId;
	private String 	inqryTtl;
	private String 	inqryCn;
	private boolean prvtYn;
	private String 	prcsStts;
	private String 	prcsUserNo;
	private Date	regYmd;
	private Date 	mdfcnYmd;
	private Date 	delDt;
	private String 	delUserNo;
	private boolean delActvtnYn;
}
