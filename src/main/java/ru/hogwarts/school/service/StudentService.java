package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.hogwarts.school.model.Student;

import java.util.List;
import java.util.Optional;

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

    public Optional<Student> getStudent(Long idStudent) {
        return studentRepository.findById(idStudent);
    }

    public Student updateStudent(Long studentId, Student student) {
        return studentRepository.save(student);
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
