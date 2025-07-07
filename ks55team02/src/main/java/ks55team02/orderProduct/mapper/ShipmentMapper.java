package ks55team02.orderProduct.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.orderProduct.domain.ShipmentDTO;

@Mapper
public interface ShipmentMapper {
	/**
     * 새로운 배송 정보를 등록합니다.
     * @param shipmentDTO 등록할 배송 정보 DTO
     * @return 삽입된 행의 수
     */
    int insertShipment(ShipmentDTO shipmentDTO);

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
     * 특정 운송장 번호로 배송 정보를 조회합니다. (운송장 번호는 UNIQUE)
     * @param waybilNo 조회할 운송장 번호
     * @return 조회된 배송 정보 DTO (없으면 null)
     */
    ShipmentDTO getShipmentByWaybilNo(String waybilNo);

    /**
     * 배송 정보를 수정합니다.
     * @param shipmentDTO 수정할 배송 정보 DTO
     * @return 업데이트된 행의 수
     */
    int updateShipment(ShipmentDTO shipmentDTO);

    /**
     * 특정 배송 번호의 배송 정보를 삭제합니다.
     * @param dlvyNo 삭제할 배송 번호
     * @return 삭제된 행의 수
     */
    int deleteShipment(String dlvyNo);

    /**
     * 다음 배송 번호를 조회합니다. (자동 생성 로직이 있다면)
     * @return 다음 배송 번호 문자열
     */
    String getNextDlvyNo();
}
