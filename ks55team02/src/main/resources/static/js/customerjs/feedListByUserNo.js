
$(() => {
	// --- 1. 요소 및 상태 변수 초기화 ---
	const container = $('.my-feed-container');
	const pageOwnerUserNo = container.data('page-owner-user-no');
	const loginUserNo = container.data('login-user-no');

	let currentPage = 1;
	let isLoading = false;
	let hasNext = true;

	const isMyFeed = pageOwnerUserNo === loginUserNo;
	const apiUrl = isMyFeed
		? '/customer/api/feeds/my-feed'
		: `/customer/api/feeds/user-feed/${pageOwnerUserNo}`;

	// --- 2. 헬퍼 함수 (HTML 생성) ---
	const createFeedItemHtml = (feed) => {
		const defaultImageUrl = '/uploads/feeds/default_feed_image.jpg';
		const imageUrl = feed.representativeImage ? feed.representativeImage.imgFilePathNm : defaultImageUrl;
		const imageAlt = feed.representativeImage ? feed.representativeImage.imgAltTxtCn : '기본 이미지';

		// 상세 페이지로 이동 시, 컨텍스트를 전달하여 돌아올 위치를 알 수 있게 함
		let detailLink;
		if (isMyFeed) {
			detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=my`;
		} else {
			detailLink = `/customer/feed/feedDetail/${feed.feedSn}?context=user&userNo=${pageOwnerUserNo}`;
		}

		return `
            <article class="feed-item">
                <a href="${detailLink}">
                    <img src="${imageUrl}" alt="${imageAlt}">
                    <div class="item-overlay">
                        <span class="likes">♥ ${feed.likeCount}</span>
                    </div>
                </a>
            </article>
        `;
	};

	// --- 3. 렌더링 및 데이터 로딩 ---
	const renderFeeds = (feeds) => {
		const gridContainer = $('#feed-grid-container');
		if (currentPage === 1 && feeds.length === 0) {
			let message = isMyFeed ? '작성한 피드가 없습니다.' : '이 사용자가 작성한 피드가 없습니다.';
			const noFeedHtml = `
	            <div class="no-feeds-container">
	                <p>${message}</p>
	            </div>
	        `;
			gridContainer.html(noFeedHtml);
			hasNext = false;
			return;
		}
		feeds.forEach(feed => {
			gridContainer.append(createFeedItemHtml(feed));
		});
	};

	const loadNextPage = () => {
		if (isLoading || !hasNext) return;
		isLoading = true;
		$('#loading-indicator').show();

		$.ajax({
			url: apiUrl,
			type: 'GET',
			data: { page: currentPage },
			dataType: 'json'
		})
			.done(response => {
				renderFeeds(response.feedList);
				hasNext = response.hasNext;
				currentPage++;
			})
			.fail((jqXHR) => {
				// 401(Unauthorized) 에러가 발생하면, 로그인 모달을 띄운다.
				if (jqXHR.status === 401) {
					$('#signin-modal').modal('show');
				} else {
					// 그 외의 에러는 콘솔에 기록한다.
					console.error('피드 로딩 중 오류 발생:', jqXHR.statusText);
				}
			})
			.always(() => {
				isLoading = false;
				$('#loading-indicator').hide();
			});
	};

	// --- 4. 이벤트 핸들러 및 초기 실행 ---
	const onScroll = () => {
		if ($(window).scrollTop() + $(window).height() >= $(document).height() - 200) {
			if (hasNext) loadNextPage();
		}
	};

	$(window).on('scroll', onScroll);

	// 페이지 로드 시 첫 페이지 데이터 로딩
	loadNextPage();
});