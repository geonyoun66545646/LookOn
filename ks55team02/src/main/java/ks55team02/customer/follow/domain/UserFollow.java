package ks55team02.customer.follow.domain;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserFollow {

	/**
     * 팔로우 ID (PK)
     */
    private long flwId;

    /**
     * 팔로우 한 사람의 userNo (follower)
     */
    private String flwrUserNo;

    /**
     * 팔로우 당한 사람의 userNo (followed)
     */
    private String flwedUserNo;

    /**
     * 팔로우 한 날짜
     */
    private LocalDateTime flwDt;
}
