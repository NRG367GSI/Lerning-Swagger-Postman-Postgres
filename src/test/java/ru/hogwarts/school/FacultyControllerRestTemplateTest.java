package ru.hogwarts.school;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.Faculty;

import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class FacultyControllerRestTemplateTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private FacultyRepository facultyRepository;

    @Autowired
    private StudentRepository studentRepository;

    private static final String FACULTY_BASE_ENDPOINT = "/faculty";

    private static final String STUDENT_BASE_ENDPOINT = "/student";

    @Test
    void testGetFacultyById() {
        Faculty facultyToCreate = new Faculty();
        facultyToCreate.setName("Hogwarts School");
        facultyToCreate.setColor("Grey");
        ResponseEntity<Faculty> createFacultyResponse = restTemplate.postForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty",
                facultyToCreate,
                Faculty.class
        );
        assertThat(createFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Faculty createdFaculty = createFacultyResponse.getBody();
        assertThat(createdFaculty).isNotNull();
        Long facultyId = createdFaculty.getFacultyId();

        ResponseEntity<Faculty> getFacultyResponse = restTemplate.getForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/getFaculty/" + facultyId,
                Faculty.class
        );

        assertThat(getFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Faculty retrievedFaculty = getFacultyResponse.getBody();
        assertThat(retrievedFaculty).isNotNull();
        assertThat(retrievedFaculty.getFacultyId()).isEqualTo(facultyId);
        assertThat(retrievedFaculty.getName()).isEqualTo("Hogwarts School");
        assertThat(retrievedFaculty.getColor()).isEqualTo("Grey");
    }

    @Test
    void testGetFacultyByIdNotFound() {
        ResponseEntity<Faculty> getFacultyResponse = restTemplate.getForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/getFaculty/99999", // ID, которого точно нет
                Faculty.class
        );

        assertThat(getFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    void testUpdateFaculty() {
        Faculty facultyToCreate = new Faculty();
        facultyToCreate.setName("Initial Faculty Name");
        facultyToCreate.setColor("Initial Color");
        ResponseEntity<Faculty> createFacultyResponse = restTemplate.postForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty",
                facultyToCreate,
                Faculty.class
        );
        assertThat(createFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Faculty createdFaculty = createFacultyResponse.getBody();
        assertThat(createdFaculty).isNotNull();
        Long facultyId = createdFaculty.getFacultyId();

        Faculty facultyToUpdate = new Faculty();
        facultyToUpdate.setName("Updated Faculty Name");
        facultyToUpdate.setColor("Updated Color");

        ResponseEntity<Faculty> updateFacultyResponse = restTemplate.exchange(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/" + facultyId,
                HttpMethod.PUT,
                new HttpEntity<>(facultyToUpdate),
                Faculty.class
        );

        assertThat(updateFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        Faculty updatedFaculty = updateFacultyResponse.getBody();
        assertThat(updatedFaculty).isNotNull();
        assertThat(updatedFaculty.getFacultyId()).isEqualTo(facultyId);
        assertThat(updatedFaculty.getName()).isEqualTo("Updated Faculty Name");
        assertThat(updatedFaculty.getColor()).isEqualTo("Updated Color");

        ResponseEntity<Faculty> verifyFacultyResponse = restTemplate.getForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/getFaculty/" + facultyId,
                Faculty.class
        );
        assertThat(verifyFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(verifyFacultyResponse.getBody().getName()).isEqualTo("Updated Faculty Name");
        assertThat(verifyFacultyResponse.getBody().getColor()).isEqualTo("Updated Color");
    }

    @Test
    void testUpdateFacultyNotFound() {
        Faculty facultyToUpdate = new Faculty();
        facultyToUpdate.setName("NonExistent Faculty");
        facultyToUpdate.setColor("NonExistent Color");

        ResponseEntity<Faculty> updateFacultyResponse = restTemplate.exchange(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/99999",
                HttpMethod.PUT,
                new HttpEntity<>(facultyToUpdate),
                Faculty.class
        );

        assertThat(updateFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testDeleteFaculty() {
        Faculty facultyToCreate = new Faculty();
        facultyToCreate.setName("Faculty to Delete");
        facultyToCreate.setColor("Black");
        ResponseEntity<Faculty> createFacultyResponse = restTemplate.postForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty",
                facultyToCreate,
                Faculty.class
        );
        assertThat(createFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Faculty createdFaculty = createFacultyResponse.getBody();
        assertThat(createdFaculty).isNotNull();
        Long facultyId = createdFaculty.getFacultyId();

        restTemplate.delete("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/deleteFaculty/" + facultyId);

        ResponseEntity<Faculty> verifyDeleteResponse = restTemplate.getForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/getFaculty/" + facultyId,
                Faculty.class
        );
        assertThat(verifyDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void testGetFacultysByColor() {
        Faculty faculty1 = new Faculty();
        faculty1.setName("Gryffindor");
        faculty1.setColor("Red");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty1, Faculty.class);

        Faculty faculty2 = new Faculty();
        faculty2.setName("Slytherin");
        faculty2.setColor("Green");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty2, Faculty.class);

        Faculty faculty3 = new Faculty();
        faculty3.setName("Another Red Faculty");
        faculty3.setColor("Red");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty3, Faculty.class);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/color/Red",
                Faculty[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Faculty> facultiesByColor = Arrays.asList(response.getBody());
        assertThat(facultiesByColor).hasSize(2); // Ожидаем 2 факультета красного цвета
        assertThat(facultiesByColor).extracting(Faculty::getName).containsExactlyInAnyOrder("Gryffindor", "Another Red Faculty");
        assertThat(facultiesByColor).allMatch(f -> f.getColor().equals("Red"));
    }

    @Test
    void testGetAllFaculty() {
        Faculty faculty1 = new Faculty();
        faculty1.setName("Faculty Alpha");
        faculty1.setColor("White");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty1, Faculty.class);

        Faculty faculty2 = new Faculty();
        faculty2.setName("Faculty Beta");
        faculty2.setColor("Black");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty2, Faculty.class);

        Faculty faculty3 = new Faculty();
        faculty3.setName("Faculty Gamma");
        faculty3.setColor("Gray");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty3, Faculty.class);

        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/getAllFaculty",
                Faculty[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Faculty> allFaculties = Arrays.asList(response.getBody());
        assertThat(allFaculties).hasSize(3);
        assertThat(allFaculties).extracting(Faculty::getName).containsExactlyInAnyOrder("Faculty Alpha", "Faculty Beta", "Faculty Gamma");
    }

    @Test
    void testSearchFacultiesByName() {
        Faculty faculty1 = new Faculty();
        faculty1.setName("History Department");
        faculty1.setColor("Brown");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty1, Faculty.class);

        Faculty faculty2 = new Faculty();
        faculty2.setName("Science Faculty");
        faculty2.setColor("Blue");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty2, Faculty.class);

        Faculty faculty3 = new Faculty();
        faculty3.setName("Art History Institute");
        faculty3.setColor("Yellow");
        restTemplate.postForEntity("http://localhost:" + port + STUDENT_BASE_ENDPOINT + "/createdStudent", faculty3, Faculty.class);

        String url = "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/searchFacultyByName?name=History";
        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(url, Faculty[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Faculty> foundFaculties = Arrays.asList(response.getBody());
        assertThat(foundFaculties).hasSize(2);
        assertThat(foundFaculties).extracting(Faculty::getName).containsExactlyInAnyOrder("History Department", "Art History Institute");
        assertThat(foundFaculties).allMatch(f -> f.getName().contains("History"));
    }

    @Test
    void testSearchFacultiesByColor() {
        Faculty faculty1 = new Faculty();
        faculty1.setName("Blue Faculty");
        faculty1.setColor("Blue");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty1, Faculty.class);

        Faculty faculty2 = new Faculty();
        faculty2.setName("Green Faculty");
        faculty2.setColor("Green");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty2, Faculty.class);

        Faculty faculty3 = new Faculty();
        faculty3.setName("Another Blue Faculty");
        faculty3.setColor("Blue");
        restTemplate.postForEntity("http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty", faculty3, Faculty.class);

        String url = "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/searchFacultyByColor?color=Blue";
        ResponseEntity<Faculty[]> response = restTemplate.getForEntity(url, Faculty[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Faculty> foundFaculties = Arrays.asList(response.getBody());
        assertThat(foundFaculties).hasSize(2);
        assertThat(foundFaculties).extracting(Faculty::getName).containsExactlyInAnyOrder("Blue Faculty", "Another Blue Faculty");
        assertThat(foundFaculties).allMatch(f -> f.getColor().equals("Blue"));
    }

    @Test
    void testGetFacultyStudents() {
        Faculty createdFaculty = new Faculty();
        createdFaculty.setName("School of Witchcraft");
        createdFaculty.setColor("Brown");
        ResponseEntity<Faculty> facultyResponse = restTemplate.postForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/createdFaculty",
                createdFaculty,
                Faculty.class
        );
        assertThat(facultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Faculty savedFaculty = facultyResponse.getBody();
        assertThat(savedFaculty).isNotNull();

        Student student1 = new Student();
        student1.setName("Student A");
        student1.setAge(15);
        student1.setFaculty(savedFaculty);
        ResponseEntity<Student> studentResponse1 = restTemplate.postForEntity(
                "http://localhost:" + port + STUDENT_BASE_ENDPOINT + "/createdStudent",
                student1,
                Student.class
        );
        assertThat(studentResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(studentResponse1.getBody()).isNotNull();


        Student student2 = new Student();
        student2.setName("Student B");
        student2.setAge(16);
        student2.setFaculty(savedFaculty);
        ResponseEntity<Student> studentResponse2 = restTemplate.postForEntity(
                "http://localhost:" + port + STUDENT_BASE_ENDPOINT + "/createdStudent",
                student2,
                Student.class
        );
        assertThat(studentResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(studentResponse2.getBody()).isNotNull();

        ResponseEntity<Student[]> response = restTemplate.getForEntity(
                "http://localhost:" + port + FACULTY_BASE_ENDPOINT + "/" + savedFaculty.getFacultyId() + "/students",
                Student[].class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        List<Student> facultyStudents = Arrays.asList(response.getBody());
        assertThat(facultyStudents).hasSize(2);
        assertThat(facultyStudents).extracting(Student::getName).containsExactlyInAnyOrder("Student A", "Student B");
        assertThat(facultyStudents).allMatch(s -> s.getFaculty().getFacultyId().equals(savedFaculty.getFacultyId()));
    }


}