package com.loopers.domain.user.event;

public interface UserActionTrackingPort {

    void trackUserAction(UserActionData actionData);

}
