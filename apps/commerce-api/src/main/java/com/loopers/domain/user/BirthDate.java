package com.loopers.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Embeddable
@Getter
public class BirthDate {

    @Column(name = "birth_date")
    private LocalDate value;

    protected BirthDate() {}

    public static BirthDate of(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("생년월일은 비어있을 수 없습니다.");
        }

        try {
            LocalDate birthDate = LocalDate.parse(value);
            if (birthDate.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("생년월일은 미래일 수 없습니다.");
            }

            BirthDate birth = new BirthDate();
            birth.value = birthDate;
            return birth;
        } catch (Exception e) {
            throw new IllegalArgumentException("올바른 날짜 형식이 아닙니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BirthDate birthDate = (BirthDate) o;
        return Objects.equals(value, birthDate.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
