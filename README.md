# LookOn 목차

- [프로젝트 소개](#프로젝트-소개)
- [팀원 소개](#팀원-소개)
- [프로젝트 일정](#프로젝트-일정)
- [기술 스택](#기술-스택)
- [개발 환경](#개발-환경)
- [주요 기능 설명](#주요-기능-설명)
- [역할 분담](#역할-분담)
- [페이지별 기능](#페이지별-기능)
- [프로젝트 후기](#프로젝트-후기)

---

## 프로젝트 소개

### 기획 배경

- 소셜 미디어에 맞춰, 사용자 일상 및 패션 공유를 위한 신규 커머스 모델을 기획했습니다.
- 사용자 생성 콘텐츠(UGC)와 소셜 미디어 서비스를 결합한 구매 유도 커머스를 추진합니다.

### 기획 목적

- 사용자 소셜 활동 기반 패션, 라이프스타일 콘텐츠를 제공하고 혁신적인 프로모션 방안을 구현하는 것이 목표입니다.
- 플랫폼 내 패션 스타일 평가 및 피드백 기능을 제공하여 소통 증진 및 교류를 촉진합니다.

### 기대 효과

- 플랫폼 내 콘텐츠가 이슈화, 타 SNS 및 커뮤니티로 확산되어 바이럴 홍보 효과를 창출합니다.
- 이용자 수 증가 시, 인플루언서 및 유명 브랜드 유입으로 플랫폼 가치 및 시장 영향력을 향상시킬 수 있습니다.
- 인플루언서 및 브랜드 유입 증가는 다양한 광고 및 전략적 협업 모델 개발로 이어져, 플랫폼 수익 모델을 견고하게 다질 것으로 기대합니다.
- 바이럴 홍보 효과와 신규 신규 마케팅 수단으로서의 성장을 통해, 관련 시장 내 선도적 영향력을 확대하고 확고한 입지를 구축할 것입니다.

---

## 팀원 소개

| 이름 | 역할 | 이메일 |
| :--- | :--- | :--- |
| 김성주 | 팀원 1 | nmzxcvb1234@naver.com |
| 서요셉 | 팀원 2 | sisi331@naver.com |
| 염가은 | 팀원 3 | tayonim@naver.com |
| 이건영 | 팀장 | qaz9621@naver.com |
| 이지성 | 팀원 4 | lgsd3@naver.com |
| 정성훈 | 팀원 5 | 96f22l@gamil.com |

---

## 프로젝트 일정

- **프로젝트 기간**: 2025년 2월 24일 ~ 2025년 8월 6일

### 주요 개발 목표

| 기간 | 주요 목표 |
| :--- | :--- |
| 2025.02.24 ~ 2025.05.19 | **기획 및 분석** <br> (팀원 구성, 역할 분담, 주제 선정, 기능 정의, UI/UX 기획 및 DB 설계) |
| 2025.02.25 ~ 2025.06.01 | **개발 환경 구축** <br> (서버, DB, API 등 개발 환경 설정) |
| 2025.02.25 ~ 2025.08.06 | **주요 기능 구현** <br> (각 팀원별 담당 기능 및 페이지 구현) |
| 2025.07.23 ~ 2025.08.06 | **테스트 및 배포** <br> (버그 수정, 성능 최적화, 배포 및 최종 발표 준비) |

---

## 기술 스택

### **Backend**

![Spring Boot](https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
- SpringBoot, Thymeleaf를 이용하여 웹 어플리케이션 팀프로젝트 구현 경험
- Service 인터페이스에 추상 메서드와 구현 클래스로 구성하여 작성한 경험 있음
- RestTemplate / webClient를 활용해 외부 API를 비동기적인 처리를 Service에서 처리하여 그 결과를 Client로 보내는 기능을 구현

![Java](https://img.shields.io/badge/Java-007396?style=for-the-badge&logo=java&logoColor=white)
- Java 문법을 활용하여 팀프로젝트 기능 구현
- 메서드 선언 및 호출, 객체 생성, 클래스, 메서드 오버로딩, 메서드 오버라이딩 등 활용

![MyBatis](https://img.shields.io/badge/MyBatis-000000?style=for-the-badge&logo=mybatis&logoColor=white)
- SQL 매핑 구문 작성 가능
- ResultMap과 assocation을 화용하여 도메인 클래스와 연결하고 DTO와 연결하여 사용 가능
- 동적 SQL 구현 경험

**MVC**
- SpringBoot를 활용하여 팀프로젝트 기능 구현
- MVC 패턴을 이해하고 Controller-Service-DTO-Mapper-DB를 활용하여 팀프로젝트 기능 구현
- 구현 과정에서 Controller에서 Model 객체에 전달할 데이터를 담아 View로 전달하거나 Responsbody로 Post 혹은 Get 방식으로 비동기 통신으로 객체 전달 경험

---

### **Frontend**

![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
- 화면을 분할하고 구조화하여 구현
- 블럭요소(table, form, section, div 등)와 인라인 요소(a, span, button, input, label)의 적절한 배치 및 활용

![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
- 팀프로젝트 기능 구현 시 스타일의 다양한 속성을 활용하여 HTML 요소에 적용해 적재적소에 활용 경험

![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
- jQuery와 혼용하여 function으로 웹 동작 구현
- 반복문을 통한 배열, 객체 활용
- 유효성 검사(정규식과 조건문을 통한 요소 제어, 이벤트 처리, Ajax 통신 경험
- ES6 문법과 그 이전 문법을 분리하여 각각 개인적으로 실습하여 적용함

![jQuery](https://img.shields.io/badge/jQuery-0769AD?style=for-the-badge&logo=jquery&logoColor=white)
- JavaScript와 같이 혼용하여 이벤트 사용 가능
- CSS 선택자를 활용하여 다양한 이벤트 구현 경험

![Bootstrap](https://img.shields.io/badge/Bootstrap-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)
- 복수의 부트스트랩 템플릿을 팀프로젝트에 적용하여 CSS, 자바스크립트 활용 경험

![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
- SpringBoot, Thymeleaf를 이용하여 웹 어플리케이션 팀프로젝트 구현 경험 있음
- 팀프로젝트 기능 구현 중 Thymleaf 문법(th:href, th:value, th:each, th:if, th:with 등)을 활용 경험

![AJAX](https://img.shields.io/badge/AJAX-000000?style=for-the-badge)
- 팀프로젝트에서 카카오 맵 API에 DB의 여행지 상세 정보를 JSON형태로 전달하거나 지역 코드에 해당하는 하위의 시군 코드를 Ajax를 활용하여 화면에 동적으로 출력한 경험 등 비동기 통신이 필요한 곳에서 get, post 방식을 변경해가며 활용한 경험

---

### **Database**

![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
- 팀프로젝트를 통해 DB 구축 당시 메인 DBMS로 사용해 경험
- DB 생성 및 일반 사용자 계정 생성, DB에 접근 권한 부여, 테이블 생성 등의 DDL 활용 경험
- INSERT, SELECT, UPDATE, DELETE의 DML 쿼리문 작성 및 활용 가능
- 프로시저와 사용자 정의 함수 사용 경험

![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
- DB 생성 및 일반 사용자 계정 생성, DB에 접근 권한 부여, 테이블 생성 등의 DDL 활용 가능
- INSERT, SELECT, UPDATE, DELETE의 DML 쿼리문 작성 및 활용 가능
- SQL 내장 함수와 JOIN을 활용한 SQL문 작성 가능
- 프로시저와 사용자 정의 함수 사용 경험

---

### **Deployment & Collaboration**

![Linux](https://img.shields.io/badge/Linux-FCC624?style=for-the-badge&logo=linux&logoColor=black)
- 리눅스 서버 호스팅을 통한 팀프로젝트 파일 배포 환경 구축
- 리눅스 활용한 환경 변수 설정 가능
- 운영서버 배포 환경 : IaaS (Infrastructure as a Service) 종류 중 OCI ( Oracle Cloud Infrastructure )와 AWS 중 OCI 선정
- cent OS를 이용해 github action을 이용한 팀프로젝트 파일 배포 환경 구축
- ubuntu 22.04 서버 호스팅을 통한 팀프로젝트 파일 배포 환경 구축
- SSH, SFTP를 활용해 팀 프로젝트 환경 구축 

![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)
- 팀프로젝트 수행 시 팀원들과 협업하여 팀 프로젝트 형상 관리 경험 있음
- Main, Develop, 팀원의 개인 Branch를 생성해 각자 맡은 기능을 구현하며 Commit/Push 후 Develop에 PullRequest하여 Merge하면서 체계적으로 gitFlow 형상 관리 경험

![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=github-actions&logoColor=white) **CI/CD**
- GitHub Actions를 활용하여 CI/CD 파이프라인 구축 경험
- main 브랜치에 Push 또는 Pull Request 발생 시 자동으로 빌드, 테스트, 배포가 이루어지도록 설정하여 개발 생산성을 향상시킴

---

### **Framework & Tools**

![Apache Maven](https://img.shields.io/badge/Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)
- 팀프로젝트 진행 시 SpringBoot에서 pom.xml 파일을 이용해 dependency를 추가하고 삭제하여 라이브러리를 관리 경험
- 팀프로젝트 진행 시 프로젝트 빌드 경험

![Eclipse](https://img.shields.io/badge/Eclipse-2C2255?style=for-the-badge&logo=eclipse&logoColor=FF8000) ![Visual Studio Code](https://img.shields.io/badge/VSCode-007ACC?style=for-the-badge&logo=visual-studio-code&logoColor=white)
- Eclipse, VSCode 등 기초 개발환경 세팅 및 Dynamic Web Project 생성 경험
- 라이브러리 및 플러그인 활용

---

## 개발 환경

| 구분 | 개발 환경 | 서비스 환경 |
| :--- | :--- | :--- |
| **OS** | Windows 10 | Linux - CentOS 8 |
| **WAS** | Apache Tomcat 10.1.30 | Apache Tomcat 10.1.30 |
| **웹 브라우저** | Chrome, Microsoft Edge | Chrome, Safari, Microsoft Edge, Firefox |
| **개발 언어** |
| 서버 | JDK 17 / Servlet 6.x / UTF-8 | NGINX 1.29.0 |
| 클라이언트 | JavaScript / HTML5 / CSS / Thymeleaf / jQuery | - |
| **Framework** | Bootstrap 5 / Spring Framework 6.x / Mybatis 3.x | - |
| **DBMS** | MySQL 8.0.26 | MySQL 8.0.26 |
| **소프트웨어 개발도구** | JDK 17 | OpenJDK 17.x |
| **통합 개발도구** | Eclipse, Spring Tool Suite, Visual Studio Code | CI/CD (GitHub Actions) |

---

## 주요 기능 설명

- **LookOn** 프로젝트의 주요 기능은 사용자 유형에 따라 다음과 같이 구성됩니다.

---

### **관리자 (Administrator)**

- **회원 관리**: 사용자 및 판매자 계정 관리, 제재 및 승인 기능
- **콘텐츠 관리**: 부적절한 게시물 및 댓글 모니터링 및 삭제
- **사이트 통계**: 회원 가입 현황, 판매 실적 등 전반적인 서비스 현황 파악
- **결제 및 정산 관리**: 판매자와의 정산 내역 및 수수료 관리

---

### **판매자 (Seller)**

- **상품 등록 및 관리**: 상품 정보, 재고, 가격 등을 등록하고 수정
- **상점 페이지 관리**: 판매자의 브랜드 페이지를 커스터마이징하고 운영
- **판매 대시보드**: 상품별 판매량, 수익 등 실적을 한눈에 확인
- **고객 응대**: 상품 관련 문의에 답변하고 고객과 소통

---

### **사용자 (User)**

- **소셜 피드**: 다른 사용자가 올린 패션 콘텐츠를 둘러보고 '좋아요' 및 댓글로 소통
- **구매 기능**: 마음에 드는 상품을 장바구니에 담고 결제
- **개인 프로필**: 자신만의 패션 콘텐츠를 업로드하고 공유
- **검색 및 필터**: 카테고리, 해시태그, 키워드 등을 활용하여 원하는 콘텐츠 및 상품을 탐색
- **마이페이지**: 주문 내역, 좋아요 목록, 팔로우 현황 등을 확인

---

## 역할 분담

| 담당 | 주요 기능 |
| :--- | :--- |
| **팀원 전체** | DB 설계 및 구축, 개발환경 구축 |
| **김성주** | 회원가입/로그인 시스템 개발, 사용자 정보 수정 및 저장 기능 개발 |
| **정성훈** | 상품 정보 관리 시스템 개발, 상품 승인/상태 관리 시스템 개발 |
| **이건영** | 결제 시스템, 환불/정산 시스템 |
| **이지성** | 상점 시스템, 고객 서비스, 리뷰 시스템 |
| **서요셉** | 게시글/게시판, 피드 시스템 |
| **염가은** | 쿠폰 시스템 개발, 신고/제재 시스템 개발, 검색 시스템 개발 |

---

## 페이지별 기능

### **사용자 (Customer)**

- **회원가입 및 로그인**
    - 로그인 및 회원가입 모달창
- **마이페이지**
    - 마이 페이지, 회원 정보수정, 프로필 저장, 비밀번호 변경, 회원 탈퇴
- **메인페이지**
    - 메인페이지 화면
    - 카테고리별 조회 모달 기능
    - 상품 유형별 슬라이드 기능
- **상품페이지**
    - 유형별 상품 목록 화면
    - 브랜드 상품 목록 화면
    - 상품 상세 화면
    - 상품 옵션별 조회 기능
- **커뮤니티**
    - 커뮤니티 게시판 목록 및 상세 페이지
    - 게시글 등록/수정 페이지
- **피드**
    - 피드 목록 및 상세 페이지
    - 마이 피드 페이지
    - 피드 등록 페이지
- **쇼핑**
    - 장바구니
    - 결제 페이지 (toss api 연동)
    - 결제 내역 및 배송 조회
- **문의 및 상점 신청**
    - 자주 묻는 질문(FAQ), 문의 리스트 및 등록 페이지
    - 상점 신청 페이지
- **쿠폰 및 검색**
    - 사용자 쿠폰 페이지
    - 검색 결과 페이지
- **신고**
    - 신고 접수 및 내 신고 내역 조회 페이지

### **판매자 (Seller)**

- **로그인**
    - 판매자 페이지 로그인
- **상품 관리**
    - 상품 목록 조회, 등록, 수정, 삭제
- **상점 관리**
    - 상점 관리 페이지 메인
    - 상점 문의 목록 및 관리 페이지

### **관리자 (Administrator)**

- **로그인**
    - 관리자 페이지 로그인
- **대시보드 및 통계**
    - 관리자 대시보드
    - 회원 정보 조회, 수정 및 로그인 기록 조회
- **상품 관리**
    - 판매자가 제출한 상품 승인/반려 관리
    - 카테고리 관리 (조회, 등록, 수정, 삭제)
- **커뮤니티 및 피드 관리**
    - 게시판, 게시글, 피드 관리 페이지
    - 공지사항/이벤트 작성 페이지
- **스토어 관리**
    - 구독권 등록, 수정, 삭제
    - 스토어 정산
    - 상점 신청 관리 및 목록 조회
    - 리뷰 목록 조회
- **문의 및 신고 관리**
    - 문의 목록 및 관리 페이지
    - 신고 목록 및 신고 상세 처리 페이지
- **부가 기능 관리**
    - 쿠폰 생성, 회원 쿠폰 조회, 쿠폰 목록 조회
    - 검색 기록 조회

---

## 프로젝트 후기

- 김성주 : 처음 구상했던 목표에 비해 완성도가 다소 떨어진 부분은 다음 프로젝트에서 반드시 개선해야 할 점이라고 생각합니다. 하지만 이러한 경험을 통해 현실적인 목표 설정과 시간 관리의 중요성을 다시 한번 깨달을 수 있었습니다. 이번 프로젝트는 매우 즐거운 경험이었습니다. 팀원들과 협력하며 하나의 목표를 향해 나아가는 과정은 매우 의미 있었고, 덕분에 프로젝트를 성공적으로 완수할 수 있었습니다. 서로의 아이디어를 공유하고, 어려운 문제를 함께 해결해나가면서 협업의 가치와 시너지를 온몸으로 느낄 수 있었습니다.

- 서요셉 : 피드와 커뮤니티 시스템을 개발하면서 사용자 경험(UX)과 기술적 구현 사이의 균형을 맞추는 것이 얼마나 중요한지 배웠습니다. 단순히 기능을 구현하는 것을 넘어, 사용자가 직관적으로 게시물을 작성하고 탐색할 수 있도록 UI/UX 측면을 함께 고려하며 개발하는 경험이 큰 도움이 되었습니다.
  
- 염가은 : 사용자의 입장에서만 사용하던 기능을 관리자의 입장에서도 생각해 보고 프로세스를 새롭게 이해할 수 있었습니다. 팀프로젝트의 구상 및 설계 단계부터 최종 구현과 배포까지 진행하면서 개발자의 관점에서 어떻게 설계해야 하는지, 어떤 방식으로 구현해야 하는지 고민하면서 개발에 필요한 사고 능력과 역량을 키울 수 있는 값진 경험이었습니다.

- 이건영 : 팀 프로젝트를 통해 결제 시스템을 개발하며 API 연동을 처음 경험했습니다. 정보를 요청하고 응답을 받는 과정을 직접 익히면서, API가 개발 시간을 크게 줄여주고 시스템의 유연성과 확장성을 높여준다는 것을 깨달았습니다. 앞으로도 이러한 경험을 바탕으로 API와 같은 기능들을 사용해 보다 좋은 개발자가 될 수 있는데 도움이 되었습니다.

- 이지성 : 이번 프로젝트를 통해 설계 단계에서의 명확한 규칙 수립이 구조적 통일성을 확보하는 데 매우 중요하다는 것을 깨달았습니다. 또한, 개발자의 관점을 넘어 실제 사용자의 편의성을 고려한 설계의 중요성도 다시 한번 상기할 수 있었습니다. 특히, 팀원들과의 활발한 소통과 협력이 성공적인 결과물을 만드는 데 필수적이며, 서로 배려하며 함께 노력하는 과정이 훌륭한 프로젝트를 완성할 수 있는 소중한 경험이었습니다.

- 정성훈 : 이번 프로젝트에서 상품 등록, 상품 승인, 상품 노출에 이르는 일련의 프로세스를 담당하며, 각 권한별 사용자의 입장에서 서비스를 개발하는 것의 중요성을 절실히 깨달았습니다. 개발자 관점이 아닌 사용자 관점을 고려했다고 생각했지만, 실제로 사용해보니 여전히 개발자 중심의 구조가 남아있는 부분이 있었습니다. 이번 경험을 통해 앞으로는 사용자의 입장을 최우선으로 고려하여 더욱 완성도 높은 프로세스를 개발할 수 있을 것 같습니다.

