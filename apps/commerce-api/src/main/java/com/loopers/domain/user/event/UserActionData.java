package com.loopers.domain.user.event;

import com.loopers.domain.user.UserId;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.ZonedDateTime;

@Getter
@RequiredArgsConstructor
public class UserActionData {

    private final UserId userId;
    private final UserActionType actionType;
    private final Long targetId;
    private final String additionalInfo;
    private final ZonedDateTime timestamp;

    public static UserActionData create(UserId userId, UserActionType actionType, Long targetId) {
        return new UserActionData(userId, actionType, targetId, null, ZonedDateTime.now());
    }

    public static UserActionData create(UserId userId, UserActionType actionType, Long targetId, String additionalInfo) {
        return new UserActionData(userId, actionType, targetId, additionalInfo, ZonedDateTime.now());
    }

}
