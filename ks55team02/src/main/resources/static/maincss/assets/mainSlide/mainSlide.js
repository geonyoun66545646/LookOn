// mainSlide.js

// 페이지의 모든 리소스가 로드된 후 스크립트 실행 (FOUC 방지)
window.addEventListener('load', function() {
    
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
    if(autoplayButton) {
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
});