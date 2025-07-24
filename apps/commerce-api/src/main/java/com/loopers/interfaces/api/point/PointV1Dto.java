package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;

public class PointV1Dto {

    public record PointRequest(
        @NotBlank(message = "충전 금액을 입력해주세요.")
        @Parameter(name = "충전금액", description = "충전하고자 하는 금액")
        int amount
    ) {
        public static PointRequest from(int amount) {
            return new PointRequest(amount);
        }
    }

    public record PointResponse(
        String userId,
        int totalAmount
    ) {
        public static PointResponse from(PointInfo info) {
            return new PointResponse(info.userId(), info.totalAmount());
        }
    }

}
