package ru.hogwarts.school;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.hogwarts.school.controller.FacultyController;
import ru.hogwarts.school.model.Faculty;
import ru.hogwarts.school.repository.FacultyRepository;
import ru.hogwarts.school.service.FacultyService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    public void shouldUpdareFaculty() throws Exception {
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

    @Test
    public void shouldDeleteFaculty() {

    }







}
