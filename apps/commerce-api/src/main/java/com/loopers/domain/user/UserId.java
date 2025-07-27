package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Pattern;

@Embeddable
public class UserId {

    // 영문 및 숫자 (4~10자 이내)
    private static final String USER_ID_REGEX = "^[a-zA-Z0-9]{4,10}$";
    private static final Pattern USER_ID_PATTERN = Pattern.compile(USER_ID_REGEX);

    @Column(name = "user_id")
    private String value;

    protected UserId() {
    }

    public static UserId of(String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아이디는 비어있을 수 없습니다.");
        }
        if (!USER_ID_PATTERN.matcher(value).matches()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "아이디는 10자 이하의 영문 소문자와 숫자로만 구성되어야 합니다.");
        }
        UserId userId = new UserId();
        userId.value = value;
        return userId;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
