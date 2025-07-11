package ks55team02.orderProduct.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ks55team02.orderProduct.domain.ShipmentDTO;
import ks55team02.orderProduct.mapper.ShipmentMapper;
import ks55team02.orderProduct.service.ShipmentService;

@Service
public class ShipmentServiceImpl implements ShipmentService {
	
	private final ShipmentMapper shipmentMapper;

    @Autowired // 생성자 주입
    public ShipmentServiceImpl(ShipmentMapper shipmentMapper) {
        this.shipmentMapper = shipmentMapper;
    }

    /**
     * 새로운 배송 정보를 추가합니다.
     * 배송 번호 자동 생성 및 생성/수정 일시 설정.
     * @param shipmentDTO 추가할 배송 정보 DTO
     * @return 성공 여부 (true: 성공, false: 실패)
     */
    @Override
    public boolean addShipment(ShipmentDTO shipmentDTO) {
        try {
            // 다음 배송 번호 생성
            String nextDlvyNo = shipmentMapper.getNextDlvyNo();
            shipmentDTO.setDlvyNo(nextDlvyNo);

            // 현재 시간 설정
            LocalDateTime now = LocalDateTime.now();
            shipmentDTO.setDlvyInfoCrtDt(now);
            shipmentDTO.setDlvyInfoMdfcnDt(now);

            int result = shipmentMapper.insertShipment(shipmentDTO);
            return result > 0;
        } catch (Exception e) {
            System.err.println("배송 정보 추가 중 오류 발생: " + e.getMessage());
            // 실제 애플리케이션에서는 로깅 프레임워크 사용 (예: SLF4J + Logback)
            throw new RuntimeException("배송 정보 추가 실패", e); // 런타임 예외로 롤백 유도
        }
    }

    /**
     * 모든 배송 정보 목록을 조회합니다.
     * @return 배송 정보 DTO 목록
     */
    @Override
    public List<ShipmentDTO> getAllShipments() {
        return shipmentMapper.getAllShipments();
    }

    /**
     * 특정 배송 번호로 배송 정보를 조회합니다.
     * @param dlvyNo 조회할 배송 번호
     * @return 조회된 배송 정보 DTO (없으면 null)
     */
    @Override
    public ShipmentDTO getShipmentByDlvyNo(String dlvyNo) {
        return shipmentMapper.getShipmentByDlvyNo(dlvyNo);
    }

    /**
     * 배송 정보를 수정합니다.
     * 수정 일시를 현재 시간으로 자동 업데이트합니다.
     * @param shipmentDTO 수정할 배송 정보 DTO
     * @return 성공 여부 (true: 성공, false: 실패)
     */
    @Override
    public boolean modifyShipment(ShipmentDTO shipmentDTO) {
        try {
            shipmentDTO.setDlvyInfoMdfcnDt(LocalDateTime.now()); // 수정 일시 업데이트
            int result = shipmentMapper.updateShipment(shipmentDTO);
            return result > 0;
        } catch (Exception e) {
            System.err.println("배송 정보 수정 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("배송 정보 수정 실패", e);
        }
    }

    /**
     * 특정 배송 번호의 배송 정보를 삭제합니다.
     * @param dlvyNo 삭제할 배송 번호
     * @return 성공 여부 (true: 성공, false: 실패)
     */
    @Override
    public boolean removeShipment(String dlvyNo) {
        try {
            int result = shipmentMapper.deleteShipment(dlvyNo);
            return result > 0;
        } catch (Exception e) {
            System.err.println("배송 정보 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("배송 정보 삭제 실패", e);
        }
    }

    /**
     * 운송장 번호를 통해 배송 정보를 조회합니다.
     * @param waybilNo 운송장 번호
     * @return 조회된 배송 정보 DTO (없으면 null)
     */
    @Override
    public ShipmentDTO getShipmentByWaybilNo(String waybilNo) {
        return shipmentMapper.getShipmentByWaybilNo(waybilNo);
    }
}
