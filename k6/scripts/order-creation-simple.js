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
  // LargeSeeder 기반 사용자 ID 사용 (user000001 ~ user100000)
  const userId = 'user' + String(Math.floor(Math.random() * 100000) + 1).padStart(6, '0');
  
  // LargeSeeder 기반 상품 ID와 가격 분포 사용
  const productId = Math.floor(Math.random() * 1000000) + 1; // 1 ~ 1,000,000
  const quantity = Math.floor(Math.random() * 9) + 1; // 1~9개 (OrderItem 생성 로직 기반)
  
  // LargeSeeder의 가격 분포 반영
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
  
  // 결제 방법 선택 (LargeSeeder 기반: CREDIT_CARD, POINT)
  const paymentMethods = ['CREDIT_CARD', 'POINT'];
  const paymentMethod = paymentMethods[Math.floor(Math.random() * paymentMethods.length)];
  
  // 쿠폰 사용 확률 10%
  const useCoupon = Math.random() < 0.1;
  const couponCode = useCoupon ? 'COUPON' + String(Math.floor(Math.random() * 10000) + 1).padStart(6, '0') : null;
  
  const orderData = {
    payment_method: paymentMethod,
    card_type: paymentMethod === 'CREDIT_CARD' ? 'SAMSUNG' : null,
    card_number: paymentMethod === 'CREDIT_CARD' ? '1234-5678-9012-3456' : null,
    point_amount: paymentMethod === 'POINT' ? Math.floor(Math.random() * 90000) + 10000 : null,
    coupon_code: couponCode,
    items: [
      {
        product_id: productId,
        quantity: quantity,
        product_name: `상품 ${productId}`,
        product_price: generatePrice()
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
