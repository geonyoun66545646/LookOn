// mainSlide.js

// 페이지의 모든 리소스가 로드된 후 스크립트 실행 (FOUC 방지)
window.addEventListener('load', function() {

	/* =================================================================== */
	/* ⭐ 메인슬라이더
	/* =================================================================== */

	const sliderContainer = document.querySelector('.main-page-slider-container');
	if (!sliderContainer) return;

	// Swiper.js 초기화
	const swiper = new Swiper('.main-page-slider-container .main-page-swiper', {
		loop: true,
		centeredSlides: true,
		autoplay: {
			delay: 4000,
			disableOnInteraction: false,
		},
		pagination: {
			el: '.main-page-slider-container .swiper-pagination',
			type: 'progressbar',
		},
		navigation: {
			nextEl: '.main-page-slider-container .swiper-button-next', // 수정됨: '다음' 버튼은 '.swiper-button-next'
			prevEl: '.main-page-slider-container .swiper-button-prev', // 수정됨: '이전' 버튼은 '.swiper-button-prev'
		},
		slidesPerView: 1.5,
		spaceBetween: 15,
		breakpoints: {
			768: { slidesPerView: 2.5, spaceBetween: 20 },
			1024: { slidesPerView: 3.5, spaceBetween: 25 },
		},
	});

	// 자동 재생/정지 버튼 로직
	const autoplayButton = document.querySelector('.main-page-slider-container .swiper-autoplay-button');
	if (autoplayButton) {
		autoplayButton.addEventListener('click', () => {
			if (swiper.autoplay.running) {
				swiper.autoplay.stop();
			} else {
				swiper.autoplay.start();
			}
		});

		swiper.on('autoplayStart', () => autoplayButton.classList.remove('paused'));
		swiper.on('autoplayStop', () => autoplayButton.classList.add('paused'));
	}


	/* =================================================================== */
	/* ⭐ [3-2단계: 추가] Weekly Best 슬라이더 초기화 ⭐ */
	/* =================================================================== */
	const weeklySwiperContainer = document.querySelector('.weekly-best-slider-container .weekly-best-swiper');

	if (weeklySwiperContainer) {
		const weeklySwiper = new Swiper(weeklySwiperContainer, {
			// 옵션 설정
			loop: false,
			autoplay: false,
			slidesPerView: 'auto', // CSS에 의해 슬라이드 너비 결정
			spaceBetween: 1, // 상품 카드 사이의 간격

			// 네비게이션 화살표 버튼 연결
			navigation: {
				nextEl: '.weekly-best-slider-container .swiper-button-next',
				prevEl: '.weekly-best-slider-container .swiper-button-prev',
			},
			// breakpoints 옵션은 이제 필요 없으므로 제거합니다. (auto 슬라이드 뷰에는 일반적으로 사용 안 함)
		});
	}
});