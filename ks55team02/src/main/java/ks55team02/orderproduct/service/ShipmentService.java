package ks55team02.orderproduct.service;

import java.util.List;

import ks55team02.orderproduct.domain.ShipmentDTO;


public interface ShipmentService {
	
	 /**
     * 새로운 배송 정보를 추가합니다.
     * @param shipmentDTO 추가할 배송 정보 DTO
     * @return 성공 여부 (true: 성공, false: 실패)
     */
    boolean addShipment(ShipmentDTO shipmentDTO);

    /**
     * 모든 배송 정보 목록을 조회합니다.
     * @return 배송 정보 DTO 목록
     */
    List<ShipmentDTO> getAllShipments();

    /**
     * 특정 배송 번호로 배송 정보를 조회합니다.
     * @param dlvyNo 조회할 배송 번호
     * @return 조회된 배송 정보 DTO (없으면 null)
     */
    ShipmentDTO getShipmentByDlvyNo(String dlvyNo);

    /**
     * 배송 정보를 수정합니다.
     * @param shipmentDTO 수정할 배송 정보 DTO
     * @return 성공 여부 (true: 성공, false: 실패)
     */
    boolean modifyShipment(ShipmentDTO shipmentDTO);

    /**
     * 특정 배송 번호의 배송 정보를 삭제합니다.
     * @param dlvyNo 삭제할 배송 번호
     * @return 성공 여부 (true: 성공, false: 실패)
     */
    boolean removeShipment(String dlvyNo);

    /**
     * 운송장 번호를 통해 배송 정보를 조회합니다.
     * @param waybilNo 운송장 번호
     * @return 조회된 배송 정보 DTO (없으면 null)
     */
    ShipmentDTO getShipmentByWaybilNo(String waybilNo);
}
