$(document).ready(function() {
    console.log("productDetail.js 파일 실행됨.");

    // --- 1. 전역 변수 및 요소 선택 ---
    const colorSelect = $('.details-row-color .product-option-select');
    const sizeSelect = $('.details-row-size .product-option-select');
    const totalPriceSpan = $('#totalPrice');
    const selectionSummary = $('#selectionSummary');
    const selectedOptionsContainer = $('#selectedOptionsContainer');
    const selectedOptionCurrentPriceSpan = $('#selectedOptionCurrentPrice');
    let selectedOptions = [];
    
    // ⭐⭐ 새로 추가: 서버에서 제공하는 상품 상태 데이터 (예시)
    // 이 데이터는 백엔드에서 뷰로 전달되어야 합니다.
    // 예를 들어, Thymeleaf를 사용한다면:
    // <script th:inline="javascript">
    //     var productStatusData = [[${productStatus}]];
    // </script>
    // 현재 로그를 기반으로 임시 데이터를 생성합니다.
    const productStatusData = [
        { gds_stts_no: 'gds_stts_no_123', opt_vl_no: ['opt_vl_no_2657', 'opt_vl_no_2658'] },
        { gds_stts_no: 'gds_stts_no_124', opt_vl_no: ['opt_vl_no_2657', 'opt_vl_no_2659'] },
        { gds_stts_no: 'gds_stts_no_125', opt_vl_no: ['opt_vl_no_2658', 'opt_vl_no_2660'] }
    ];

    // 이미지 갤러리 관련 요소
    const mainImage = document.getElementById('product-detail-zoom');
    const galleryItems = document.querySelectorAll('.product-detail-gallery-item');
    const imageCounter = document.querySelector('.product-detail-image-counter span');
    const thumbnailGallery = document.querySelector('.product-detail-image-gallery');
    const thumbUpButton = document.querySelector('.product-detail-thumbnail-nav-btn.up');
    const thumbDownButton = document.querySelector('.product-detail-thumbnail-nav-btn.down');

    // --- 2. 초기화 및 헬퍼 함수 ---

    function formatPrice(price) {
        return price.toLocaleString('ko-KR') + '원';
    }

    function getBaseProductPrice() {
        const priceText = $('.final-price-wrapper .final-price').text() || '0';
        return parseInt(priceText.replace(/[^0-9]/g, ''), 10) || 0;
    }

    function updateTotalPrice() {
        const basePrice = getBaseProductPrice();
        let total = selectedOptions.reduce((sum, option) => sum + (basePrice * option.quantity), 0);
        totalPriceSpan.text(formatPrice(total));
    }

    function updateOptionsDisplay() {
        selectedOptionsContainer.empty();
        const basePrice = getBaseProductPrice();

        if (selectedOptions.length > 0) {
            selectionSummary.show();

            selectedOptions.forEach((option, index) => {
                const optionElement = $(`
                    <div class="selected-option-item-custom" data-index="${index}">
                        <div class="option-name-display-custom">
                            <span>${option.color} / ${option.size}</span>
                            <span class="option-item-price">${formatPrice(basePrice)}</span>
                        </div>
                        <div class="option-controls-custom">
                            <button class="qty-control-button minus" type="button">-</button>
                            <input type="text" class="option-qty-input-custom" value="${option.quantity}" readonly>
                            <button class="qty-control-button plus" type="button">+</button>
                            <button class="remove-option-button" type="button">×</button>
                        </div>
                    </div>
                `);
                selectedOptionsContainer.append(optionElement);
            });
        } else {
            selectionSummary.hide();
        }
        updateTotalPrice();
    }
    
    // ⭐⭐ 새로 추가: 옵션 번호로 상품 상태 번호를 찾는 함수
    function findProductStatus(colorOptNo, sizeOptNo) {
        const selectedOptNos = [colorOptNo, sizeOptNo].sort();
        const status = productStatusData.find(status => {
            const statusOptNos = status.opt_vl_no.sort();
            return statusOptNos.length === 2 && 
                   statusOptNos[0] === selectedOptNos[0] && 
                   statusOptNos[1] === selectedOptNos[1];
        });
        return status ? status.gds_stts_no : null;
    }

    /**
     * ⭐ 최종 수정된 옵션 선택 시 처리 로직입니다.
     * 이제 고유한 gds_stts_no를 찾아서 저장합니다.
     */
    function handleOptionSelection() {
        const selectedColorText = colorSelect.find('option:selected').text();
        const selectedSizeText = sizeSelect.find('option:selected').text();
        
        const selectedColorOptNo = colorSelect.val();
        const selectedSizeOptNo = sizeSelect.val();

        if (!selectedColorOptNo || !selectedSizeOptNo) {
            selectedOptionCurrentPriceSpan.text('옵션을 모두 선택해주세요.');
            return;
        }

        // ⭐⭐ 수정: 선택된 옵션 번호에 해당하는 gds_stts_no를 찾습니다.
        const gdsSttsNo = findProductStatus(selectedColorOptNo, selectedSizeOptNo);
        if (!gdsSttsNo) {
             alert('선택하신 옵션 조합은 재고가 없습니다.');
             return;
        }

        selectedOptionCurrentPriceSpan.text(formatPrice(getBaseProductPrice()));

        // ⭐⭐ 수정: 고유 번호(gdsSttsNo)를 사용하여 이미 추가된 옵션인지 확인
        const isExisting = selectedOptions.some(opt => opt.gdsSttsNo === gdsSttsNo);

        if (isExisting) {
            alert('이미 추가된 옵션입니다.');
        } else {
            selectedOptions.push({
                color: selectedColorText,
                size: selectedSizeText,
                quantity: 1,
                gdsSttsNo: gdsSttsNo // ⭐⭐ 이제 gdsSttsNo를 저장합니다.
            });
            updateOptionsDisplay();
        }
        selectedOptionCurrentPriceSpan.text('옵션을 모두 선택해주세요.');
    }

    // --- 3. 이벤트 리스너 등록 ---

    if (mainImage && galleryItems.length > 0) {
        const totalImages = galleryItems.length;
        const updateImageCounter = (currentIndex) => {
            if (imageCounter) {
                const counterContainer = imageCounter.closest('.product-detail-image-counter');
                if (counterContainer) {
                    counterContainer.innerHTML = `${currentIndex + 1}/${totalImages} <i class="icon-arrows"></i>`;
                }
            }
        };
        galleryItems.forEach((item, index) => {
            item.addEventListener('click', function(e) {
                e.preventDefault();
                galleryItems.forEach(thumb => thumb.classList.remove('active'));
                this.classList.add('active');
                const newImageSrc = this.getAttribute('data-image');
                mainImage.src = newImageSrc;
                updateImageCounter(index);
            });
        });
        updateImageCounter(0);
    }

    if (thumbnailGallery && thumbUpButton && thumbDownButton) {
        const scrollAmount = 100;
        thumbUpButton.addEventListener('click', () => {
            thumbnailGallery.scrollBy({ top: -scrollAmount, behavior: 'smooth' });
        });
        thumbDownButton.addEventListener('click', () => {
            thumbnailGallery.scrollBy({ top: scrollAmount, behavior: 'smooth' });
        });
    }

    $('.product-option-select').on('change', handleOptionSelection);

    selectedOptionsContainer.on('click', '.qty-control-button, .remove-option-button', function() {
        const $target = $(this);
        const $item = $target.closest('.selected-option-item-custom');
        if ($item.length === 0) return;
        const index = $item.data('index');
        let option = selectedOptions[index];

        if ($target.hasClass('plus') && option.quantity < 10) {
            option.quantity++;
        } else if ($target.hasClass('minus') && option.quantity > 1) {
            option.quantity--;
        } else if ($target.hasClass('remove-option-button')) {
            selectedOptions.splice(index, 1);
        }
        updateOptionsDisplay();
    });

    /**
     * ⭐ 최종 수정된 장바구니 버튼 클릭 이벤트
     * 이제 optNo 대신 gdsSttsNo를 전송합니다.
     */
    $('.btn-cart').on('click', function(e) {
        e.preventDefault();

        if (selectedOptions.length === 0) {
            alert('최소 하나 이상의 옵션을 추가해주세요.');
            return;
        }

        const gdsNo = currentProductGdsNo;
        const storeId = currentProductStoreId;

        const payload = {
            gdsNo: gdsNo,
            storeId: storeId,
            selectedOptions: selectedOptions.map(option => ({
                // ⭐⭐ 이제 optNo 대신 gdsSttsNo를 사용합니다.
                optNo: option.gdsSttsNo, 
                quantity: option.quantity
            }))
        };

        console.log("전송할 페이로드:", JSON.stringify(payload, null, 2));

        $.ajax({
            url: '/api/cart/add',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(payload),
            success: function(response) {
                if (confirm('장바구니에 상품이 추가되었습니다. 장바구니로 이동하시겠습니까?')) {
                    window.location.href = '/cart';
                }
            },
            error: function(xhr, status, error) {
                console.error("장바구니 추가 중 오류 발생:", error);
                let errorMessage = '장바구니 추가 중 오류가 발생했습니다.';
                if (xhr.status === 401) {
                    errorMessage = '로그인이 필요합니다.';
                } else if (xhr.responseText) {
                    try {
                        const errorResponse = JSON.parse(xhr.responseText);
                        errorMessage = errorResponse.message || errorMessage;
                    } catch (e) {
                        errorMessage = xhr.responseText;
                    }
                }
                alert(errorMessage);
            },
            complete: function() {
                selectedOptions = [];
                updateOptionsDisplay();
            }
        });
    });

    /* ========================== 구매하기 버튼 2025.07.11 gy ========================== */
    $('.btn-buy-now').on('click', function(e) {
        e.preventDefault();
        const productName = $('.product-details .product-title').text().trim();
        const productImageSrc = $('.product-main-image img').attr('src');
        const productGdsNo = typeof currentProductGdsNo !== 'undefined' ? currentProductGdsNo : null;
        if (selectedOptions.length === 0) {
            alert('구매하시려면 최소 하나 이상의 옵션을 선택해주세요.');
            return;
        }
        if (confirm('선택하신 상품을 바로 구매하시겠습니까?')) {
            let buyNowData = [];
            selectedOptions.forEach(option => {
                const uniqueItemId = `${productName}_${option.color}_${option.size}`;
                const basePrice = getBaseProductPrice();
                buyNowData.push({
                    id: uniqueItemId,
                    gdsNo: productGdsNo,
                    name: `${productName} ${option.color} ${option.size}`,
                    price: basePrice,
                    quantity: option.quantity,
                    image: productImageSrc,
                    color: option.color,
                    size: option.size
                });
            });
            sessionStorage.setItem('buy_now_data', JSON.stringify(buyNowData));
            window.location.href = '/checkout';
        } else {
            console.log("구매하기 취소됨.");
        }
    });

    /* =================================================================== */
    /* ⭐ [새로 추가] 비슷한 상품 슬라이더 초기화 ⭐ */
    /* =================================================================== */
    const similarSwiperContainer = document.querySelector('.weekly-best-swiper');
    if (similarSwiperContainer) {
        const similarProductsSwiper = new Swiper(similarSwiperContainer, {
            loop: false,
            autoplay: false,
            slidesPerView: 'auto',
            spaceBetween: 1,
            navigation: {
                nextEl: '.swiper-button-next',
                prevEl: '.swiper-button-prev',
            },
        });
    }
    
    const gallery = document.querySelector('.product-detail-image-gallery');
    if (gallery) {
        gallery.addEventListener('wheel', function(event) {
            if (event.deltaY !== 0) {
                event.preventDefault();
                window.scrollBy(0, event.deltaY);
            }
        });
        let isDown = false;
        let startY;
        let scrollTop;
        gallery.addEventListener('mousedown', (e) => {
            isDown = true;
            gallery.classList.add('active');
            startY = e.pageY - gallery.offsetTop;
            scrollTop = gallery.scrollTop;
        });
        gallery.addEventListener('mouseleave', () => {
            isDown = false;
            gallery.classList.remove('active');
        });
        gallery.addEventListener('mouseup', () => {
            isDown = false;
            gallery.classList.remove('active');
        });
        gallery.addEventListener('mousemove', (e) => {
            if (!isDown) return;
            e.preventDefault();
            const y = e.pageY - gallery.offsetTop;
            const walk = (y - startY) * 2;
            gallery.scrollTop = scrollTop - walk;
        });
    }
    console.log("페이지 로드 시 모든 스크립트 초기화 완료.");
});