package ru.hogwarts.school.exception;

public class FacultyAlreadyExistsException extends RuntimeException{

    public FacultyAlreadyExistsException(String message) {
        super(message);
    }
}
