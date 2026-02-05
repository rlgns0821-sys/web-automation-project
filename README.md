# 웹 자동화 관리 플랫폼

## 🔗 Live URL
https://triston-nongelatinizing-rema.ngrok-free.dev/

## 📌 프로젝트 소개
로컬 환경에서만 실행되던 자동화 스크립트를  
Spring Boot 기반의 웹 서비스로 확장한 프로젝트입니다.

사용자는 웹 UI를 통해 조건을 설정하고,  
서버에서 자동 작업을 실행·중지·관리할 수 있습니다.

## 🛠 기술 스택
- Back-end: Spring Boot
- Front-end: Thymeleaf / HTML / CSS / JavaScript
- Automation: Puppeteer / Selenium
- DB: MySQL
- Infra: Local + ngrok
- Tools: Git / GitHub

## ⭐ 주요 기능
- 자동화 조건 설정 (날짜 / 시간 / 대상)
- 자동 실행 및 중지 제어
- 실행 상태 및 로그 확인
- 사용자 / 관리자 기능 분리

## 👤 담당 역할
- 개인 프로젝트 (100%)
- 서비스 기획 및 기능 정의
- 백엔드 구조 설계 (Controller / Service / DAO)
- 자동화 스크립트 웹 연동
- 실행 충돌 및 예외 처리

## 🔥 핵심 구현 포인트
- 로컬 자동화 로직을 웹 요청 기반 구조로 재설계
- 중복 실행 방지를 위한 상태 관리 로직 구현
- 사용자 입력값 검증 및 서버 측 예외 처리

## 🧨 트러블슈팅
### 1. 자동화 중복 실행 문제
- 원인: 사용자 연속 요청
- 해결: 실행 상태 플래그 관리 및 버튼 비활성화
- 결과: 실행 오류 및 서버 부하 감소

### 2. 외부 접근 환경 구성 문제
- 원인: 로컬 환경 한계
- 해결: ngrok을 통한 외부 접근 구성
- 결과: 실제 서비스 형태로 테스트 가능

## ▶ 실행 방법
```bash
./gradlew bootRun