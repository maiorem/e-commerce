import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

export const options = {
  stages: [
    { duration: '2m', target: 10 },    // 2분간 10명으로 증가
    { duration: '5m', target: 50 },    // 5분간 50명으로 증가
    { duration: '3m', target: 100 },   // 3분간 100명으로 증가
    { duration: '2m', target: 0 },     // 2분간 0명으로 감소
  ],
};

// 기본 URL (로컬 Spring Boot 앱)
const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';


export default function () {
  const randomProductId = Math.floor(Math.random() * 1000000) + 1;
  http.get(`${BASE_URL}/api/v1/products/${randomProductId}`);

}



// 테스트 완료 후 요약
export function handleSummary(data) {
  console.log('=== 상품상세 API 성능 테스트 결과 ===');
  console.log(`총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`95% 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`에러율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);

} 