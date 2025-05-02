package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class StudentService {
    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository1) {
        this.studentRepository = studentRepository1;
    }

    public Student createStudent(Student student) {
        return studentRepository.save(student);
    }

    public Student getStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Студент с ID " + studentId + " не найден"));
    }

    public Student updateStudent(Long studentId, Student student) {
        return studentRepository.findById(studentId)
                .map(existingStudent -> {
                    student.setId(studentId); // Ensure the ID is set for update
                    return studentRepository.save(student);
                })
                .orElseThrow(() -> new StudentNotFoundException("Студент с ID " + studentId + " не найден для обновления"));
    }

    public void deleteStudent(Long idStudent) {
        studentRepository.deleteById(idStudent);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public List<Student> getStudentsByAge(int age) {
        return studentRepository.findByAge(age);
    }
}
