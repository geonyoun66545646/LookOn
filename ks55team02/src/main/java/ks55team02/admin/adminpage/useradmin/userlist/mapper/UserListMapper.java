	package ks55team02.admin.adminpage.useradmin.userlist.mapper;
	
	import java.util.List;
	import java.util.Map;
	
	import org.apache.ibatis.annotations.Mapper;
	import org.apache.ibatis.annotations.Param;
	
	import ks55team02.admin.adminpage.useradmin.userlist.domain.UserList;
	
	@Mapper
	public interface UserListMapper {
	
		/**
		 * 주어진 검색 조건({@link UserList} 객체에 포함된)에 따라 회원 목록을 조회합니다.
		 * 페이징(offset, amount), 검색어, 회원 상태, 정렬 기준 등이 조건으로 사용될 수 있습니다.
		 *
		 * @param user 검색 및 페이징 조건을 담은 {@link UserList} 도메인 객체
		 * @return     조건에 맞는 회원 목록을 담은 {@code List<UserList>}
		 */
		List<UserList> getUserList(UserList user);
		
		/**
		 * 주어진 검색 조건에 맞는 전체 회원 수를 조회합니다.
		 * 이는 페이징 처리를 위한 총 레코드 수를 계산하는 데 사용됩니다.
		 *
		 * @param searchCriteria 검색 조건을 담은 {@link UserList} 도메인 객체
		 * @return               검색 조건에 맞는 전체 회원 수 (int 타입)
		 */
		int getUserCount(UserList searchCriteria);
		
		/**
		 * 여러 회원의 상태를 일괄적으로 변경합니다.
		 * 주로 '휴면', '활동', '탈퇴' 등의 상태 변경에 사용될 수 있습니다.
		 *
		 * @param paramMap 상태를 변경할 회원 번호 목록({@code List<String> userNos})과 변경할 상태 값({@code String status})을 담은 Map
		 * @return         업데이트된 레코드의 수
		 */
		int updateUserStatus(Map<String, Object> paramMap);
		
		/**
		 * 활성 상태로 변경된 회원들의 로그인 기록을 일괄적으로 생성합니다.
		 * 이는 주로 휴면 계정을 활동 계정으로 전환할 때 사용될 수 있습니다.
		 *
		 * @param userNos 로그인 기록을 생성할 사용자 번호(user_no) 목록
		 */
	    void insertLoginHistoryForActivatedUsers(List<String> userNos);
	    
	    /**
	     * 주어진 사용자 번호(userNo)로 특정 회원 정보를 조회합니다.
	     *
	     * @param userNo 조회할 회원의 고유 번호
	     * @return     조회된 회원 정보를 담은 {@link UserList} 객체. 해당하는 회원이 없을 경우 null을 반환합니다.
	     */
	    UserList getUserByUserNo(@Param("userNo") String userNo);
	}
	
