package ks55team02.orderProduct.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ks55team02.orderProduct.domain.ShipmentDTO;
import ks55team02.orderProduct.service.ShipmentService;

@RestController // RESTful API 컨트롤러임을 나타냄 (응답을 JSON/XML로 자동 변환)
@RequestMapping("/api/shipments") // 기본 URL 경로 설정
public class ShipmentController {

    private final ShipmentService shipmentService;

    @Autowired // 생성자 주입
    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    /**
     * 새로운 배송 정보를 등록합니다.
     * POST /api/shipments
     * @param shipmentDTO 등록할 배송 정보 (JSON 형식으로 요청 본문에 포함)
     * @return 성공 시 201 Created, 실패 시 400 Bad Request 또는 500 Internal Server Error
     */
    @PostMapping
    public ResponseEntity<String> addShipment(@RequestBody ShipmentDTO shipmentDTO) {
        try {
            boolean success = shipmentService.addShipment(shipmentDTO);
            if (success) {
                return new ResponseEntity<>("배송 정보 등록 성공", HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("배송 정보 등록 실패", HttpStatus.BAD_REQUEST);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 모든 배송 정보 목록을 조회합니다.
     * GET /api/shipments
     * @return 배송 정보 DTO 목록과 200 OK
     */
    @GetMapping
    public ResponseEntity<List<ShipmentDTO>> getAllShipments() {
        List<ShipmentDTO> shipments = shipmentService.getAllShipments();
        return new ResponseEntity<>(shipments, HttpStatus.OK);
    }

    /**
     * 특정 배송 번호로 배송 정보를 조회합니다.
     * GET /api/shipments/{dlvyNo}
     * @param dlvyNo 조회할 배송 번호
     * @return 조회된 배송 정보 DTO와 200 OK, 없으면 404 Not Found
     */
    @GetMapping("/{dlvyNo}")
    public ResponseEntity<ShipmentDTO> getShipmentByDlvyNo(@PathVariable String dlvyNo) {
        ShipmentDTO shipment = shipmentService.getShipmentByDlvyNo(dlvyNo);
        if (shipment != null) {
            return new ResponseEntity<>(shipment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 운송장 번호로 배송 정보를 조회합니다.
     * GET /api/shipments/by-waybilNo?waybilNo={waybilNo}
     * @param waybilNo 조회할 운송장 번호
     * @return 조회된 배송 정보 DTO와 200 OK, 없으면 404 Not Found
     */
    @GetMapping("/by-waybilNo")
    public ResponseEntity<ShipmentDTO> getShipmentByWaybilNo(@RequestParam String waybilNo) {
        ShipmentDTO shipment = shipmentService.getShipmentByWaybilNo(waybilNo);
        if (shipment != null) {
            return new ResponseEntity<>(shipment, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * 배송 정보를 수정합니다.
     * PUT /api/shipments/{dlvyNo}
     * @param dlvyNo 수정할 배송 번호
     * @param shipmentDTO 수정할 배송 정보 (JSON 형식으로 요청 본문에 포함)
     * @return 성공 시 200 OK, 실패 시 400 Bad Request 또는 500 Internal Server Error, 대상 없으면 404 Not Found
     */
    @PutMapping("/{dlvyNo}")
    public ResponseEntity<String> updateShipment(@PathVariable String dlvyNo, @RequestBody ShipmentDTO shipmentDTO) {
        // 경로 변수의 dlvyNo와 DTO의 dlvyNo가 일치하는지 확인 (선택 사항, 클라이언트 책임)
        if (!dlvyNo.equals(shipmentDTO.getDlvyNo())) {
            return new ResponseEntity<>("경로의 배송 번호와 요청 본문의 배송 번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
        try {
            ShipmentDTO existingShipment = shipmentService.getShipmentByDlvyNo(dlvyNo);
            if (existingShipment == null) {
                return new ResponseEntity<>("수정할 배송 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }

            boolean success = shipmentService.modifyShipment(shipmentDTO);
            if (success) {
                return new ResponseEntity<>("배송 정보 수정 성공", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("배송 정보 수정 실패", HttpStatus.BAD_REQUEST);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 특정 배송 번호의 배송 정보를 삭제합니다.
     * DELETE /api/shipments/{dlvyNo}
     * @param dlvyNo 삭제할 배송 번호
     * @return 성공 시 204 No Content, 실패 시 400 Bad Request 또는 500 Internal Server Error, 대상 없으면 404 Not Found
     */
    @DeleteMapping("/{dlvyNo}")
    public ResponseEntity<String> deleteShipment(@PathVariable String dlvyNo) {
        try {
            ShipmentDTO existingShipment = shipmentService.getShipmentByDlvyNo(dlvyNo);
            if (existingShipment == null) {
                return new ResponseEntity<>("삭제할 배송 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
            }

            boolean success = shipmentService.removeShipment(dlvyNo);
            if (success) {
                return new ResponseEntity<>("배송 정보 삭제 성공", HttpStatus.NO_CONTENT); // 204 No Content는 본문 없음
            } else {
                return new ResponseEntity<>("배송 정보 삭제 실패", HttpStatus.BAD_REQUEST);
            }
        } catch (RuntimeException e) {
            return new ResponseEntity<>("서버 오류: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
