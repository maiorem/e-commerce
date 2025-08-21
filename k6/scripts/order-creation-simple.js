import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 10 },    // 1분간 10명으로 증가
    { duration: '2m', target: 50 },    // 2분간 50명으로 증가
    { duration: '2m', target: 100 },   // 2분간 100명으로 증가
    { duration: '1m', target: 0 },     // 1분간 0명으로 감소
  ],
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // 랜덤 사용자 ID 생성
  const userId = `user${Math.floor(Math.random() * 100000) + 1}`;
  
  // 주문 데이터 생성
  const orderData = {
    payment_method: 'CREDIT_CARD',
    card_type: 'SAMSUNG',
    card_number: '1234-5678-9012-3456',
    point_amount: null,
    coupon_code: null,
    items: [
      {
        product_id: Math.floor(Math.random() * 1000000) + 1,
        quantity: Math.floor(Math.random() * 3) + 1,
        product_name: '테스트 상품',
        product_price: Math.floor(Math.random() * 100000) + 10000
      }
    ]
  };
  
  // 헤더 설정
  const headers = {
    'Content-Type': 'application/json',
    'X-USER-ID': userId,
  };
  
  // 주문 생성 요청
  const response = http.post(
    `${BASE_URL}/api/v1/orders`,
    JSON.stringify(orderData),
    { headers: headers }
  );
  
  // 응답 검증
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response has orderId': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data && body.data.orderId;
      } catch (e) {
        return false;
      }
    },
    'response time < 3s': (r) => r.timings.duration < 3000,
  });
  
  // 요청 간 간격 (1~3초)
  sleep(Math.random() * 2 + 1);
}

// 테스트 완료 후 요약
export function handleSummary(data) {
  console.log('=== 주문 생성 API 간단 부하테스트 결과 ===');
  console.log(`총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`95% 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`에러율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);
}
