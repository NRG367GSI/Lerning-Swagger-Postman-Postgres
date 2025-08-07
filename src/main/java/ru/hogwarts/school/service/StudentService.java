package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.exception.InvalidAgeRangeException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudentService {
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository, FacultyRepository facultyRepository) {
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
    }

    public Student createStudent(Student student) {
        Long idFaculty = student.getFaculty().getFacultyId();
        Faculty facultyReference = facultyRepository.findById(idFaculty)
                .orElseThrow(() -> new FacultyNotFoundException("Факультет с ID " + idFaculty + " не найден"));
        student.setFaculty(facultyReference);
        return studentRepository.save(student);
    }

    public Student getStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Студент с ID " + studentId + " не найден"));
    }

    public Student updateStudent(Long studentId, Student student) {
        return studentRepository.findById(studentId)
                .map(existingStudent -> {
                    student.setId(studentId);
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

    public List<Student> findByStudentAgeBeatvin(int ageMin, int ageMax) {
        if (ageMin >= ageMax) {
            throw new InvalidAgeRangeException("Минимальный возраст должен быть меньше максимального.");
        }
        return studentRepository.findByAgeBetween(ageMin, ageMax);
    }

    @Transactional(readOnly = true)
    public Optional<Faculty> getStudentsFaculty(Long studentId) {
        Optional<Student> studentOptional = studentRepository.findById(studentId);
        return studentOptional.map(Student::getFaculty);
    }

    @Transactional(readOnly = true)
    public Optional<Faculty> getStudentFacultyId(Long studentId) {
        return studentRepository.findById(studentId)
                .map(student -> student.getFaculty() != null ? student.getFaculty() : null);
    }

    @Transactional(readOnly = true)
    public Optional<List<Student>> getStudentsByFaculty(Long studentId) {
        return studentRepository.findById(studentId)
                .map(student -> student.getFaculty() != null ? student.getFaculty().getStudents() : null);
    }

    @Transactional(readOnly = true)
    public List<Student> getFullStudent() {
        return studentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Long> getFullIdStudent() {
        return studentRepository.findAll().stream().map(Student::getId).collect(Collectors.toList());
    }
}