package ru.hogwarts.school.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.exception.FacultyAlreadyExistsException;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;

import java.util.List;
import java.util.Optional;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;

    public FacultyService(FacultyRepository facultyRepository) {
        this.facultyRepository = facultyRepository;
    }

    public Faculty createFaculty(Faculty faculty) {
        if (faculty.getFacultyId() != null && facultyRepository.findById(faculty.getFacultyId()).isPresent()) {
            throw new FacultyAlreadyExistsException(
                    "Faculty with ID " + faculty.getFacultyId() + " already exists."
            );
        }
        return facultyRepository.save(faculty);
    }

    public Faculty getFaculty(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .orElseThrow(() -> new FacultyNotFoundException("Факультет с ID " + facultyId + " не найден"));
    }

    public Faculty updateFaculty(Long id, Faculty faculty) {
        return facultyRepository.findById(id)
                .map(existingFaculty -> {
                    faculty.setFacultyId(id);
                    return facultyRepository.save(faculty);
                })
                .orElseThrow(() -> new FacultyNotFoundException("Факультет с ID " + id + " не найден для обновления"));
    }

    public void deleteFaculty(Long facultyId) {
        if (facultyRepository.existsById(facultyId)) {
            facultyRepository.deleteById(facultyId);
        } else {
            throw new FacultyNotFoundException("Факультет с ID " + facultyId + " не найден для обновления");
        }
    }

    public List<Faculty> getFacultysByColor(String color) {
        return facultyRepository.findByColor(color);
    }

    public List<Faculty> getAllFaculty() {
        return facultyRepository.findAll();
    }

    public List<Faculty> searchFacultiesByName(String name) {
        return facultyRepository.findByNameContainingIgnoreCase(name);
    }

    public Optional<List<Student>> getStudentsByFacultyId(Long facultyId) {
        return facultyRepository.findById(facultyId)
                .map(Faculty::getStudents);
    }
}
