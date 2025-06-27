package ks55team02.tossApi.mapper;

import org.apache.ibatis.annotations.Mapper;

import ks55team02.tossApi.domain.Payment;
import ks55team02.tossApi.domain.PaymentHistory;

@Mapper
public interface PaymentMapper {
	
	/**
     * 결제 정보를 payments 테이블에 저장합니다.
     * 이 메소드 이름 "savePayment"은 paymentMapper.xml의 id와 일치해야 합니다.
     */
    void savePayment(Payment payments);

    /**
     * 결제 이력 정보를 payments_history 테이블에 저장합니다.
     * 이 메소드 이름 "savePaymentHistory"은 paymentMapper.xml의 id와 일치해야 합니다.
     */
    void savePaymentHistory(PaymentHistory paymentHistory);
    
}
