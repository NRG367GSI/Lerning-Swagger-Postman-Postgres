package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.exception.InvalidAgeRangeException;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.StudentService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/createStudent")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        Student createdStudent = studentService.createStudent(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdStudent);
    }

    @GetMapping("/getStudent/{studentId}")
    public ResponseEntity<Student> getStudent(@PathVariable Long studentId) {
        Student student = studentService.getStudent(studentId);
        return ResponseEntity.ok(student);
    }

    @PutMapping("/updateStudent/{studentId}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long studentId, @RequestBody Student student) {
        Student updatedStudent = studentService.updateStudent(studentId, student);
        if (updatedStudent == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedStudent);
    }

    @DeleteMapping("/deleteStudent/{studentId}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getStudentsByAge/{age}")
    public ResponseEntity<List<Student>> getStudentsByAge(@PathVariable int age) {
        List<Student> students = studentService.getStudentsByAge(age);
        if (students.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(students);
    }

    @GetMapping("/age/between")
    public ResponseEntity<List<Student>> getStudentsByAgeBetween(
            @RequestParam int minAge,
            @RequestParam int maxAge) {
        List<Student> students = studentService.findByStudentAgeBeatvin(minAge, maxAge);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{studentId}/faculty")
    public ResponseEntity<Faculty> getStudentFaculty(@PathVariable Long studentId) {
        return studentService.getStudentsFaculty(studentId)
                .map(faculty -> new ResponseEntity<>(faculty, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/getFullStudent")
    public ResponseEntity<List<Student>> getAllStudent() {
        return new ResponseEntity<>(studentService.getFullStudent(), HttpStatus.OK);
    }

    @GetMapping("/getFullStudentId")
    public ResponseEntity<List<Long>> getAllStudentId() {
        return new ResponseEntity<>(studentService.getFullIdStudent(), HttpStatus.OK);
    }
}
