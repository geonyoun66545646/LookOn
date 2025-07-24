$(() => {
	// --- 1. 요소 및 상태 변수 초기화 ---
	const $feedContainer = $('.feed-container');
	const $container = $('#feed-list-grid-container');
	const $loadingIndicator = $('#loading-indicator');
	const $sortDropdown = $('#sort-dropdown'); // [신규] 정렬 드롭다운 요소 캐싱

	// HTML의 data 속성에서 초기 상태를 읽어옴
	const state = {
		currentPage: parseInt($feedContainer.data('current-page'), 10) || 1,
		isLoading: false,
		hasNext: $feedContainer.data('has-next'),
		activeTab: $feedContainer.data('active-tab') || 'discover',
		sortOrder: $sortDropdown.val() || 'latest' // [신규] 현재 정렬 상태 초기화
	};

	// 비로그인 상태에서 팔로잉 탭으로 접근했을 경우 로그인 모달 띄우기
	if ($feedContainer.data('needs-login')) {
		$('#signin-modal').modal('show');
	}

	const loginUser = window.loginUser || null;

	// --- 2. 헬퍼 함수 (HTML 생성) ---
	const createFeedItemHtml = (feed) => {
		const defaultImageUrl = '/images/default-feed.png';
		const imageUrl = feed.representativeImage?.imgFilePathNm || defaultImageUrl;
		const imageAlt = feed.representativeImage?.imgAltTxtCn || '피드 대표 이미지';
		const writerNickname = feed.writerInfo?.userNcnm || '알 수 없는 사용자';
		const profileImageUrl = feed.writerInfo?.prflImg || '/uploads/profiles/defaultprofile.jpg';
		const detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=all`;

		return `
            <article class="feed-item">
                <a href="${detailLink}" class="feed-image-link">
                    <img src="${imageUrl}" alt="${imageAlt}">
                    <div class="item-overlay">
                        <span class="likes">♥ ${feed.likeCount}</span>
                    </div>
                </a>
                <div class="feed-writer-info">
                    <a href="/customer/feed/feedListByUserNo/${feed.wrtrUserNo}" class="writer-profile-link">
                        <img src="${profileImageUrl}" alt="${writerNickname} 프로필" class="writer-profile-image">
                        <span>${writerNickname}</span>
                    </a>
                </div>
            </article>
        `;
	};

	// --- 3. 핵심 로직 (2페이지부터 데이터 로딩) ---
	const loadNextPage = () => {
		if (state.isLoading || !state.hasNext) {
			return;
		}
		state.isLoading = true;
		$loadingIndicator.show();

		const apiUrl = state.activeTab === 'following' ? '/customer/api/feeds/following' : '/customer/api/feeds/list';
		const nextPage = state.currentPage + 1; // 다음 페이지 요청

		$.ajax({
			url: apiUrl,
			type: 'GET',
			data: {
				page: nextPage,
				sort: state.sortOrder
			},
			dataType: 'json'
		})
			.done(response => {
				if (response && response.feedList && response.feedList.length > 0) {
					response.feedList.forEach(feed => {
						$container.append(createFeedItemHtml(feed));
					});
					state.currentPage = nextPage; // 현재 페이지 업데이트
					state.hasNext = response.hasNext;
				} else {
					state.hasNext = false;
				}
			})
			.fail((jqXHR) => {
				console.error('피드 추가 로딩 중 오류 발생:', jqXHR.statusText);
				state.hasNext = false;
			})
			.always(() => {
				state.isLoading = false;
				$loadingIndicator.hide();
			});
	};

	const switchToNewTab = (newTab) => {
		const targetUrl = `/customer/feed/feedList?tab=${newTab}`;
		window.location.href = targetUrl;
	};

	// --- 4. 이벤트 핸들러 ---

	// 무한 스크롤
	$(window).on('scroll', () => {
		if (state.hasNext) {
			if ($(window).scrollTop() + $(window).height() >= $(document).height() - 200) {
				loadNextPage();
			}
		}
	});

	// 탭 클릭 시 페이지 이동으로 처리
	$('#discover-tab').on('click', function(e) {
		e.preventDefault();
		if (state.activeTab !== 'discover') {
			switchToNewTab('discover');
		}
	});

	$('#following-tab').on('click', function(e) {
		e.preventDefault();
		if (!loginUser) {
			$('#signin-modal').modal('show');
			return;
		}
		if (state.activeTab !== 'following') {
			switchToNewTab('following');
		}
	});

	// 정렬 순서 변경 이벤트 핸들러
	$sortDropdown.on('change', function() {
		const selectedSort = $(this).val();

		// 현재 URL에서 쿼리 파라미터를 가져와 객체로 변환
		const currentParams = new URLSearchParams(window.location.search);

		// sort 파라미터 값을 새로 선택된 값으로 설정
		currentParams.set('sort', selectedSort);

		// 새로운 쿼리 스트링을 포함하여 페이지를 리로드
		window.location.href = window.location.pathname + '?' + currentParams.toString();
	});

	// --- 초기 실행 ---
	// JS는 초기 로딩을 하지 않으므로 이 섹션은 비워둡니다.
});