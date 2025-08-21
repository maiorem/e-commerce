import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

export const options = {
  stages: [
    { duration: '1m', target: 5 },     // 1분간 5명으로 증가
    { duration: '2m', target: 20 },    // 2분간 20명으로 증가
    { duration: '3m', target: 50 },    // 3분간 50명으로 증가
    { duration: '2m', target: 100 },   // 2분간 100명으로 증가
    { duration: '2m', target: 100 },   // 2분간 100명 유지
    { duration: '2m', target: 0 },     // 2분간 0명으로 감소
  ]
};
/ 커스텀 메트릭
const orderCreationDuration = new Trend('order_creation_duration');
const orderSuccessRate = new Rate('order_success_rate');


// 기본 URL (로컬 Spring Boot 앱)
const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';

// 테스트 데이터 생성 함수
function generateOrderData() {
  const userId = `user${Math.floor(Math.random() * 100000) + 1}`;
  const productId = Math.floor(Math.random() * 1000000) + 1;
  const quantity = Math.floor(Math.random() * 3) + 1; // 1~3개
  const productPrice = Math.floor(Math.random() * 500000) + 10000; // 10,000 ~ 510,000원
  
  // 랜덤 결제 방법 선택 (80% 카드, 20% 포인트)
  const useCardPayment = Math.random() < 0.8;
  
  const orderData = {
    payment_method: useCardPayment ? 'CREDIT_CARD' : 'POINT',
    card_type: useCardPayment ? 'SAMSUNG' : null,
    card_number: useCardPayment ? '1234-5678-9012-3456' : null,
    point_amount: useCardPayment ? null : Math.floor(Math.random() * 10000) + 1000,
    coupon_code: Math.random() < 0.3 ? `COUPON${Math.floor(Math.random() * 10000) + 1}` : null, // 30% 확률로 쿠폰 사용
    items: [
      {
        product_id: productId,
        quantity: quantity,
        product_name: `상품 ${productId}`,
        product_price: productPrice
      }
    ]
  };
  
  return { userId, orderData };
}

export default function () {
  const { userId, orderData } = generateOrderData();
  
  // 헤더 설정
  const headers = {
    'Content-Type': 'application/json',
    'X-USER-ID': userId,
  };
  
  // 주문 생성 요청
  const startTime = Date.now();
  const response = http.post(
    `${BASE_URL}/api/v1/orders`,
    JSON.stringify(orderData),
    { headers: headers }
  );
  
  const endTime = Date.now();
  const duration = endTime - startTime;
  
  // 응답 검증
  const success = check(response, {
    'status is 200': (r) => r.status === 200,
    'response has orderId': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.data && body.data.orderId;
      } catch (e) {
        return false;
      }
    },
    'response time < 2s': (r) => r.timings.duration < 2000,
  });
  
  // 커스텀 메트릭 업데이트
  orderCreationDuration.add(duration);
  orderSuccessRate.add(success);
  
  // 응답 로깅 (디버깅용)
  if (!success) {
    console.log(`주문 생성 실패 - User: ${userId}, Status: ${response.status}, Body: ${response.body}`);
  }
  
  // 요청 간 간격 (0.5~2초)
  sleep(Math.random() * 1.5 + 0.5);
}

// 테스트 완료 후 요약
export function handleSummary(data) {
  console.log('=== 주문 생성 API 부하테스트 결과 ===');
  console.log(`총 요청 수: ${data.metrics.http_reqs.values.count}`);
  console.log(`평균 응답 시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
  console.log(`95% 응답 시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
  console.log(`99% 응답 시간: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms`);
  console.log(`최대 응답 시간: ${data.metrics.http_req_duration.values.max.toFixed(2)}ms`);
  console.log(`에러율: ${(data.metrics.http_req_failed.values.rate * 100).toFixed(2)}%`);
  console.log(`주문 생성 성공률: ${(data.metrics.order_success_rate.values.rate * 100).toFixed(2)}%`);
  console.log(`평균 주문 생성 시간: ${data.metrics.order_creation_duration.values.avg.toFixed(2)}ms`);

}
