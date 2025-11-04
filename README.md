# 🚗 착행 (ChakHaeng)

> **OnDevice AI를 활용한 교통 법규 위반 탐지 및 신고 자동화 서비스**  
휴대폰 카메라만으로 교통법규 위반을 인식하고 신고서를 자동 생성하는 **온디바이스 AI 기반 안전운전 도우미**

**기간:** 2025.08.25 ~ 2025.10.02 / **팀:** 모바일 2명, 백엔드 1명, AI 3명 (총 6명)  
**수상:** 🏅 SSAFY 특화 프로젝트 우수상  

---

## 🗂 프로젝트 개요

- **목표**
  - 온디바이스 AI를 통해 **교통법규 위반을 실시간 탐지**하고, **국민신문고 신고를 자동화**하여 신고 부담 최소화
  - **네트워크 연결 없이도 동작하는 경량 추론 구조**로 현장 즉시성·보안성·신뢰성 확보
  - 영상·위치·차량번호 데이터를 자동 수집·정리하여 **신속하고 정확한 신고 생태계** 구축  

- **배경**
  - 교통법규 위반은 사고의 주요 원인이지만, **촬영·편집·신고 과정이 번거로워 신고율이 낮음**
  - AI가 단말 내부에서 실시간으로 감지·작성·제출까지 처리하면 시민 참여형 교통 감시 가능
  - 클라우드 추론의 지연·비용 문제를 해결하기 위해 **TFLite 기반 온디바이스 추론 구조**로 설계  

---

## 🧱 기술 스택

- **Mobile:** Kotlin, Jetpack Compose(Material3), Hilt, Room, Retrofit2, Coroutines, Flow, Media3(ExoPlayer), WorkManager, Credential Manager, FCM  
- **Server:** Java, Spring Boot, Spring Security, JPA, JWT, AWS S3 Presigned URL, Swagger, Jenkins CI/CD  
- **AI:** Python, PyTorch, TensorFlow Lite, OpenCV, ONNX, YOLOv8  
- **Database:** PostgreSQL, Redis  
- **Infra / Tools:** GitLab, Docker, Nginx, AWS EC2, Jenkins, Figma, Jira, Notion, Android Studio, JupyterLab  

---

## 🧩 주요 기능

### 1) AI 실시간 위반 탐지  
- YOLOv8 기반 객체 인식으로 **신호위반·차선침범·중앙선침범·이륜차 인도주행** 감지  
- CameraX 프레임을 OpenCV로 전처리 후 TFLite 추론, 0.1~0.2초 내 실시간 판별  

### 2) 자동 신고 생성  
- 탐지 결과의 차량번호·GPS·시간 데이터를 국민신문고 양식에 자동 매핑  
- 사용자 검토 후 바로 제출 가능한 **반자동 신고 절차** 구현  

### 3) 영상 업로드 및 증거 관리  
- Presigned S3 URL 기반 안전 업로드 파이프라인  
- 업로드 완료 후 서버 콜백 POST로 상태 반영, 중단 시 자동 재시도  

### 4) 실시간 알림 및 기록  
- 탐지 즉시 FCM 푸시 전송, Room+Flow 기반 실시간 UI 동기화  

### 5) 통계 및 게이미피케이션  
- 탐지 횟수, 위반 유형 비율, 지역 분포 시각화  
- 미션·뱃지 시스템으로 사용자 참여 유도  

---

## 🏗 시스템 아키텍처

```
CameraX → TFLite Inference → ViolationEntity
         ↓
Auto Report Builder → Spring Boot API
         ↓
Presigned S3 Upload → PostgreSQL + Redis
         ↓
FCM Notification → Android Room/Flow → UI 반영
```

---

## 📌 성과 요약

| 분야 | 핵심 성과 |
|------|------------|
| AI 추론 | 온디바이스 실시간 탐지 **0.1~0.2초 내 응답**, 정확도 약 **90%** |
| 신고 자동화 | 탐지 후 **3초 이내 신고서 자동 생성 및 제출 가능** |
| 사용자 편의성 | 기존 대비 **신고 절차 80% 단축** |
| 보안성 | 클라우드 의존 없이 **완전 오프라인 추론 구조** |
| 협업 성과 | SSAFY 특화 프로젝트 **우수상 수상** |

---

## 🧪 문제 해결(트러블슈팅) 사례

- **문제:** 서버 추론 방식의 지연 및 네트워크 의존성  
  - **해결:** YOLOv8 TFLite 온디바이스 추론으로 구조 전환  
- **문제:** 대용량 영상 업로드 중단 및 누락  
  - **해결:** Presigned S3 업로드 + 상태 콜백 + WorkManager 재시도 구조 설계  
- **문제:** 메모리 누수로 인한 프레임 드롭  
  - **해결:** 세마포어 기반 ByteBuffer 재사용으로 메모리 안정화  
- **문제:** FCM 알림의 불일치  
  - **해결:** Room 업서트 + Flow 구독으로 UI 동기화 및 이력 일관성 확보  

---

## 🧠 배운 점 (Lessons Learned)

- **온디바이스 추론의 장점**: 지연 최소화·개인정보 보호·오프라인 대응의 중요성 체감  
- **End-to-End 데이터 흐름 설계**: 탐지→신고→전송까지 일관된 파이프라인의 안정성 확보  
- **Agile 운영 경험**: Jira·KPT 기반 협업으로 커뮤니케이션 효율 상승  
- **Compose 아키텍처 이해도 강화**: MVVM+Clean 구조로 유지보수성과 확장성 개선  

---

## 🗺 로드맵 (Roadmap)

- 🚀 AI 모델 정밀화 (Rule 강화, 데이터셋 확장, 정확도 95% 목표)  
- 🌐 WebSocket 기반 실시간 탐지 대시보드 추가  
- 📱 AR Overlay UI로 실시간 위반 시각화  
- 🧠 온디바이스 STT/TTS 명령 시스템 고도화 (“신고해” 음성 명령 자동 실행)  
- ☁️ MSA 기반 서버 구조로 확장성 강화  

---

## 🤝 기여 (Contributing)

- 브랜치 전략: `main`(배포) / `develop`(통합) / `feature/*`  
- PR 규칙: 기능 단위 제출, 테스트 결과 및 이슈 링크 첨부  
- 커밋 컨벤션: `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`  

---

## 👥 팀 & 역할

| 이름 | 역할 | 주요 업무 |
|------|------|-----------|
| 김연주 | DATA AI | 데이터 가공, 도로 객체 인식 모델 구현, 도로 객체 인식 결과 후처리 알고리즘 구현 |
| 반충기 | DATA AI | 데이터 가공, 차선 인식 모델 구현, 차선 인식 결과 후처리 알고리즘 구현, TFLite 변환, 온디바이스 탐지 처리 |
| 홍민기 | Data, AI | OCR 모델 학습, 객체 트래킹 구현, 신호 위반 알고리즘 구현 |
| 박상윤 | Android | 온디바이스 탐지 처리, 전처리/후처리 구현, 신고 자동화, UI/UX 화면(로그인, 신고 상세, 탐지 카메라) 구현 Google 로그인, UI/UX 설계 |
| 서현호 | Android | MVVM + 클린 아키텍처 적용 프로젝트 설계, 주요 UI/UX 화면(홈, 탐지, 신고, 통계, 프로필) 구현 |
| 박성준 | Server | Spring Boot, JWT, Presigned S3 업로드 API, FCM 서버 |
---

## 🧭 슬로건

> **“탐지하고, 기록하고, 신뢰로 신고한다.”**
