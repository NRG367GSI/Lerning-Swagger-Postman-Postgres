package ru.hogwarts.school.exception;

public class FacultyNotFoundException extends RuntimeException {
    public FacultyNotFoundException(String message) {
        super(message);
    }

    @Override
    public String toString() {
        return "FacultyNotFoundException{}";
    }
}
