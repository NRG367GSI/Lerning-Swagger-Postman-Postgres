package ru.hogwarts.school;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.FacultyController;
import ru.hogwarts.school.exception.FacultyNotFoundException;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.service.FacultyService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FacultyController.class)
@Import(FacultyService.class)
public class FacultyControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FacultyRepository facultyRepository;

    @MockitoSpyBean
    private FacultyService facultyService;

    @Captor
    ArgumentCaptor<Faculty> facultyArgumentCaptor;

    private Faculty facultyToSend;
    private Faculty savedFacultyResponse;
    private String facultyJson;
    private Long facultyId;


    @BeforeEach
    public void setUp() throws JsonProcessingException {
        facultyToSend = new Faculty();
        facultyToSend.setFacultyId(null);
        facultyToSend.setName("testFaculty");
        facultyToSend.setColor("red");

        facultyJson = objectMapper.writeValueAsString(facultyToSend);

        savedFacultyResponse = new Faculty(1L, "testFaculty", "red");

        facultyId = 1L;
    }

    @Test
    void shouldCreatedFacultyTest() throws Exception {

        doReturn(savedFacultyResponse).when(facultyService).createFaculty(any(Faculty.class));

mockMvc.perform(post("/faculty/createdFaculty")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(facultyJson)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.facultyId").value(1L))
                .andExpect(jsonPath("$.name").value("testFaculty"))
                .andExpect(jsonPath("$.color").value("red"))
                .andExpect(header().string("Location", "/faculty/1"));


        verify(facultyService, times(1)).createFaculty(facultyArgumentCaptor.capture());

        Faculty captureFaculty = facultyArgumentCaptor.getValue();


        assertThat(captureFaculty.getFacultyId()).isNull();

        assertThat(captureFaculty.getName()).isEqualTo("testFaculty");

        assertThat(captureFaculty.getColor()).isEqualTo("red");

    }

    @Test
    void shouldGetFacultyById() throws Exception {

        doReturn(savedFacultyResponse).when(facultyService).getFaculty(anyLong());

        mockMvc.perform(get("/faculty/getFaculty/{facultyId}", facultyId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facultyId").value(1L))
                .andExpect(jsonPath("$.name").value("testFaculty"))
                .andExpect(jsonPath("$.color").value("red"));

        verify(facultyService, times(1)).getFaculty(facultyId);
    }

    @Test
    public void shouldUpdateFaculty() throws Exception {
        Faculty updateFacultySend = new Faculty(null, "updateFaculty", "blue");
        Faculty facultySaved = new Faculty(1L, "updateFaculty", "blue");
        String facultyUpdateJson = objectMapper.writeValueAsString(updateFacultySend);
        when(facultyRepository.findById(facultyId)).thenReturn(Optional.ofNullable(savedFacultyResponse));
        when(facultyRepository.save(any(Faculty.class))).thenReturn(facultySaved);

        mockMvc.perform(put("/faculty/updateFaculty/{id}", facultyId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(facultyUpdateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.facultyId").value(1L))
                .andExpect(jsonPath("$.name").value("updateFaculty"))
                .andExpect(jsonPath("$.color").value("blue"));

        verify(facultyRepository, times(1)).findById(facultyId);
        verify(facultyRepository, times(1)).save(facultySaved);
    }

    static Stream<Arguments> deleteFacultyTestCases() {
        return Stream.of(
                Arguments.of(1L, HttpStatus.NO_CONTENT, false),
                Arguments.of(99L, HttpStatus.NOT_FOUND, true)
        );
    }

    @ParameterizedTest
    @MethodSource("deleteFacultyTestCases")
    void shouldDeleteFaculty(Long facultyId, HttpStatus expectedStatus, boolean throwsException) throws Exception {

        if (throwsException) {

            doThrow(new FacultyNotFoundException("Факультет не найден"))
                    .when(facultyService)
                    .deleteFaculty(facultyId);

        } else {
            doNothing()
                    .when(facultyService)
                    .deleteFaculty(facultyId);
        }

        mockMvc.perform(delete("/faculty/deleteFaculty/{facultyId}", facultyId))
                .andDo(print())
                .andExpect(status().is(expectedStatus.value()));

        verify(facultyService, times(1)).deleteFaculty(facultyId);

    }

    @Test
    public void shouldGetFacultyByColor() throws Exception{

        String testColor = "red";
        Faculty faculty1 = new Faculty(1L, "Gryffindor", testColor);
        Faculty faculty2 = new Faculty(2L, "Slytherin", testColor);
        List<Faculty> expectedFaculties = List.of(faculty1, faculty2);

        String jsonListFaculty = objectMapper.writeValueAsString(expectedFaculties);


        when(facultyRepository.findByColor(testColor)).thenReturn(expectedFaculties);

        mockMvc.perform(get("/faculty/searchFacultyByColor").param("color", testColor))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonListFaculty));

        verify(facultyService, times(1)).getFacultysByColor(testColor);

        verify(facultyRepository, times(1)).findByColor(testColor);
    }

    @Test
    void shouldGetFacultyStudents() throws Exception {
        Long facultyId = 1L;
        Faculty faculty = new Faculty(facultyId, "Gryffindor", "red");
        Student student1 = new Student(101L, "Harry Potter", 12);
        Student student2 = new Student(102L, "Hermione Granger", 12);
        List<Student> expectedStudents = List.of(student1, student2);

        doReturn(expectedStudents)
                .when(facultyService)
                .getStudentsByFacultyId(facultyId);

        when(facultyRepository.findById(facultyId)).thenReturn(Optional.of(faculty));

        mockMvc.perform(get("/faculty/{facultyId}/students", facultyId))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(expectedStudents)));

        verify(facultyService, times(1)).getStudentsByFacultyId(facultyId);
    }







}
