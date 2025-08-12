import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');

// 테스트 설정
export const options = {
  stages: [
    { duration: '2m', target: 10 },    // 2분간 10명으로 증가
    { duration: '5m', target: 50 },    // 5분간 50명으로 증가
    { duration: '3m', target: 100 },   // 3분간 100명으로 증가
    { duration: '2m', target: 0 },     // 2분간 0명으로 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95% 요청이 2초 이내
    http_req_failed: ['rate<0.1'],     // 에러율 10% 미만
    errors: ['rate<0.1'],              // 커스텀 에러율 10% 미만
  },
};

// 기본 URL (로컬 Spring Boot 앱)
const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

export default function () {

  // 1. 상품 목록 조회 (페이징)
  const productListResponse = http.get(`${BASE_URL}/api/v1/products?sortBy=LIKES&pageNumber=0&pageSize=10`);
  
  check(productListResponse, {
    '상품 목록 응답 상태': (r) => r.status === 200,
    '상품 목록 응답 크기 > 0': (r) => r.json('content') && r.json('content').length > 0,
  });

  // 2. 상품 검색 (브랜드)
  const searchResponse = http.get(`${BASE_URL}/api/v1/products?brandId=1&sortBy=LIKES&pageNumber=0&pageSize=10`);

  check(searchResponse, {
    '상품 검색 응답 상태': (r) => r.status === 200,
    '상품 검색 응답 크기 > 0': (r) => r.json('content') && r.json('content').length > 0,
  });

  sleep(1)

}

// 테스트 완료 후 요약
export function handleSummary(data) {
  console.log('=== 상품 API 성능 테스트 결과 ===');
  console.log(`총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`95% 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`에러율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);

} 