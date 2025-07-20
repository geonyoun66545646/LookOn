package ks55team02.orderproduct.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TrackingDetailDTO {
	@JsonProperty("time")
	private String time; // 시간 (예: "2025-07-07 10:00:00")
	@JsonProperty("location")
	private String location; // 위치 (예: "서울강남")
	@JsonProperty("description")
	private String description; // 설명 (예: "상품접수")
	@JsonProperty("telno") // 담당 점소 전화번호 (API 응답에 따라 있을 수도 없을 수도 있음)
	private String telno;
	@JsonProperty("manName") // 담당자 이름 (API 응답에 따라 있을 수도 없을 수도 있음)
	private String manName;
}
