# Constella - 나만의 별자리 지도

## 프로젝트 소개
Constella는 사용자가 방문한 국가들을 3D 지구본 위에 핀으로 표시하고, 이 핀들을 연결하여 자신만의 별자리를 만드는 웹 애플리케이션입니다. 통계 기능을 통해 방문 국가 정보를 시각적으로 확인할 수 있으며, 별자리 저장 및 보기 기능을 제공합니다.

## 주요 기능
-   **3D 지구본 시각화**: Three.js와 ThreeGlobe를 활용하여 사용자 친화적인 3D 지구본을 제공합니다.
-   **방문 국가 표시**: 사용자가 방문한 국가들을 지구본 위에 핀으로 표시합니다.
-   **별자리 생성 및 보기**: 방문한 국가 핀들을 연결하여 사용자만의 별자리를 생성하고 시각적으로 보여줍니다.
-   **별자리 저장**: 생성된 별자리를 저장하여 나중에 다시 볼 수 있습니다.
-   **통계 대시보드**: 총 방문 국가 수, 최다 방문 국가 등 사용자별 방문 통계를 제공합니다.
-   **나라 추가/관리**: 사용자가 방문 국가를 추가하고 관리할 수 있습니다.
-   **사용자 인증**: 로그인/로그아웃 기능을 통해 사용자별 맞춤 경험을 제공합니다.

## 기술 스택

### 프론트엔드
-   **React**: UI 개발을 위한 JavaScript 라이브러리
-   **Three.js**: 3D 그래픽 렌더링
-   **ThreeGlobe**: Three.js 기반 지구본 시각화 라이브러리
-   **React Router Dom**: 클라이언트 측 라우팅
-   **CSS / Inline Styles**: 스타일링
-   **Fetch API**: 백엔드 API 통신
-   **Local Storage**: 사용자 세션 데이터(userId) 관리

### 백엔드
-   **Spring Boot**: RESTful API 서버 구축 (Java 기반)
-   **Spring Security**: API 보안 및 인증/인가 (추정)
-   **데이터베이스**: 데이터 저장 및 관리 (구체적인 종류는 언급되지 않았으나 관계형 DB로 추정)

## 설치 및 실행 방법

### 1. 개발 환경 설정
-   Node.js (LTS 버전 권장) 및 npm 또는 Yarn이 설치되어 있어야 합니다.
-   백엔드 환경(Java, Spring Boot) 설정이 필요합니다.

### 2. 프로젝트 클론
```bash
git clone [프로젝트_레포지토리_URL]
cd Constella_front
```

### 3. 프론트엔드 설치 및 실행
```bash
# 의존성 설치
npm install
# 또는 yarn install

# 개발 서버 실행
npm start
# 또는 yarn start
```
프로젝트가 `http://localhost:3000` (기본값)에서 실행됩니다.

### 4. 백엔드 설정 및 실행 (별도 안내 필요)
백엔드 저장소에 대한 정보는 이 README에 포함되어 있지 않습니다. 백엔드를 실행하려면 해당 저장소의 지침을 따르십시오. 일반적으로 다음과 같은 단계가 필요합니다:
-   Java 개발 환경 설정 (JDK 11 이상 권장)
-   Maven 또는 Gradle을 이용한 의존성 설치
-   Spring Boot 애플리케이션 실행 (예: `java -jar your-app.jar` 또는 IDE에서 실행)
-   **API 통신을 위해 백엔드 서버가 `http://localhost:8080`에서 실행 중이어야 합니다.**

### 5. 폰트 임포트
`public/index.html` 파일의 `<head>` 섹션에 다음 폰트 링크가 포함되어 있는지 확인하세요:
```html
<link href="https://fonts.googleapis.com/css2?family=Montserrat:wght@400;600;700&display=swap" rel="stylesheet">
```

### 6. CSS 호버 효과
버튼 호버 효과를 위해 `src/styles/TopBar.css` 파일이 존재하고 `GlobeViewer.jsx`에 임포트되었는지 확인하세요.
`src/styles/TopBar.css` 예시:
```css
.top-bar-button:hover {
  color: #FFD700 !important;
  text-shadow: 0 0 10px rgba(255, 215, 0, 0.7);
  transform: translateY(-3px);
  cursor: pointer;
}
```

## API 엔드포인트 (프론트엔드에서 사용)
-   `http://localhost:8080/api/countries`: 국가 목록 조회
-   `http://localhost:8080/api/stats/summary/{userId}`: 사용자별 통계 요약 조회
-   `http://localhost:8080/api/stats/by-country/{userId}`: 사용자별 국가별 통계 조회
-   `http://localhost:8080/api/constellation/saved/{userId}`: 사용자 저장 별자리 핀 조회
-   `http://localhost:8080/api/constellation/save`: 별자리 저장 (POST)
-   `http://localhost:8080/api/constellation/history/{userId}`: 별자리 히스토리 조회 (아직 백엔드 구현 확인 필요)
-   (로그인/회원가입 관련 API는 별도 명시되지 않았으나, `/login` 라우팅으로 보아 존재할 것으로 추정)

## 알려진 문제 및 향후 개선 사항
-   `GlobeViewer.jsx`의 상단 바 UI가 브라우저 캐싱 등으로 인해 즉시 반영되지 않을 수 있습니다. 강제 새로고침(캐시 지우기 포함) 및 개발 서버 재시작이 필요할 수 있습니다.
-   백엔드 `ConstellationController`의 `/api/constellation` POST 요청 404 에러는 백엔드 라우팅 또는 Spring Security 설정 문제일 가능성이 높습니다.
-   별자리 히스토리 API (`/api/constellation/history/{userId}`)가 현재 빈 데이터를 반환하고 있으며, 데이터 저장 로직 및 DB 스키마, 서비스 로직에 대한 백엔드 검토가 필요합니다.
-   **로고 이미지 교체**: `public/images/constella_logo.png` 경로에 이미지를 저장하고 `GlobeViewer.jsx`의 `src` 속성을 변경해야 합니다.

---
이 README는 프로젝트의 현재 상태와 제가 파악한 정보를 기반으로 작성되었습니다. 필요에 따라 내용을 추가하거나 수정해주십시오. 
