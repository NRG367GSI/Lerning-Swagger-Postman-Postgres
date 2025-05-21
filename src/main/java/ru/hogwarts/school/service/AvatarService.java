package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exception.StudentNotFoundException;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import java.nio.file.Path;
import java.util.Optional;

import static io.swagger.v3.core.util.AnnotationsUtils.getExtensions;

@Service
@Transactional
public class AvatarService {

    @Value("${path.to.avatars.folder}")
    private String avatarsDir;

    @Autowired
    private final AvatarRepository avatarRepository;
    @Autowired
    private final StudentRepository studentRepository;

    public AvatarService(AvatarRepository avatarRepository, StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
    }

    private String getExtensions(String fileName) {
        return Optional.ofNullable(fileName)
                .filter(f -> f.contains("."))
                .map(f -> f.substring(fileName.lastIndexOf(".") + 1))
                .orElse("");
    }

    public void uploadAvatar(Long studentId, MultipartFile avatarFile) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Студент с ID " + studentId + " не найден для загрузки аватара."));
        Path filePath = Path.of(avatarsDir, student.getId() + "." + getExtensions(avatarFile.getOriginalFilename()));

    }
}
