package ks55team02.orderProduct.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ks55team02.orderProduct.domain.SweetTrackerResponseDTO;
import ks55team02.orderProduct.service.TrackingService;

@Controller
public class TrackingController {

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    @GetMapping("/track-delivery") // 이 URL로 접속하여 테스트
    public String trackDelivery(@RequestParam(name = "invoiceNo", required = false) String invoiceNo,
                                @RequestParam(name = "courierCode", required = false) String courierCode,
                                Model model) {

        if (invoiceNo != null && !invoiceNo.isEmpty() && courierCode != null && !courierCode.isEmpty()) {
            SweetTrackerResponseDTO response = trackingService.getTrackingInfo(invoiceNo, courierCode);
            
            if (response != null && "200".equals(response.getCode()) && response.getData() != null) {
                model.addAttribute("response", response.getData()); // 파싱된 데이터 객체 전달
                model.addAttribute("currentStatus", response.getData().getStatus()); // 현재 상태 문자열 전달
            } else {
                String errorMessage = "운송장 정보를 찾을 수 없거나 API 호출에 실패했습니다.";
                if (response != null && response.getMessage() != null) {
                    errorMessage += " 오류: " + response.getMessage();
                }
                model.addAttribute("message", errorMessage);
            }
        } else {
            model.addAttribute("message", "운송장 번호와 택배사 코드를 입력해주세요.");
        }
        
        return "customer/fragments/shippmentStts"; // 결과를 보여줄 Thymeleaf 템플릿 경로
    }

    // 운송장 입력 폼을 보여줄 메소드
    @GetMapping("/track-form")
    public String showTrackForm() {
        return "tracking/trackForm"; // 운송장 번호 입력 폼 템플릿
    }
}
