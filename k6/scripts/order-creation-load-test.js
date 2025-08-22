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

// 커스텀 메트릭
const orderCreationDuration = new Trend('order_creation_duration');
const orderSuccessRate = new Rate('order_success_rate');


// 기본 URL (로컬 Spring Boot 앱)
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// LargeSeeder 기반 테스트 데이터 생성 함수
function generateOrderData() {
  // LargeSeeder 기반 사용자 ID 사용 (user000001 ~ user100000)
  const userId = 'user' + String(Math.floor(Math.random() * 100000) + 1).padStart(6, '0');
  
  // 상품 개수 (1~3개, 평균 3개의 아이템으로 600,000개 주문 아이템 생성)
  const itemCount = Math.floor(Math.random() * 3) + 1;
  const items = [];
  
  // LargeSeeder의 가격 분포 함수
  function generatePrice() {
    const rand = Math.random();
    if (rand < 0.85) {
      // 85%: 1,000 ~ 50,000원
      return Math.floor(Math.random() * 49) * 1000 + 1000;
    } else if (rand < 0.95) {
      // 10%: 51,000 ~ 300,000원
      return Math.floor(Math.random() * 250) * 1000 + 51000;
    } else {
      // 5%: 301,000 ~ 1,000,000원
      return Math.floor(Math.random() * 700) * 1000 + 301000;
    }
  }
  
  // 여러 상품 아이템 생성
  for (let i = 0; i < itemCount; i++) {
    const productId = Math.floor(Math.random() * 1000000) + 1; // 1 ~ 1,000,000
    const quantity = Math.floor(Math.random() * 9) + 1; // 1~9개
    
    items.push({
      product_id: productId,
      quantity: quantity,
      product_name: `상품 ${productId}`,
      product_price: generatePrice()
    });
  }
  
  // LargeSeeder 기반 결제 방법 선택 (CREDIT_CARD, POINT)
  const paymentMethods = ['CREDIT_CARD', 'POINT'];
  const paymentMethod = paymentMethods[Math.floor(Math.random() * paymentMethods.length)];
  
  // 쿠폰 사용 확률 10% (UserCoupon 50,000개 / User 100,000명 = 0.5개 평균)
  const useCoupon = Math.random() < 0.1;
  const couponCode = useCoupon ? 'COUPON' + String(Math.floor(Math.random() * 10000) + 1).padStart(6, '0') : null;
  
  // 포인트 사용시 LargeSeeder 기반 포인트 금액
  function generatePointAmount() {
    const lambda = 1.0 / 10000.0;
    const u = Math.max(1e-12, Math.random());
    const x = Math.floor(-Math.log(u) / lambda);
    return Math.min(x, 100000);
  }
  
  const orderData = {
    payment_method: paymentMethod,
    card_type: paymentMethod === 'CREDIT_CARD' ? 'SAMSUNG' : null,
    card_number: paymentMethod === 'CREDIT_CARD' ? '1234-5678-9012-3456' : null,
    point_amount: paymentMethod === 'POINT' ? generatePointAmount() : null,
    coupon_code: couponCode,
    items: items

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
