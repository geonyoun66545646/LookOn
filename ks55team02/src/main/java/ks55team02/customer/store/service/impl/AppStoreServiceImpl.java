package ks55team02.customer.store.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.admin.adminpage.useradmin.userlist.mapper.UserListMapper;
import ks55team02.customer.store.mapper.AppStoreMapper;
import ks55team02.customer.store.service.AppStoreService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class AppStoreServiceImpl implements AppStoreService{
	
	private final AppStoreMapper appStoreMapper;
	private final UserListMapper userListMapper;
	

@Override
public UserList getUserInfo(String userNo) {
	UserList userList= userListMapper.getUserByUserNo(userNo);
	return userList;
}
	

	
	
}
