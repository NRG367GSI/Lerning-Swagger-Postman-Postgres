package ru.hogwarts.school.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;

@RestController
@RequestMapping("/faculty")
public class FacultyController {

    FacultyService facultyService;

    @Autowired
    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }


    @PostMapping("/createdFaculty")
    public ResponseEntity<Faculty> createdFaculty(@RequestBody Faculty faculty) {
        Faculty createdFaculty = facultyService.createFaculty(faculty);
        return ResponseEntity.ok(createdFaculty);
    }

    @GetMapping("/getFaculty/{facultyId}")
    public ResponseEntity<Faculty> getFaculty(Long facultyId) {
        Faculty faculty = facultyService.getFaculty(facultyId);
        if (faculty == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(faculty);
    }

    @PutMapping("/updateFaculty/{facultyId}")
    public ResponseEntity<Faculty> updateFaculty(Faculty faculty) {
        Faculty updateFaculty = facultyService.updateFaculty(faculty);
        if (updateFaculty == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updateFaculty);
    }

    @DeleteMapping("/deleteFaculty")
    public ResponseEntity<Faculty> deleteFaculty(Long facultyId) {
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
}
