package ks55team02.customer.register.service;

import org.springframework.stereotype.Service;

import ks55team02.customer.register.domain.UserJoinRequest;

@Service
public interface CustomerRegisterService {

	void joinUser(UserJoinRequest userJoinRequest);
}
