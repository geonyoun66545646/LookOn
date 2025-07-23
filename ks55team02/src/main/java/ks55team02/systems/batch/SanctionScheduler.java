package ks55team02.systems.batch;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SanctionScheduler {

	private final SanctionBatchMapper sanctionBatchMapper;

	/**
	 * 매일 0시 0분 0초에 실행되는 스케줄러 cron = "[초] [분] [시] [일] [월] [요일]" (fixedRate = 10000)
	 */
	@Scheduled(cron = "0 0 0 * * *")
	@Transactional
	public void releaseExpiredSanctions() {
		log.info("▶▶▶▶▶ 제재 만료 사용자 복구 스케줄러 시작 ◀◀◀◀◀");

		// 1. 제재 기간이 오늘 날짜로 만료된 모든 사용자 번호를 조회합니다.
		List<String> userNoListToRelease = sanctionBatchMapper.findUsersWithExpiredSanctions();

		if (userNoListToRelease != null && !userNoListToRelease.isEmpty()) {
			log.info("복구 대상 사용자: {} 명", userNoListToRelease.size());

			// 2. 조회된 사용자들의 상태를 '활동'으로 일괄 업데이트합니다.
			sanctionBatchMapper.updateUsersStatusToActive(userNoListToRelease);

			log.info("총 {}명의 사용자를 '활동' 상태로 복구했습니다.", userNoListToRelease.size());
		} else {
			log.info("금일 제재가 만료된 사용자가 없습니다.");
		}

		log.info("▶▶▶▶▶ 제재 만료 사용자 복구 스케줄러 종료 ◀◀◀◀◀");
	}
}