package ks55team02.admin.adminpage.useradmin.userlist.servic.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
import ks55team02.admin.adminpage.useradmin.userlist.mapper.UserListMapper;
import ks55team02.admin.adminpage.useradmin.userlist.servic.UserListService;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class UserListServiceImpl implements UserListService{

private final UserListMapper userListMapper;

	
	@Override
	public List<UserList> getUserList(UserList searchCondition) {

		// 1. Mapper를 호출하여 데이터베이스에서 회원 목록을 조회합니다.
		List<UserList> userList=userListMapper.getUserList(searchCondition);
		
		// 2. 조회된 회원 목록이 null이 아닌 경우에만 휴면 상태를 체크합니다.
		if(userList != null) {
			// 3. 현재 시간에서 1년을 뺀 시간을 기준으로 휴면 상태를 판단합니다
			LocalDateTime sleep = LocalDateTime.now().minusYears(1);
			
			// 4. 조회된 각 회원에 대해 반복문을 실행합니다.
			for(UserList user : userList) {
				// 5. 현재 회원 상태가 '활동'인 경우에만 휴면 여부를 확인합니다.
				if("활동".equals(user.getUserStatus())) {
					// 6. 회원의 마지막 로그인 일자를 가져옵니다.
					LocalDateTime lastLogin = user.getLastLoginDt();
					
					// 7. 마지막 로그인 기록이 없거나(null), 1년 전보다 오래된 경우(isBefore)
					if(lastLogin == null || lastLogin.isBefore(sleep)) {
						// 8. 해당 회원의 상태를 '휴면'으로 변경합니다.
						user.setUserStatus("휴면");
					}
				}
			}
		}
		// 9. 처리된 회원 목록을 반환합니다.
		return userList;
	}
	
	public int getUserCount(UserList searchCriteria) {
		// Mapper를 호출하여 데이터베이스에서 전체 회원 수를 조회한 후 반환합니다.	
		return userListMapper.getUserCount(searchCriteria);
	}
	
	@Override
	public void updateUserStatus(List<String> userNos, String status) {
	// 1. 전달받은 회원 번호 목록이 유효한지(null이거나 비어있는지) 검사합니다.
    //    유효하지 않으면 메서드를 즉시 종료합니다.	
			// 유효성 검사
			if(userNos == null || userNos.isEmpty()) {
				return ;
			}
		// 2. Mapper로 전달할 파라미터를 담기 위해 Map 객체를 생성합니다.
		Map<String, Object> paramMap = new HashMap<>();
		// 3. 회원 번호 목록을 'userNos'라는 키로 Map에 저장합니다.
		paramMap.put("userNos", userNos); // 회원 번호 목록
		// 4. 변경할 상태 값을 'status'라는 키로 Map에 저장합니다.
        paramMap.put("status", status);   // 변경할 상태
        
        // 5. Map 객체를 Mapper로 전달하여 DB의 회원 상태를 일괄적으로 업데이트합니다.
        userListMapper.updateUserStatus(paramMap);
	}
}
