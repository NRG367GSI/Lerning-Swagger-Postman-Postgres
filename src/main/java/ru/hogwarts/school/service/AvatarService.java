package ru.hogwarts.school.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.exception.AvatarNotFoundException;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import ru.hogwarts.school.exception.StudentNotFoundException;

import jakarta.annotation.PostConstruct;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Optional;

import static java.nio.file.StandardOpenOption.*;


@Service
@Transactional
public class AvatarService {

    @Value("${path.to.avatars.folder}")
    private String avatarsDir;

    @Value("${avatar.miniature.width}")
    private int avatarMiniatureWidth;

    @Value("${avatar.miniature.height}")
    private int avatarMiniatureHeight;

    private final AvatarRepository avatarRepository;
    private final StudentRepository studentRepository;

    public AvatarService(AvatarRepository avatarRepository, StudentRepository studentRepository) {
        this.avatarRepository = avatarRepository;
        this.studentRepository = studentRepository;
    }

    @PostConstruct
    public void initAvatarDirectory() {
        if (avatarsDir == null || avatarsDir.trim().isEmpty()) {
            throw new IllegalArgumentException("Свойство 'path.to.avatars.folder' должно быть указано в application.properties.");
        }
        try {
            Path avatarFolderPath = Path.of(avatarsDir);
            Files.createDirectories(avatarFolderPath);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось инициализировать директорию для аватаров по пути: " + avatarsDir, e);
        }
    }


    public void uploadStudentAvatar(Long studentId, MultipartFile file) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student not found with ID: " + studentId));

        Optional<Avatar> optionalAvatar = avatarRepository.findByAvatarId(studentId);

        String originalFilename = file.getOriginalFilename();
        String nameNewAvatar = "";
        if (originalFilename != null) {
            nameNewAvatar = createNewName(originalFilename, studentId);
        }
        Path pathNewFile = Path.of(avatarsDir, nameNewAvatar);



        if (optionalAvatar.isPresent()) {
            Avatar oldAvatar = optionalAvatar.get();
            String pathOldAvatar = oldAvatar.getFilePath();
            Files.deleteIfExists(Path.of(pathOldAvatar));
            saveAvatarFile(pathNewFile, file);
            oldAvatar.setFileName(nameNewAvatar);
            oldAvatar.setFileSize(file.getSize());
            oldAvatar.setData(createThumbnail(avatarMiniatureWidth, avatarMiniatureHeight, file));
            oldAvatar.setFilePath(pathNewFile.toString());
            oldAvatar.setMediaType(file.getContentType());
            avatarRepository.save(oldAvatar);
        } else {
            Avatar newAvatar = new Avatar();
            saveAvatarFile(pathNewFile, file);
            newAvatar.setFileName(nameNewAvatar);
            newAvatar.setFileSize(file.getSize());
            newAvatar.setData(createThumbnail(avatarMiniatureWidth, avatarMiniatureHeight, file));
            newAvatar.setFilePath(pathNewFile.toString());
            newAvatar.setMediaType(file.getContentType());
            newAvatar.setStudent(student);
            avatarRepository.save(newAvatar);
        }


    }

    public void saveAvatarFile(Path path, MultipartFile avatarFile) throws IOException {
        try (
                InputStream is = avatarFile.getInputStream();
                OutputStream os = Files.newOutputStream(path, CREATE, WRITE);
                BufferedInputStream bis = new BufferedInputStream(is, 1024);
                BufferedOutputStream bos = new BufferedOutputStream(os, 1024);
        ) {
            bis.transferTo(bos);
        }
    }

    public String createNewName(String originalFileName, Long id) {
        String fileExtension = "";
        int dotIndex = originalFileName.lastIndexOf(".");
        if (dotIndex > 0 && dotIndex < originalFileName.length() - 1) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        return id.toString() + fileExtension;
    }

    public byte[] createThumbnail(int thumbnailWidth, int thumbnailHeight, MultipartFile imageFile) throws IOException {
        InputStream originalImageStream = imageFile.getInputStream();
        BufferedImage originalImage = ImageIO.read(originalImageStream);

        double originalWidth = originalImage.getWidth();
        double originalHeight = originalImage.getHeight();

        double scale = Math.min(thumbnailWidth/originalWidth, thumbnailHeight/originalHeight);

        int scaleWidth = (int) (originalWidth * scale);
        int scaleHeight = (int) (originalHeight * scale);

        BufferedImage thumbnail = new BufferedImage(scaleWidth, scaleHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D paintingImageObject = thumbnail.createGraphics();

        paintingImageObject.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        paintingImageObject.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        paintingImageObject.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintingImageObject.drawImage(originalImage, 0, 0, scaleWidth, scaleHeight, null);
        paintingImageObject.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(thumbnail, "jpg", baos);
        return baos.toByteArray();
    }

    public byte[] getAvatarMiniature(Long studentId) {
        return avatarRepository.findByAvatarId(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Avatar not found with ID: " + studentId))
                .getData();
    }

    public byte[] getAvatarFile(Long studentId) throws IOException {
        String filePath = avatarRepository.findByAvatarId(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Avatar not found with ID: " + studentId))
                .getFilePath();

        Path pathToFile = Path.of(filePath);
        if (Files.notExists(pathToFile)) {
            throw new FileNotFoundException("Avatar file not found on disk: " + filePath);
        }

        return Files.readAllBytes(pathToFile);
    }

    public MediaType getMediaType(Long studentId) {
        String typeMiniature =  avatarRepository.findByAvatarId(studentId)
                .orElseThrow(() -> new AvatarNotFoundException("Student not found with ID: " + studentId)).getMediaType();
        return MediaType.parseMediaType(typeMiniature);
    }





}