package ru.hogwarts.school.exception;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "StudentNotFoundException{}";
    }
}
