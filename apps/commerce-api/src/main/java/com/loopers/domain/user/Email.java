package com.loopers.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Embeddable
@Getter
public class Email {

    @Column(name = "email")
    private String value;

    protected Email() {}

    public static Email of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("이메일은 비어있을 수 없습니다.");
        }

        String trimmedValue = value.trim();
        
        // 기본적인 이메일 형식 검증
        if (!trimmedValue.contains("@")) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }

        // @ 앞뒤로 문자가 있어야 함
        String[] parts = trimmedValue.split("@");
        if (parts.length != 2 || parts[0].isEmpty() || parts[1].isEmpty()) {
            throw new IllegalArgumentException("올바른 이메일 형식이 아닙니다.");
        }
        Email email = new Email();
        email.value = trimmedValue;
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
