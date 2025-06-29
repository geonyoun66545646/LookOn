package ks55team02.admin.adminpage.useradmin.userlist.servic;

import java.util.List;


import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;


public interface UserListService {

	/**
     * 조건에 맞는 회원 목록과, 동적으로 계산된 상태(휴면 등)를 함께 조회
     * @param searchCondition 검색 조건
     * @return 상태가 재계산된 회원 목록
     */
     List<UserList> getUserList(UserList searchCondition);
     
     /**
      * 여러 회원의 상태를 일괄적으로 변경하는 메소드
      * @param userNos 변경할 회원 번호 목록
      * @param status 변경될 상태값
      */
     void updateUserStatus(List<String> userNos, String status);
     
     int getUserCount(UserList searchCriteria);
}
