package ru.hogwarts.school;
import org.assertj.core.api.Assertions;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
class StudentSchoolApplicationTests {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private FacultyRepository facultyRepository;

	@Autowired
	private StudentRepository studentRepository;

	private static final String FACULTY_ENDPOINT = "/faculty/createdFaculty";
	private static final String STUDENT_ENDPOINT = "/student/createdStudent";


	@Test
	public void testStudent() {
		studentRepository.deleteAll();
		facultyRepository.deleteAll();


		// 1. Создаем Faculty через REST API
		Faculty facultyToCreate = new Faculty();
		facultyToCreate.setName("Gryffindor");
		facultyToCreate.setColor("Red");

		ResponseEntity<Faculty> facultyResponse = this.restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				facultyToCreate,
				Faculty.class
		);

		assertThat(facultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty createdFaculty = facultyResponse.getBody();
		assertThat(createdFaculty).isNotNull();
		assertThat(createdFaculty.getFacultyId()).isNotNull(); // Проверяем, что ID сгенерирован
		System.out.println("Созданный Faculty ID через REST API: " + createdFaculty.getFacultyId());


		// 2. Используем созданный Faculty для Student
		Faculty facultyForStudentRequest = new Faculty();
		facultyForStudentRequest.setFacultyId(createdFaculty.getFacultyId()); // Теперь точно используем ID из БД

		Student student = new Student();
		student.setId(null);
		student.setAge(15);
		student.setName("Potter");
		student.setFaculty(facultyForStudentRequest);

		ResponseEntity<Student> studentResponse = this.restTemplate.postForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT,
				student,
				Student.class
		);

		assertThat(studentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Student createdStudent = studentResponse.getBody();
		assertThat(createdStudent).isNotNull();
		assertThat(createdStudent.getId()).isNotNull();
		assertThat(createdStudent.getName()).isEqualTo("Potter");
		assertThat(createdStudent.getAge()).isEqualTo(15);
		assertThat(createdStudent.getFaculty()).isNotNull();
		assertThat(createdStudent.getFaculty().getFacultyId()).isEqualTo(createdFaculty.getFacultyId());
	}

	@Test
	void testGetStudentById() {
		// Arrange
		Faculty facultyToCreate = new Faculty();
		facultyToCreate.setName("Hufflepuff");
		facultyToCreate.setColor("Yellow");
		ResponseEntity<Faculty> facultyResponse = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				facultyToCreate,
				Faculty.class
		);
		assertThat(facultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty createdFaculty = facultyResponse.getBody();
		assertThat(createdFaculty).isNotNull();

		Student studentToCreate = new Student();
		studentToCreate.setName("Hermione");
		studentToCreate.setAge(16);
		studentToCreate.setFaculty(createdFaculty);

		ResponseEntity<Student> createStudentResponse = restTemplate.postForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT, // Используем STUDENT_ENDPOINT как есть
				studentToCreate,
				Student.class
		);
		assertThat(createStudentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Student createdStudent = createStudentResponse.getBody();
		assertThat(createdStudent).isNotNull();
		Long studentId = createdStudent.getId();

		// Act
		ResponseEntity<Student> getStudentResponse = restTemplate.getForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/getStudent/" + studentId,
				Student.class
		);

		// Assert
		assertThat(getStudentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		Student retrievedStudent = getStudentResponse.getBody();
		assertThat(retrievedStudent).isNotNull();
		assertThat(retrievedStudent.getId()).isEqualTo(studentId);
		assertThat(retrievedStudent.getName()).isEqualTo("Hermione");
		assertThat(retrievedStudent.getAge()).isEqualTo(16);
		assertThat(retrievedStudent.getFaculty()).isNotNull();
		assertThat(retrievedStudent.getFaculty().getFacultyId()).isEqualTo(createdFaculty.getFacultyId());
	}


}
