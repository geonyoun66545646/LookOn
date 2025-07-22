/**
 * 
 */


$(() => {
		    const salesChartCanvas = $("#salesChart");
		
		    if (salesChartCanvas.length) {
				const monthlyLabels = window.monthlyLabels;
		        const revenueData = window.revenueData;
		        const totalSalesData = window.totalSalesData;
		        
		        const monthlyAverageRevenueData = window.monthlyAverageRevenueData;
		        const monthlyAverageTotalSalesData = window.monthlyAverageTotalSalesData;
		        
		        // 데이터가 없는 경우를 대비한 방어 코드: 차트를 그릴 데이터가 없으면 함수 종료
		        if (!monthlyLabels || monthlyLabels.length === 0) {
		            console.warn("월별 통계 차트를 그릴 데이터가 없습니다.");
		            return; 
		        }
		
		        const ctx = salesChartCanvas[0].getContext('2d');
		        
		        const salesChart = new Chart(ctx, {
		            type: 'line',
		            data: {
		                labels: monthlyLabels,
		                datasets: [
		                    {
		                        label: '수수료 수익',
		                        data: revenueData,
		                        backgroundColor: 'rgba(25, 135, 84, 0.1)',
		                        borderColor: 'rgba(25, 135, 84, 1)',
		                        borderWidth: 2,
		                        fill: true,
		                        tension: 0.3,
		                        hidden: false, // 기본으로 보이게
		                        pointRadius: 4,      // 기본 점 크기 (기존 0이었다면 이 값을 주어 보이게 함)
		                        pointBackgroundColor: 'rgba(25, 135, 84, 1)', // 점 내부 색상
		                        pointBorderColor: 'rgba(255, 255, 255, 0.8)', // 점 테두리 색상
		                        pointHoverRadius: 6, // 호버 시 점 크기
		                        pointHoverBackgroundColor: 'rgba(25, 135, 84, 1)',
		                        pointHoverBorderColor: 'rgba(255, 255, 255, 1)'
		                    },
		                    {
		                        label: '총 판매액',
		                        data: totalSalesData,
		                        backgroundColor: 'rgba(13, 110, 253, 0.1)',
		                        borderColor: 'rgba(13, 110, 253, 1)',
		                        borderWidth: 2,
		                        fill: true,
		                        tension: 0.3,
		                        hidden: true, // 기본으로 숨기기
		                        pointRadius: 4,
		                        pointBackgroundColor: 'rgba(13, 110, 253, 1)',
		                        pointBorderColor: 'rgba(255, 255, 255, 0.8)',
		                        pointHoverRadius: 6,
		                        pointHoverBackgroundColor: 'rgba(13, 110, 253, 1)',
		                        pointHoverBorderColor: 'rgba(255, 255, 255, 1)'
		                    },
		                    {
		                        label: '수수료 수익 평균',
		                        data: monthlyAverageRevenueData,
		                        backgroundColor: 'transparent', // 채우기 없음
		                        borderColor: 'rgba(255, 99, 132, 0.8)', // 붉은색 계열로 구분
		                        borderWidth: 1,
		                        fill: false, // 영역 채우기 없음
		                        tension: 0, // 직선으로 표시
		                        borderDash: [5, 5], // 점선으로 표시
		                        pointRadius: 0, // 점 표시 안 함
		                        hidden: false // 기본으로 보이게 (원한다면 hidden: true)
		                    },
		                    {
		                        label: '총 판매액 평균',
		                        data: monthlyAverageTotalSalesData,
		                        backgroundColor: 'transparent',
		                        borderColor: 'rgba(75, 192, 192, 0.8)', // 청록색 계열로 구분
		                        borderWidth: 1,
		                        fill: false,
		                        tension: 0,
		                        borderDash: [5, 5],
		                        pointRadius: 0,
		                        hidden: true // 기본으로 숨김 (원한다면 hidden: false)
		                    }
		                ]
		            },
		            options: {
		                responsive: true,
		                maintainAspectRatio: false, // 부모 컨테이너 크기에 맞춰 유연하게 조절
		                plugins: {
		                    legend: { // 범례 (데이터셋이 여러 개일 때 자동으로 생성됨)
		                        display: true, // 범례를 보이게 설정 (기본값)
		                        position: 'bottom', // 범례 위치를 차트 아래로 옮김
		                        align: 'start',    // 범례를 왼쪽으로 정렬
		                        labels: {
		                            boxWidth: 20, // 범례 색상 박스 크기 조절
		                            padding: 15,  // 범례 항목 간의 간격
		                            font: {
		                                size: 12   // 범례 텍스트 크기
		                            }
		                        }
		                    },
		                    onClick: (e, legendItem, legend) => {
		                        const chart = legend.chart;
		                        const clickedIndex = legendItem.datasetIndex;
		                        const meta = chart.getDatasetMeta(clickedIndex);

		                        // 1. 연결된 데이터셋 인덱스 설정 (수수료 수익 + 평균 / 총 판매액 + 평균)
		                        let linkedDatasets = [];
		                        if (clickedIndex === 0 || clickedIndex === 2) { // 수수료 관련
		                            linkedDatasets = [0, 2];
		                        } else if (clickedIndex === 1 || clickedIndex === 3) { // 판매액 관련
		                            linkedDatasets = [1, 3];
		                        }

		                        // 2. 연결된 데이터셋들의 가시성 토글
		                        if (linkedDatasets.length > 0) {
		                            const isHidden = !meta.hidden; // 현재 상태의 반대값으로 설정
		                            linkedDatasets.forEach(idx => {
		                                chart.setDatasetVisibility(idx, isHidden);
		                            });
		                            chart.update();
		                        } else {
		                            // 기본 동작 (다른 데이터셋이 추가된 경우)
		                            meta.hidden = !meta.hidden;
		                            chart.update();
		                        }
		                    },        
		                    tooltip: { // 툴팁 설정
		                        callbacks: {
		                            label: (context) => {
		                                let label = context.dataset.label || '';
		                                if (label) { label += ': '; }
		                                
		                                // 현재 값 포맷팅
		                                const currentValue = context.parsed.y;
		                                label += new Intl.NumberFormat('ko-KR').format(currentValue) + '원';

		                                // 이전 데이터셋 값 가져오기 (성장률 계산)
		                                const currentIndex = context.dataIndex;
		                                if (currentIndex > 0) { // 첫 번째 데이터가 아닐 경우에만 계산
		                                    const previousValue = context.dataset.data[currentIndex - 1];
		                                    
		                                    if (previousValue !== 0) { // 0으로 나누는 것 방지
		                                        const change = currentValue - previousValue;
		                                        const percentageChange = (change / previousValue * 100).toFixed(2); // 소수점 두 자리

		                                        let changeText = '';
		                                        if (change > 0) {
		                                            changeText = ` (+${percentageChange}%)`; // 증가
		                                        } else if (change < 0) {
		                                            changeText = ` (${percentageChange}%)`; // 감소
		                                        } else {
		                                            changeText = ` (0%)`; // 변화 없음
		                                        }
		                                        label += changeText;
		                                    } else if (currentValue !== 0) {
		                                        // 이전 값이 0인데 현재 값이 있으면 무한대 증가로 표시
		                                        label += ` (∞%)`; 
		                                    }
		                                }
		                                return label;
		                            }
		                        }                 
		                    },
		                    title: {
		                        display: true,
		                        text: '월별 수익 통계', // 메인 제목
		                        font: {
		                            size: 16,
		                            weight: 'bold'
		                        },
		                        padding: {
		                            top: 10,
		                            bottom: 20
		                        }
		                    },
		                    // 4. 차트 부제목 (Subtitle) 설정 - legend, tooltip, title과 같은 레벨입니다.
		                    subtitle: {
		                        display: true,
		                        text: '정산 완료 건 기준 (최근 6개월)', // 부제목
		                        font: {
		                            size: 12,
		                            weight: 'normal'
		                        },
		                        padding: {
		                            bottom: 10
		                        }
		                    }
		                },
		                scales: { // 축 설정
		                    y: {
		                    	beginAtZero: true,
		                        ticks: {
		                            callback: (value) => {
		                                // 1억원 이상은 '억' 단위로 표시 (선택 사항)
		                                if (value >= 100000000) {
		                                    return (value / 100000000).toLocaleString() + '억원';
		                                }
		                                // 1만원 이상은 '만원' 단위로 표시
		                                if (value >= 10000) {
		                                    return (value / 10000).toLocaleString() + '만원';
		                                }
		                                // 그 외는 일반 원 단위
		                                return new Intl.NumberFormat('ko-KR').format(value) + '원';
		                            },
		                            maxTicksLimit: 6 // y축 눈금 개수 제한 (데이터 밀집도에 따라 조절)
		                        },
		                        grid: { // Y축 그리드 라인 스타일
		                            color: 'rgba(0, 0, 0, 0.08)', // 더 연한 회색
		                            borderColor: 'rgba(0, 0, 0, 0.15)', // 축 라인 색상
		                            lineWidth: 1
		                        }
		                    },
		                    x: {
		                    	grid: {
		                            color: 'rgba(0, 0, 0, 0.08)',
		                            borderColor: 'rgba(0, 0, 0, 0.15)',
		                            lineWidth: 1
		                        }
		                    }
		                }
		            }
		        });
		    }
		});