package ks55team02.customer.post.domain;

import lombok.Data;

@Data
public class Board {
	private String bbsClsfCd;
	private String bbsCreatrUserNo;
	private String bbsNm;
	private String bbsPrpsCn;
	private String readAuthrtLvlVal;
	private String wrtAuthrtLvlVal;
	private String cmntWrtAuthrtLvlVal;
	private String crtDt;
	private String mdfcnDt;
	private String delDt;
}
