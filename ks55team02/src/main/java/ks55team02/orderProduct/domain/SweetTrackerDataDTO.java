package ks55team02.orderProduct.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class SweetTrackerDataDTO {
	@JsonProperty("invoiceNo") // JSON 필드명과 Java 필드명이 다를 경우 매핑
	private String invoiceNo;
	@JsonProperty("senderName")
	private String senderName;
	@JsonProperty("receiverName")
	private String receiverName;
	@JsonProperty("itemName")
	private String itemName;
	@JsonProperty("itemQty")
	private String itemQty;
	@JsonProperty("status") // 현재 배송 상태 (예: '배달완료', '배송중' 등)
	private String status;
	@JsonProperty("trackingDetails")
	private List<TrackingDetailDTO> trackingDetails; // 배송 상세 이력 목록
}
