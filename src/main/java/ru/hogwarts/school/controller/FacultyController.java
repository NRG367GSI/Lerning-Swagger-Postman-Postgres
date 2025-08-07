package ru.hogwarts.school.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.service.FacultyService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @PostMapping("/createdFaculty")
    public ResponseEntity<Faculty> createdFaculty(@RequestBody Faculty faculty) {
        Faculty createdFaculty = facultyService.createFaculty(faculty);
        return ResponseEntity.created(URI.create("/faculty/" + createdFaculty.getFacultyId()))
                .body(createdFaculty);
    }

    @GetMapping("/getFaculty/{facultyId}")
    public ResponseEntity<Faculty> getFaculty(@PathVariable Long facultyId) {
        Faculty faculty = facultyService.getFaculty(facultyId);
        if (faculty == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(faculty);
    }

    @PutMapping("/updateFaculty/{id}")
    public ResponseEntity<Faculty> updateFaculty(@PathVariable Long id, @RequestBody Faculty faculty) {
        Faculty updatedFaculty = facultyService.updateFaculty(id, faculty);
        return ResponseEntity.ok(updatedFaculty);
    }

    @DeleteMapping("/deleteFaculty/{facultyId}")
    public ResponseEntity<Void> deleteFaculty(@PathVariable Long facultyId) {
        facultyService.deleteFaculty(facultyId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/color/{color}")
    public ResponseEntity<List<Faculty>> getFacultysByColor(@PathVariable String color) {
        return ResponseEntity.ok(facultyService.getFacultysByColor(color));
    }

    @GetMapping("/getAllFaculty")
    public ResponseEntity<List<Faculty>> getAllFaculty() {
        return ResponseEntity.ok(facultyService.getAllFaculty());
    }

    @GetMapping("/searchFacultyByName")
    public ResponseEntity<List<Faculty>> searchFacultiesByName(@RequestParam String name) {
        return ResponseEntity.ok(facultyService.searchFacultiesByName(name));
    }

    @GetMapping("/searchFacultyByColor")
    public ResponseEntity<List<Faculty>> searchFacultiesByColor(@RequestParam String color) {
        return ResponseEntity.ok(facultyService.getFacultysByColor(color));
    }

    @GetMapping("/{facultyId}/students")
    public ResponseEntity<List<Student>> getFacultyStudents(@PathVariable Long facultyId) {
        List<Student> students = facultyService.getStudentsByFacultyId(facultyId);
        return ResponseEntity.ok(students);
    }
}
