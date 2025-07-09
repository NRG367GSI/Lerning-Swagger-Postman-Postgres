package ru.hogwarts.school;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

		ResponseEntity<Student> getStudentResponse = restTemplate.getForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/getStudent/" + studentId,
				Student.class
		);

		assertThat(getStudentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		Student retrievedStudent = getStudentResponse.getBody();
		assertThat(retrievedStudent).isNotNull();
		assertThat(retrievedStudent.getId()).isEqualTo(studentId);
		assertThat(retrievedStudent.getName()).isEqualTo("Hermione");
		assertThat(retrievedStudent.getAge()).isEqualTo(16);
		assertThat(retrievedStudent.getFaculty()).isNotNull();
		assertThat(retrievedStudent.getFaculty().getFacultyId()).isEqualTo(createdFaculty.getFacultyId());
	}

	@Test
	void testUpdateStudent() {
		Faculty initialFaculty = new Faculty();
		initialFaculty.setName("Ravenclaw");
		initialFaculty.setColor("Blue");
		ResponseEntity<Faculty> initialFacultyResponse = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				initialFaculty,
				Faculty.class
		);
		assertThat(initialFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty createdInitialFaculty = initialFacultyResponse.getBody();
		assertThat(createdInitialFaculty).isNotNull();

		Student studentToCreate = new Student();
		studentToCreate.setName("Ron");
		studentToCreate.setAge(17);
		studentToCreate.setFaculty(createdInitialFaculty);

		ResponseEntity<Student> createStudentResponse = restTemplate.postForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT,
				studentToCreate,
				Student.class
		);
		assertThat(createStudentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Student createdStudent = createStudentResponse.getBody();
		assertThat(createdStudent).isNotNull();
		Long studentId = createdStudent.getId();

		Faculty newFaculty = new Faculty();
		newFaculty.setName("Slytherin");
		newFaculty.setColor("Green");
		ResponseEntity<Faculty> newFacultyResponse = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				newFaculty,
				Faculty.class
		);
		assertThat(newFacultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty createdNewFaculty = newFacultyResponse.getBody();
		assertThat(createdNewFaculty).isNotNull();

		Student studentToUpdate = new Student();
		studentToUpdate.setName("Ronald Weasley"); // Новое имя
		studentToUpdate.setAge(18); // Новый возраст
		studentToUpdate.setFaculty(createdNewFaculty); // Новый факультет

		ResponseEntity<Student> updateStudentResponse = restTemplate.exchange(
				"http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/updateStudent/" + studentId,
				HttpMethod.PUT,
				new HttpEntity<>(studentToUpdate), // Тело запроса
				Student.class
		);

		assertThat(updateStudentResponse.getStatusCode()).isEqualTo(HttpStatus.OK); // Ожидаем 200 OK
		Student updatedStudent = updateStudentResponse.getBody();
		assertThat(updatedStudent).isNotNull();
		assertThat(updatedStudent.getId()).isEqualTo(studentId); // ID должен остаться прежним
		assertThat(updatedStudent.getName()).isEqualTo("Ronald Weasley");
		assertThat(updatedStudent.getAge()).isEqualTo(18);
		assertThat(updatedStudent.getFaculty()).isNotNull();
		assertThat(updatedStudent.getFaculty().getFacultyId()).isEqualTo(createdNewFaculty.getFacultyId());

		ResponseEntity<Student> verifyStudentResponse = restTemplate.getForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/getStudent/" + studentId,
				Student.class
		);
		assertThat(verifyStudentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(verifyStudentResponse.getBody().getName()).isEqualTo("Ronald Weasley");
		assertThat(verifyStudentResponse.getBody().getAge()).isEqualTo(18);
		assertThat(verifyStudentResponse.getBody().getFaculty().getFacultyId()).isEqualTo(createdNewFaculty.getFacultyId());
	}

	@Test
	void testDeleteStudent() {
		Faculty createdFaculty = new Faculty();
		createdFaculty.setName("Gryffindor");
		createdFaculty.setColor("Red");
		ResponseEntity<Faculty> facultyResponse = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				createdFaculty,
				Faculty.class
		);
		assertThat(facultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty savedFaculty = facultyResponse.getBody();
		assertThat(savedFaculty).isNotNull();

		Student studentToCreate = new Student();
		studentToCreate.setName("Draco");
		studentToCreate.setAge(16);
		studentToCreate.setFaculty(savedFaculty);

		ResponseEntity<Student> createStudentResponse = restTemplate.postForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT,
				studentToCreate,
				Student.class
		);
		assertThat(createStudentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Student createdStudent = createStudentResponse.getBody();
		assertThat(createdStudent).isNotNull();
		Long studentId = createdStudent.getId();

		restTemplate.delete("http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/deleteStudent/" + studentId);

		ResponseEntity<Student> verifyDeleteResponse = restTemplate.getForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/getStudent/" + studentId,
				Student.class
		);
		assertThat(verifyDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void testGetStudentsByAge() {
		Faculty faculty1 = new Faculty();
		faculty1.setName("FacultyA");
		faculty1.setColor("ColorA");
		ResponseEntity<Faculty> facultyResponse1 = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				faculty1,
				Faculty.class
		);
		assertThat(facultyResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty savedFaculty1 = facultyResponse1.getBody();
		assertThat(savedFaculty1).isNotNull();

		Faculty faculty2 = new Faculty();
		faculty2.setName("FacultyB");
		faculty2.setColor("ColorB");
		ResponseEntity<Faculty> facultyResponse2 = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				faculty2,
				Faculty.class
		);
		assertThat(facultyResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty savedFaculty2 = facultyResponse2.getBody();
		assertThat(savedFaculty2).isNotNull();

		Student student1 = new Student();
		student1.setName("Student1");
		student1.setAge(18);
		student1.setFaculty(savedFaculty1);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student1, Student.class);

		Student student2 = new Student();
		student2.setName("Student2");
		student2.setAge(20);
		student2.setFaculty(savedFaculty2);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student2, Student.class);

		Student student3 = new Student();
		student3.setName("Student3");
		student3.setAge(18);
		student3.setFaculty(savedFaculty1);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student3, Student.class);

		ResponseEntity<Student[]> response = restTemplate.getForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/getStudentsByAge/18",
				Student[].class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		List<Student> studentsByAge = Arrays.asList(response.getBody());
		assertThat(studentsByAge).hasSize(2); // Ожидаем 2 студентов
		assertThat(studentsByAge).extracting(Student::getName).containsExactlyInAnyOrder("Student1", "Student3");
		assertThat(studentsByAge).allMatch(s -> s.getAge() == 18);
	}

	@Test
	void testGetStudentsByAgeBetween() {
		Faculty faculty = new Faculty();
		faculty.setName("TestFaculty");
		faculty.setColor("TestColor");
		ResponseEntity<Faculty> facultyResponse = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				faculty,
				Faculty.class
		);
		assertThat(facultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty savedFaculty = facultyResponse.getBody();
		assertThat(savedFaculty).isNotNull();

		Student student1 = new Student();
		student1.setName("OldStudent");
		student1.setAge(25);
		student1.setFaculty(savedFaculty);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student1, Student.class);

		Student student2 = new Student();
		student2.setName("MidStudent");
		student2.setAge(18);
		student2.setFaculty(savedFaculty);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student2, Student.class);

		Student student3 = new Student();
		student3.setName("YoungStudent");
		student3.setAge(15);
		student3.setFaculty(savedFaculty);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student3, Student.class);

		Student student4 = new Student();
		student4.setName("AnotherMidStudent");
		student4.setAge(20);
		student4.setFaculty(savedFaculty);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student4, Student.class);

		String url = "http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/age/between?minAge=17&maxAge=21";
		ResponseEntity<Student[]> response = restTemplate.getForEntity(url, Student[].class);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		List<Student> students = Arrays.asList(response.getBody());
		assertThat(students).hasSize(2);
		assertThat(students).extracting(Student::getName).containsExactlyInAnyOrder("MidStudent", "AnotherMidStudent");
		assertThat(students).allMatch(s -> s.getAge() >= 17 && s.getAge() <= 21);
	}

	@Test
	void testGetStudentFaculty() {
		Faculty createdFaculty = new Faculty();
		createdFaculty.setName("Gryffindor");
		createdFaculty.setColor("Red");
		ResponseEntity<Faculty> facultyResponse = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				createdFaculty,
				Faculty.class
		);
		assertThat(facultyResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty savedFaculty = facultyResponse.getBody();
		assertThat(savedFaculty).isNotNull();

		Student createdStudent = new Student();
		createdStudent.setName("Harry");
		createdStudent.setAge(14);
		createdStudent.setFaculty(savedFaculty);
		ResponseEntity<Student> studentResponse = restTemplate.postForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT,
				createdStudent,
				Student.class
		);
		assertThat(studentResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Student savedStudent = studentResponse.getBody();
		assertThat(savedStudent).isNotNull();

		ResponseEntity<Faculty> response = restTemplate.getForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/" + savedStudent.getId() + "/faculty",
				Faculty.class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		Faculty retrievedFaculty = response.getBody();
		assertThat(retrievedFaculty).isNotNull();
		assertThat(retrievedFaculty.getFacultyId()).isEqualTo(savedFaculty.getFacultyId());
		assertThat(retrievedFaculty.getName()).isEqualTo(savedFaculty.getName());
		assertThat(retrievedFaculty.getColor()).isEqualTo(savedFaculty.getColor());
	}



	@Test
	void testGetAllStudent() {
		Faculty faculty1 = new Faculty();
		faculty1.setName("FacultyA");
		faculty1.setColor("ColorA");
		ResponseEntity<Faculty> facultyResponse1 = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				faculty1,
				Faculty.class
		);
		assertThat(facultyResponse1.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty savedFaculty1 = facultyResponse1.getBody();
		assertThat(savedFaculty1).isNotNull();

		Faculty faculty2 = new Faculty();
		faculty2.setName("FacultyB");
		faculty2.setColor("ColorB");
		ResponseEntity<Faculty> facultyResponse2 = restTemplate.postForEntity(
				"http://localhost:" + port + FACULTY_ENDPOINT,
				faculty2,
				Faculty.class
		);
		assertThat(facultyResponse2.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		Faculty savedFaculty2 = facultyResponse2.getBody();
		assertThat(savedFaculty2).isNotNull();

		Student student1 = new Student();
		student1.setName("Student A");
		student1.setAge(10);
		student1.setFaculty(savedFaculty1);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student1, Student.class);

		Student student2 = new Student();
		student2.setName("Student B");
		student2.setAge(12);
		student2.setFaculty(savedFaculty2);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student2, Student.class);

		Student student3 = new Student();
		student3.setName("Student C");
		student3.setAge(11);
		student3.setFaculty(savedFaculty1);
		restTemplate.postForEntity("http://localhost:" + port + STUDENT_ENDPOINT, student3, Student.class);

		ResponseEntity<Student[]> response = restTemplate.getForEntity(
				"http://localhost:" + port + STUDENT_ENDPOINT.replace("/createdStudent", "") + "/getFullStudent",
				Student[].class
		);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isNotNull();
		List<Student> allStudents = Arrays.asList(response.getBody());
		assertThat(allStudents).hasSize(3);
		assertThat(allStudents).extracting(Student::getName).containsExactlyInAnyOrder("Student A", "Student B", "Student C");
	}




}
