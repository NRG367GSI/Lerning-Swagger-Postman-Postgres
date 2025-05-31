import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.imgscalr.Scalr;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.model.Student;
import ru.hogwarts.school.repository.AvatarRepository;
import ru.hogwarts.school.repository.StudentRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

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

        try {
            Files.createDirectories(Paths.get(avatarsDir));
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию для загрузки аватаров: " + avatarsDir + " - " + e.getMessage());
        }
    }

    public Avatar uploadStudentAvatar(Long studentId, MultipartFile file) throws IOException {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        Avatar avatar = findOrCreateAvatarEntity(studentId, student);

        deleteOldAvatarFile(avatar.getFilePath());

        Path newFullSizeFilePath = saveFullSizeAvatarFile(file);

        byte[] miniatureData = generateAvatarMiniature(file);

        updateAvatarEntityFields(avatar, file, newFullSizeFilePath, miniatureData);

        return avatarRepository.save(avatar);
    }

    private Avatar findOrCreateAvatarEntity(Long studentId, Student student) {
        Optional<Avatar> existingAvatarOpt = avatarRepository.findByStudentId(studentId);
        Avatar avatar;
        if (existingAvatarOpt.isPresent()) {
            avatar = existingAvatarOpt.get();
        } else {
            avatar = new Avatar();
            avatar.setStudent(student);
        }
        return avatar;
    }

    private void deleteOldAvatarFile(String filePath) {
        if (filePath != null && !filePath.isEmpty()) {
            Path oldAvatarPath = Paths.get(filePath);
            try {
                boolean deleted = Files.deleteIfExists(oldAvatarPath);
                if (deleted) {
                    System.out.println("Старый полноразмерный аватар успешно удален с диска: " + oldAvatarPath);
                } else {
                    System.out.println("Старый полноразмерный аватар не найден на диске для удаления (возможно, уже удален): " + oldAvatarPath);
                }
            } catch (IOException e) {
                System.err.println("Ошибка при удалении старого полноразмерного аватара " + oldAvatarPath + " с диска: " + e.getMessage());
            }
        }
    }

    private Path saveFullSizeAvatarFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;
        Path newFilePath = Paths.get(avatarsDir, uniqueFileName);

        try (var inputStream = file.getInputStream();
             var outputStream = Files.newOutputStream(newFilePath, CREATE, WRITE, TRUNCATE_EXISTING)) {
            inputStream.transferTo(outputStream);
            System.out.println("Новый полноразмерный аватар успешно сохранен на диск: " + newFilePath);
            return newFilePath;
        } catch (IOException e) {
            System.err.println("Ошибка при сохранении нового полноразмерного аватара на диск: " + newFilePath + " - " + e.getMessage());
            throw e;
        }
    }

    private byte[] generateAvatarMiniature(MultipartFile originalFile) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(originalFile.getBytes())) {
            BufferedImage originalImage = ImageIO.read(bais);
            if (originalImage == null) {
                System.err.println("Ошибка: Загруженный файл не является распознаваемым изображением.");
                throw new IOException("Загруженный файл не является распознаваемым изображением.");
            }

            BufferedImage resizedImage = Scalr.resize(originalImage, Scalr.Method.AUTOMATIC, Scalr.Mode.FIT_TO_WIDTH, avatarMiniatureWidth, avatarMiniatureHeight, Scalr.OP_ANTIALIAS);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                String format = originalFile.getContentType().startsWith("image/") ?
                        originalFile.getContentType().substring(originalFile.getContentType().indexOf('/') + 1) : "png";

                ImageIO.write(resizedImage, format, baos);
                System.out.println("Миниатюра аватара успешно сгенерирована.");
                return baos.toByteArray();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при генерации миниатюры аватара: " + e.getMessage());
            throw e;
        }
    }

    private void updateAvatarEntityFields(Avatar avatar, MultipartFile file, Path newFullSizeFilePath, byte[] miniatureData) {
        avatar.setFilePath(newFullSizeFilePath.toString());
        avatar.setFileName(file.getOriginalFilename());
        avatar.setFileSize(file.getSize());
        avatar.setMediaType(file.getContentType());
        avatar.setData(miniatureData);
    }
}