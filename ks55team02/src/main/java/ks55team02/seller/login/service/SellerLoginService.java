package ks55team02.seller.login.service;

import ks55team02.customer.login.domain.LoginUser;
import ks55team02.seller.login.domain.SellerLoginRequest;

public interface SellerLoginService {

	LoginUser login(SellerLoginRequest loginRequest);
}
