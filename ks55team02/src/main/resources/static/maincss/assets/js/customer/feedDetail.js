$(() => {
    // ====================================================================
    // 1. 초기 설정: 페이지의 '맥락' 정보를 HTML에서 가져와 변수에 저장
    // 이 변수들은 이 스크립트 파일 내의 모든 함수에서 접근 가능합니다.
    // ====================================================================
    const container = $('.feed-detail-container');
    const context = container.data('context');

    // 무한 스크롤 상태를 관리하는 변수
    let isLoading = false;
    let hasNext = true; // 다음 피드가 더 있는지 여부


    // ====================================================================
    // 2. HTML 생성 함수: 서버에서 받은 JSON 데이터를 HTML 구조로 변환
    // ====================================================================
	const createFeedDetailHtml = (feed) => {
	     // ---------------------------------
	     // 1. 데이터 준비 및 HTML 조각 생성
	     // ---------------------------------

	     // [작성자 정보]
	     const writerNickname = feed.writerInfo?.userNcnm || '알 수 없는 사용자';
	     const writerProfileImg = '/uploads/profiles/profiles.jpg'; // 현재 고정된 프로필 이미지
	     const writerProfileLink = `/customer/feed/feedListByUserNo/${feed.wrtrUserNo}`;
	     
	     // [이미지 목록 HTML 생성]
	     // imageList가 배열이고 내용이 있을 경우에만, 각 이미지를 <img> 태그로 변환하여 합칩니다.
	     let imageListHtml = '';
	     if (feed.imageList && Array.isArray(feed.imageList) && feed.imageList.length > 0) {
	         imageListHtml = feed.imageList.map(image => `
	             <img src="${image.imgFilePathNm}" alt="피드 이미지" class="main-feed-image">
	         `).join(''); // 생성된 모든 <img> 태그를 하나의 문자열로 합칩니다.
	     }

	     // [피드 내용(feedCn) HTML 생성]
	     let feedContentHtml = '';
	     if (feed.feedCn && feed.feedCn.trim() !== '') {
	         feedContentHtml = `
	             <div class="feed-content-body">
	                 <p>${feed.feedCn}</p>
	             </div>
	         `;
	     }

	     // [해시태그 HTML 생성] (현재는 정적이지만, 향후 동적으로 변경 가능)
	     const hashtagsHtml = `
	         <div class="hashtags">
	             <a href="#">#오늘의코디</a> <a href="#">#여행코디</a> <a href="#">#클럽</a> <a href="#">#빈티지</a> <a href="#">#아웃핏</a> <a href="#">#마실룩</a> <a href="#">#데일리룩</a>
	         </div>
	     `;

	     // ---------------------------------
	     // 2. 최종 HTML 구조 조립
	     // ---------------------------------
	     return `
	         <article class="feed-post" 
	                  data-crt-dt="${feed.crtDt}" 
	                  data-wrtr-user-no="${feed.wrtrUserNo}">
	             
	             <header class="feed-post-header">
	                 <div class="user-profile">
	                     <a href="${writerProfileLink}">
	                         <img src="${writerProfileImg}" alt="프로필 사진" class="profile-image">
	                     </a>
	                     <div class="user-info">
	                         <span class="username">${writerNickname}</span>
	                     </div>
	                 </div>
	                 <div class="post-actions">
	                     <button class="follow-btn">+ 팔로우</button>
	                     <button class="more-options-btn" aria-label="더보기">
	                         <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M6 10c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm12 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm-6 0c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z"></path></svg>
	                     </button>
	                 </div>
	             </header>

	             <div class="feed-post-content">
	                 ${imageListHtml}
	             </div>
	             
	             <footer class="feed-post-footer">
	                 <div class="interaction-buttons">
	                     <button aria-label="좋아요"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"></path></svg></button>
	                     <button aria-label="댓글"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-2 12H6v-2h12v2zm0-3H6V9h12v2zm0-3H6V6h12v2z"></path></svg></button>
	                     <button aria-label="공유"><svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"><path d="M18 16.08c-.76 0-1.44.3-1.96.77L8.91 12.7c.05-.23.09-.46.09-.7s-.04-.47-.09-.7l7.05-4.11c.54.5 1.25.81 2.04.81 1.66 0 3-1.34 3-3s-1.34-3-3-3-3 1.34-3 3c0 .24.04.47.09.7L8.04 9.81C7.5 9.31 6.79 9 6 9c-1.66 0-3 1.34-3 3s1.34 3 3 3c.79 0 1.5-.31 2.04-.81l7.12 4.16c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3s3-1.34 3-3-1.34-3-3-3z"></path></svg></button>
	                 </div>
	                 <p class="like-count">좋아요 ${feed.likeCount}개</p>
	                 ${feedContentHtml}
	                 ${hashtagsHtml}
	                 <div class="comment-input-area">
	                     <input type="text" placeholder="첫 댓글을 남겨주세요" class="comment-input">
	                 </div>
	                 <p class="post-timestamp">${feed.crtDt}</p>
	             </footer>
	         </article>
	     `;
	 };


    // ====================================================================
    // 3. 비동기 통신 함수: 서버에 다음 피드 데이터를 요청
    // ====================================================================
    const loadNextFeed = () => {
        if (isLoading || !hasNext) {
            return; // 이미 로딩 중이거나 다음 피드가 없으면 함수 종료
        }

        // 현재 페이지에 로드된 마지막 피드 요소를 기준으로 다음 피드를 찾습니다.
        const lastFeed = $('article.feed-post').last();
		const wrtrUserNo = lastFeed.data('wrtr-user-no');
        const currentFeedCrtDt = lastFeed.data('crt-dt');

        // 기준이 되는 피드가 없으면(페이지에 피드가 하나도 없으면) 함수 종료
        if (!currentFeedCrtDt) {
            return; 
        }

        isLoading = true;
        $('#loading-indicator').show(); // 로딩 인디케이터 표시

        // [핵심] 서버에 보낼 요청 파라미터들을 객체로 만듭니다.
        // 스크립트 상단에 선언된 context, userNo 변수를 여기서 사용합니다.
        const requestData = {
            currentFeedCrtDt: currentFeedCrtDt,
            limit: 3, // 한 번에 불러올 피드 개수
            context: context, 
            userNo: wrtrUserNo
        };
        
        $.ajax({
            url: '/customer/feed/next',
            type: 'GET',
            data: requestData,
            dataType: 'json'
        })
        .done(nextFeedList => {
            // 서버 응답이 성공(2xx)이고, 내용이 있는 경우
            if (nextFeedList && nextFeedList.length > 0) {
                // 받아온 피드 목록을 하나씩 화면에 추가
                nextFeedList.forEach(nextFeed => {
                    const newFeedHtml = createFeedDetailHtml(nextFeed);
                    container.append(newFeedHtml);
                });
            } else {
                // 서버 응답은 성공했지만, 더 이상 다음 피드가 없는 경우 (204 No Content)
                hasNext = false;
                // (선택 사항) "마지막 피드입니다."와 같은 메시지를 표시할 수 있습니다.
                 $('#loading-indicator').html('<p>마지막 피드입니다.</p>').show();
            }
        })
        .fail(err => {
            console.error("다음 피드를 불러오는 데 실패했습니다.", err);
            hasNext = false; // 오류 발생 시 더 이상 시도하지 않음
        })
        .always(() => {
            isLoading = false;
            // 로딩 인디케이터를 숨기거나, 마지막 피드 메시지를 그대로 둘 수 있습니다.
            if(hasNext) {
                $('#loading-indicator').hide();
            }
        });
    };

    // ====================================================================
    // 4. 이벤트 핸들러: 스크롤 이벤트를 감지하여 함수 호출
    // ====================================================================
    const onScroll = () => {
        // 페이지 하단 근처에 도달했는지 확인
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 200) {
            loadNextFeed();
        }
    };

    // window 객체에 스크롤 이벤트 리스너를 등록합니다.
    $(window).on('scroll', onScroll);

});