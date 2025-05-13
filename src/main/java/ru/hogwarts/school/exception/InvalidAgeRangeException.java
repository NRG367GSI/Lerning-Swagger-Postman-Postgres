package ru.hogwarts.school.exception;

public class InvalidAgeRangeException extends RuntimeException {

    public InvalidAgeRangeException(String message) {
        super(message);
    }

    public InvalidAgeRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
