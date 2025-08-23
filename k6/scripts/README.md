# K6 부하테스트 스크립트

이 디렉토리에는 다양한 API에 대한 부하테스트 스크립트가 포함되어 있습니다.

## 📁 스크립트 목록

### 1. **product-detail.js** - 상품 상세 API 부하테스트
- **목적**: 상품 상세 조회 API의 성능 테스트
- **엔드포인트**: `GET /api/v1/products/{id}`
- **특징**: 랜덤 상품 ID로 조회

### 2. **product-list-ramp_up.js** - 상품 목록 API 부하테스트
- **목적**: 상품 목록 조회 API의 성능 테스트
- **엔드포인트**: `GET /api/v1/products?pageSize=20`
- **특징**: 페이지네이션된 상품 목록 조회

### 3. **order-creation-load-test.js** - 주문 생성 API 고급 부하테스트 ⭐
- **목적**: 주문 생성 API의 종합적인 성능 테스트
- **엔드포인트**: `POST /api/v1/orders`
- **특징**: 
  - 다양한 결제 방법 (카드 80%, 포인트 20%)
  - 쿠폰 사용 (30% 확률)
  - 커스텀 메트릭 (주문 생성 시간, 성공률)
  - 상세한 응답 검증

### 4. **order-creation-simple.js** - 주문 생성 API 간단 부하테스트
- **목적**: 주문 생성 API의 기본적인 성능 테스트
- **엔드포인트**: `POST /api/v1/orders`
- **특징**: 
  - 카드 결제만 사용
  - 간단한 데이터 구조
  - 빠른 실행

## 🚀 실행 방법

### 기본 실행
```bash
# 주문 생성 고급 부하테스트
k6 run order-creation-load-test.js

# 주문 생성 간단 부하테스트
k6 run order-creation-simple.js

# 상품 상세 부하테스트
k6 run product-detail.js

# 상품 목록 부하테스트
k6 run product-list-ramp_up.js
```

### 환경변수 설정
```bash
# 다른 서버 URL로 테스트
BASE_URL=http://localhost:8080 k6 run order-creation-load-test.js

# Docker 환경에서 실행
BASE_URL=http://host.docker.internal:8080 k6 run order-creation-load-test.js
```

### Docker에서 실행
```bash
# Docker 컨테이너에서 k6 실행
docker run -i --rm -v $(pwd):/scripts -w /scripts grafana/k6 run order-creation-load-test.js
```

## 📊 주문 생성 API 부하테스트 상세

### 테스트 시나리오
1. **1분**: 5명의 가상 사용자로 시작
2. **2분**: 20명으로 증가
3. **3분**: 50명으로 증가
4. **2분**: 100명으로 증가
5. **2분**: 100명 유지 (최대 부하)
6. **2분**: 0명으로 감소

### 생성되는 주문 데이터
- **사용자 ID**: `user1` ~ `user100000` (랜덤)
- **상품 ID**: 1 ~ 1,000,000 (랜덤)
- **수량**: 1 ~ 3개 (랜덤)
- **상품 가격**: 10,000원 ~ 510,000원 (랜덤)
- **결제 방법**: 
  - 카드 결제 (80%): SAMSUNG 카드, 고정 카드번호
  - 포인트 결제 (20%): 1,000 ~ 10,000 포인트 사용
- **쿠폰**: 30% 확률로 사용

### 성능 기준
- **응답 시간**: 95% 요청이 2초 이내
- **에러율**: 10% 미만
- **주문 생성 성공률**: 모니터링

### 커스텀 메트릭
- `order_creation_duration`: 주문 생성 소요 시간
- `order_success_rate`: 주문 생성 성공률

## 🔧 테스트 전 준비사항

### 1. 데이터베이스 준비
```bash
# LargeSeeder로 테스트 데이터 생성
./gradlew :apps:commerce-api:runLargeSeeder
```

### 2. 애플리케이션 실행
```bash
# Spring Boot 애플리케이션 실행
./gradlew :apps:commerce-api:bootRun
```

### 3. 인프라 서비스 실행
```bash
# MySQL, Redis 등 실행
docker-compose -f docker/infra-compose.yml up -d
```

## 📈 결과 분석

### 성공적인 테스트 결과
- 응답 시간이 임계값 내에 유지
- 에러율이 10% 미만
- 주문 생성 성공률이 90% 이상

### 문제가 있는 경우
- **응답 시간 초과**: 데이터베이스 성능, 인덱스 확인
- **높은 에러율**: 애플리케이션 로그, 데이터베이스 연결 확인
- **주문 생성 실패**: 비즈니스 로직, 데이터 검증 확인

## 🎯 테스트 목표

1. **성능 검증**: 주문 생성 API가 예상 부하를 처리할 수 있는지 확인
2. **안정성 검증**: 장시간 부하 상황에서도 안정적으로 동작하는지 확인
3. **데이터 일관성**: 주문 생성 시 재고, 포인트, 쿠폰 등이 올바르게 처리되는지 확인
4. **사용자 경험**: 응답 시간이 사용자가 기다릴 수 있는 수준인지 확인
