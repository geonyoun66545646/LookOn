package ks55team02.admin.adminpage.storeadmin.appadmin.domain;

import java.util.List;

import lombok.Data;

@Data
public class StatusUpdateRequest {
	
		private List<String> aplyIds;
	    private String newStatus; // "승인" 또는 "반려"
	    private String rejectionReason;

	}