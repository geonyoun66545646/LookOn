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
			// ⭐ 메인 슬라이더 버튼 선택자도 더 명확하게 지정합니다.
			nextEl: '.main-page-slider-container .swiper-button-next',
			prevEl: '.main-page-slider-container .swiper-button-prev',
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
	/* ⭐ Weekly Best 슬라이더 초기화 ⭐ */
	/* =================================================================== */
	const weeklySwiperContainer = document.querySelector('.weekly-best-slider-container .weekly-best-swiper');

	if (weeklySwiperContainer) {
		const weeklySwiper = new Swiper(weeklySwiperContainer, {
			// 옵션 설정
			loop: false,
			autoplay: false,
			slidesPerView: 'auto',
			spaceBetween: 1,

			// ⭐ Weekly Best 슬라이더 버튼 선택자 수정 ⭐
			navigation: {
				nextEl: '.weekly-best-slider-container .swiper-button-next', // 이 부분이 원래도 이렇게 되어있었어야 합니다.
				prevEl: '.weekly-best-slider-container .swiper-button-prev', // 이 부분이 원래도 이렇게 되어있었어야 합니다.
			},
		});
	}

	/* =================================================================== */
	/* ⭐ Sale 슬라이더 초기화 ⭐ */
	/* =================================================================== */
	const saleSwiperContainer = document.querySelector('.sale-products-slider-container .sale-products-swiper');

	if (saleSwiperContainer) {
		const saleSwiper = new Swiper(saleSwiperContainer, {
			loop: false,
			autoplay: false,
			slidesPerView: 'auto',
			spaceBetween: 1,

			// ⭐ Sale 슬라이더 버튼 선택자 수정 (고유 클래스 사용) ⭐
			navigation: {
				nextEl: '.sale-products-slider-container .swiper-button-next',
				prevEl: '.sale-products-slider-container .swiper-button-prev',
			},
		});
	}
});