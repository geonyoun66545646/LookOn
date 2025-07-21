package ks55team02.customer.follow.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ks55team02.customer.follow.mapper.FollowMapper;
import ks55team02.customer.follow.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

	private final FollowMapper followMapper;

    @Override
    public boolean isFollowing(String followerUserNo, String followedUserNo) {
        // Mapper의 결과가 0보다 크면(1이면) true, 아니면 false를 반환
        return followMapper.isFollowing(followerUserNo, followedUserNo) > 0;
    }

    @Override
    public void follow(String followerUserNo, String followedUserNo) {
        // [비즈니스 규칙 1] 자기 자신은 팔로우할 수 없음
        if (followerUserNo.equals(followedUserNo)) {
            log.warn("자기 자신을 팔로우하려는 시도. userNo: {}", followerUserNo);
            return; // 메소드 종료
        }
        
        // [비즈니스 규칙 2] 이미 팔로우 중인 경우, 중복해서 INSERT 하지 않음
        if (isFollowing(followerUserNo, followedUserNo)) {
            log.info("이미 팔로우 중인 사용자에게 중복 팔로우 시도. from: {}, to: {}", followerUserNo, followedUserNo);
            return;
        }

        // 모든 규칙을 통과하면 팔로우 실행
        followMapper.follow(followerUserNo, followedUserNo);
        log.info("팔로우 성공. from: {}, to: {}", followerUserNo, followedUserNo);
    }

    @Override
    public void unfollow(String followerUserNo, String followedUserNo) {
        // [비즈니스 규칙] 자기 자신은 언팔로우할 수 없음
        if (followerUserNo.equals(followedUserNo)) {
            log.warn("자기 자신을 언팔로우하려는 시도. userNo: {}", followerUserNo);
            return;
        }
        
        // 팔로우 관계가 존재할 때만 언팔로우 실행 (불필요한 DB 접근 방지)
        if (isFollowing(followerUserNo, followedUserNo)) {
            followMapper.unfollow(followerUserNo, followedUserNo);
            log.info("언팔로우 성공. from: {}, to: {}", followerUserNo, followedUserNo);
        } else {
            log.info("팔로우 관계가 아닌 사용자에게 언팔로우 시도. from: {}, to: {}", followerUserNo, followedUserNo);
        }
    }
}
