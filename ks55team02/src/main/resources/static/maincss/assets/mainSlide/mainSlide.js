window.addEventListener('load', function () {
	/* =========================== 메인 슬라이더 =========================== */
	const sliderContainer = document.querySelector('.main-page-slider-container');
	if (sliderContainer) {
		const swiper = new Swiper('.main-page-slider-container .main-page-swiper', {
			loop: true,
			centeredSlides: true,
			autoplay: { delay: 4000, disableOnInteraction: false },
			pagination: {
				el: '.main-page-slider-container .swiper-pagination',
				type: 'progressbar',
			},
			navigation: {
				// 메인 슬라이더 버튼 선택자
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

		const autoplayButton = document.querySelector('.main-page-slider-container .swiper-autoplay-button');
		if (autoplayButton) {
			autoplayButton.addEventListener('click', () => {
				swiper.autoplay.running ? swiper.autoplay.stop() : swiper.autoplay.start();
			});
			swiper.on('autoplayStart', () => autoplayButton.classList.remove('paused'));
			swiper.on('autoplayStop', () => autoplayButton.classList.add('paused'));
		}
	}

	/* ======================== Weekly Best 슬라이더 ======================== */
	const weeklySwiperContainer = document.querySelector('.weekly-best-slider-container .weekly-best-swiper');
	if (weeklySwiperContainer) {
	    new Swiper(weeklySwiperContainer, {
	        loop: false,
	        autoplay: false,
	        slidesPerView: 'auto',
	        spaceBetween: 1,
	        navigation: {
	            // ⭐ Weekly Best 슬라이더 버튼 선택자를 해당 컨테이너와 고유 버튼 클래스로 명확히 지정 ⭐
	            nextEl: '.weekly-best-slider-container .weekly-best-button-next', // 기존에도 이 부분은 올바르게 지정되어 있었습니다.
	            prevEl: '.weekly-best-slider-container .weekly-best-button-prev', // 기존에도 이 부분은 올바르게 지정되어 있었습니다.
	        },
	    });
	}

	/* ======================= Sale Products 슬라이더 ======================= */
	const saleSwiperContainer = document.querySelector('.sale-products-slider-container .sale-products-swiper');

	if (saleSwiperContainer) {
	    new Swiper(saleSwiperContainer, {
	        loop: false,
	        autoplay: false,
	        slidesPerView: 'auto',
	        spaceBetween: 10,
	        navigation: {
	            // ⭐ Sale Products 슬라이더 버튼 선택자를 해당 컨테이너와 고유 버튼 클래스로 명확히 지정 ⭐
	            nextEl: '.sale-products-slider-container .sale-products-button-next',
	            prevEl: '.sale-products-slider-container .sale-products-button-prev',
	        },
	    });
	    console.log('[Swiper] Sale 슬라이더 초기화 완료');
	}
});