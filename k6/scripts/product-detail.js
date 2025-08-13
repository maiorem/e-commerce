import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭 정의
const errorRate = new Rate('errors');
const productDetailDuration = new Trend('product_detail_duration');
const cacheHitRate = new Rate('cache_hits');

// 테스트 설정
export const options = {
  stages: [
    // 부하 증가 단계
    { duration: '1m', target: 30 },   // 1분간 30명까지 증가
    { duration: '3m', target: 60 },   // 3분간 60명까지 증가
    { duration: '2m', target: 60 },   // 2분간 60명 유지
    { duration: '1m', target: 0 },    // 1분간 0명까지 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<1000'], // 95% 요청이 1초 이내
    http_req_failed: ['rate<0.05'],    // 에러율 5% 미만
    errors: ['rate<0.05'],             // 커스텀 에러율 5% 미만
  },
};

// 테스트 데이터
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';


// 상품 상세 조회 (랜덤 ID)
const randomProductId = Math.floor(Math.random() * 1000000) + 1;
const productDetailResponse = http.get(`${BASE_URL}/api/v1/products/${randomProductId}`);

check(productDetailResponse, {
  '상품 상세 응답 상태': (r) => r.status === 200,
  '상품 상세 응답 데이터 존재': (r) => r.json('data') && r.json('data').id,
});

// 에러율 계산
errorRate.add(productListResponse.status !== 200 || searchResponse.status !== 200 || productDetailResponse.status !== 200);

// 요청 간 간격
sleep(1);


// 테스트 완료 후 요약
export function handleSummary(data) {
  console.log('=== 상품상세 API 성능 테스트 결과 ===');
  console.log(`총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`95% 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`에러율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);

} 