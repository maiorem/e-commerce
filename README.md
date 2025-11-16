# 🛒 E-Commerce Platform

> **멀티모듈 아키텍처 기반 E-Commerce 플랫폼**
> Kotlin + Spring Boot + JPA + Redis + Kafka를 활용한 확장 가능하고 견고한 커머스 시스템

## 📋 목차

- [프로젝트 개요](#-프로젝트-개요)
- [시스템 아키텍처](#-시스템-아키텍처)
- [주요 기능](#-주요-기능)
- [기술 스택](#-기술-스택)
- [실행 방법](#-실행-방법)
- [API 문서](#-api-문서)
- [설계 문서](#-설계-문서)
- [모듈 구조](#-모듈-구조)
- [개발 환경 설정](#-개발-환경-설정)

## 🎯 프로젝트 개요

본 프로젝트는 **멀티모듈 아키텍처**로 구성된 현대적인 E-Commerce 플랫폼입니다. 도메인 주도 설계(DDD)와 클린 아키텍처 원칙을 적용하여 확장성과 유지보수성을 확보했습니다.

### 핵심 가치
- **확장성**: 마이크로서비스 아키텍처로 전환 가능한 구조
- **성능**: Redis 캐싱과 비동기 처리로 최적화
- **안정성**: 테스트 주도 개발과 Circuit Breaker 패턴 적용
- **관찰가능성**: 모니터링과 로깅 시스템 완비

## 🏗 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Client App    │────│  Commerce API   │────│   External PG   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                               │
                ┌──────────────┼──────────────┐
                │              │              │
        ┌───────▼────┐  ┌──────▼──────┐  ┌───▼─────┐
        │   MySQL    │  │    Redis     │  │  Kafka  │
        │ (Primary)  │  │ (Master/RO)  │  │ (Events)│
        └────────────┘  └─────────────┘  └─────────┘
```

### 멀티모듈 구조

```
Root
├── apps ( Spring Boot Applications )
│   ├── 📦 commerce-api        # 메인 E-Commerce API 서버
│   ├── 📦 pg-simulator        # 결제 게이트웨이 시뮬레이터
│   ├── 📦 commerce-collector  # 이벤트 수집 서비스
│   └── 📦 collector-batch     # 배치 처리 서비스
├── modules ( Reusable Configurations )
│   ├── 📦 jpa     # JPA 설정 및 공통 엔티티
│   ├── 📦 redis   # Redis 설정 및 캐시 구성
│   └── 📦 kafka   # Kafka 설정 및 메시징
└── supports ( Add-on Modules )
    ├── 📦 monitoring  # 애플리케이션 모니터링
    ├── 📦 logging     # 통합 로깅
    └── 📦 jackson     # JSON 직렬화 설정
```

## ✨ 주요 기능

### 🛍 상품 관리
- **상품 목록 조회**: 브랜드/카테고리 필터링, 정렬, 페이징 지원
- **상품 상세 조회**: 상품 정보, 재고, 좋아요 수 표시
- **브랜드 정보**: 브랜드별 상품 목록 및 상세 정보

### 👤 사용자 기능
- **상품 좋아요**: 관심 상품 등록/취소 및 목록 조회
- **포인트 시스템**: 적립 및 사용 내역 관리
- **주문 내역**: 상세 주문 정보 및 결제 이력 조회

### 🛒 주문 & 결제
- **주문 생성**: 다중 상품 주문, 재고 확인, 포인트 차감
- **쿠폰 시스템**: 정액/정률 할인 쿠폰 적용
- **결제 처리**: 외부 PG사 연동 및 결제 이력 관리

### 📊 시스템 운영
- **성능 모니터링**: Actuator 기반 헬스 체크
- **캐싱 전략**: Redis를 활용한 조회 성능 최적화
- **비동기 처리**: Kafka를 통한 이벤트 기반 아키텍처

## 🛠 기술 스택

### Core Framework
- **Language**: Java 21
- **Framework**: Spring Boot 3
- **Build Tool**: Gradle

### Data & Persistence
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA + QueryDSL
- **Cache**: Redis 7.0 (Master/Replica)

### Messaging & Communication
- **Message Broker**: Apache Kafka 3.5.1
- **HTTP Client**: OpenFeign
- **API Documentation**: OpenAPI 3.0 (Swagger)

### Infrastructure & Monitoring
- **Containerization**: Docker Compose
- **Monitoring**: Spring Boot Actuator + Grafana
- **Logging**: Structured Logging
- **Testing**: JUnit 5 + TestContainers + MockK

### Quality & Reliability
- **Circuit Breaker**: Resilience4j
- **Retry Logic**: Spring Retry
- **Code Quality**: JaCoCo (Test Coverage)
- **Data Generation**: Instancio (Test Fixtures)

## 🚀 실행 방법

### 1️⃣ 사전 요구사항
- **Java 21** 이상
- **Docker & Docker Compose**
- **Git**

### 2️⃣ 인프라 서비스 실행
```bash
# MySQL, Redis, Kafka 실행
docker-compose -f docker/infra-compose.yml up -d

# 서비스 상태 확인
docker-compose -f docker/infra-compose.yml ps
```

### 3️⃣ 애플리케이션 빌드
```bash
# 프로젝트 전체 빌드
./gradlew clean build

# 테스트 건너뛰고 빌드
./gradlew clean build -x test
```

### 4️⃣ 메인 API 서버 실행
```bash
# Commerce API 서버 실행
./gradlew :apps:commerce-api:bootRun

# 또는 JAR 파일로 실행
java -jar apps/commerce-api/build/libs/commerce-api-*.jar
```

### 5️⃣ 테스트 데이터 생성 (선택사항)
```bash
# 대용량 테스트 데이터 생성
./gradlew :apps:commerce-api:runLargeSeeder
```

### 6️⃣ 서비스 확인
- **API 서버**: http://localhost:8080
- **API 문서**: http://localhost:8080/swagger-ui.html
- **Actuator**: http://localhost:8080/actuator/health
- **Kafka UI**: http://localhost:9090

## 📚 API 문서

### REST API Endpoints

#### 상품 관리
```http
GET    /api/v1/products              # 상품 목록 조회
GET    /api/v1/products/{id}         # 상품 상세 조회
POST   /api/v1/products/{id}/likes   # 상품 좋아요 등록/취소
```

#### 사용자 관리
```http
GET    /api/v1/users/{id}/likes      # 사용자 좋아요 목록
GET    /api/v1/users/{id}/orders     # 사용자 주문 목록
```

#### 주문 관리
```http
POST   /api/v1/orders               # 주문 생성
GET    /api/v1/orders/{id}          # 주문 상세 조회
```

#### 쿠폰 관리
```http
GET    /api/v1/coupons              # 사용자 쿠폰 목록
```

### 공통 헤더
```http
X-USER-ID: {userId}                 # 사용자 인증 헤더 (필수)
Content-Type: application/json      # JSON 요청 시 필수
```

자세한 API 명세는 [Swagger UI](http://localhost:8080/swagger-ui.html)에서 확인할 수 있습니다.

## 📋 설계 문서

프로젝트의 상세한 설계 문서는 `docs/design/` 디렉터리에서 확인할 수 있습니다:

### 📄 문서 목록
1. **[요구사항 정의](docs/design/01-requirements.md)**
   - 8가지 핵심 기능 요구사항
   - HTTP 상태코드 및 에러 메시지 정의
   - 유비쿼터스 용어 정의

2. **[시퀀스 다이어그램](docs/design/02-sequence-diagrams.md)**
   - 좋아요 등록/취소 플로우
   - 주문 생성 프로세스
   - 쿠폰 사용 시나리오

3. **[클래스 다이어그램](docs/design/03-class-diagrams.md)**
   - 도메인 모델 설계
   - 엔티티 관계 정의
   - Value Object 및 Enum 구조

4. **[ERD](docs/design/04-erd.md)**
   - 데이터베이스 스키마 설계
   - 테이블 관계 및 제약조건
   - 인덱스 전략

## 🏭 모듈 구조

### Applications (apps)
실행 가능한 Spring Boot 애플리케이션들입니다.

#### 📦 commerce-api
- **역할**: 메인 E-Commerce API 서버
- **포트**: 8080
- **주요 기능**: 상품, 주문, 사용자, 쿠폰 관리
- **의존성**: JPA, Redis, Kafka, Monitoring

#### 📦 pg-simulator
- **역할**: 결제 게이트웨이 시뮬레이터
- **목적**: 외부 PG사 API 모킹
- **기능**: 결제 승인/거절 시뮬레이션

#### 📦 commerce-collector
- **역할**: 실시간 이벤트 수집 서비스
- **기능**: Kafka를 통한 이벤트 스트림 처리

#### 📦 collector-batch
- **역할**: 배치 처리 서비스
- **기능**: 주기적 데이터 집계 및 분석

### Modules (modules)
재사용 가능한 구성 모듈들입니다.

#### 📦 jpa
- **역할**: JPA 공통 설정 및 기본 엔티티
- **구성**: BaseEntity, 공통 Repository, QueryDSL 설정

#### 📦 redis
- **역할**: Redis 캐시 설정 및 유틸리티
- **구성**: Master/Replica 설정, 캐시 전략

#### 📦 kafka
- **역할**: Kafka 메시징 설정
- **구성**: Producer/Consumer 설정, 토픽 관리

### Supports (supports)
부가 기능을 지원하는 애드온 모듈들입니다.

#### 📦 monitoring
- **역할**: 애플리케이션 모니터링 설정
- **구성**: Actuator, 헬스 체크, 메트릭

#### 📦 logging
- **역할**: 통합 로깅 시스템
- **구성**: 구조화 로그, 로그 레벨 관리

#### 📦 jackson
- **역할**: JSON 직렬화 설정
- **구성**: 날짜 형식, 필드 명명 규칙

## ⚙️ 개발 환경 설정

### 로컬 개발 환경 구성

#### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd loopers-l2-round1
```

#### 2. IDE 설정 (IntelliJ IDEA 권장)
- **Kotlin Plugin** 활성화
- **Spring Boot Plugin** 설치
- **QueryDSL** 소스 폴더 등록: `build/generated/sources/annotationProcessor/java/main`

#### 3. 환경변수 설정
```bash
# 데이터베이스 연결 정보
export MYSQL_HOST=localhost
export MYSQL_PORT=3307
export MYSQL_USER=application
export MYSQL_PASSWORD=application
export MYSQL_DATABASE=loopers

# Redis 연결 정보
export REDIS_MASTER_HOST=localhost
export REDIS_MASTER_PORT=6379
export REDIS_READONLY_HOST=localhost
export REDIS_READONLY_PORT=6380

# Kafka 연결 정보
export KAFKA_BOOTSTRAP_SERVERS=localhost:19092
```

### 테스트 실행
```bash
# 전체 테스트 실행
./gradlew test

# 특정 모듈 테스트
./gradlew :apps:commerce-api:test

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
```

### 코드 품질 검사
```bash
# 빌드 시 자동 실행됨
./gradlew build

# 테스트 커버리지 확인
# build/reports/jacoco/test/html/index.html 열기
```

