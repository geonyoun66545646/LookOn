package ks55team02.admin.adminpage.useradmin.userlist.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.systems.crypto.annotation.Decrypt;

@Mapper
public interface UserListMapper {

		
		// 검색 조건에 맞는 회원 목록 조회
		List<UserList> getUserList(UserList user);
		
		// 검색 조건에 맞는 전체 회원 수 조회
		int getUserCount(UserList searchCriteria);
		
		// 회원 상태 일괄 변경
		int updateUserStatus(Map<String, Object> paramMap);
		
		// 여러 회원의 로그인 기록을 일괄적으로 생성
	    void insertLoginHistoryForActivatedUsers(List<String> userNos);
	    
	    
	    // user_no 으로 특정 유저 검색(이지성)
	    UserList getUserByUserNo(@Param("userNo") String userNo);
}

