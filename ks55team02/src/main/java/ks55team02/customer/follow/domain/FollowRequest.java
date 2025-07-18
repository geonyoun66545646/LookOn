package ks55team02.customer.follow.domain;

import lombok.Data;

@Data
public class FollowRequest {

	/**
     * 팔로우 당하는 사람의 userNo
     */
    private String followedUserNo;
}
