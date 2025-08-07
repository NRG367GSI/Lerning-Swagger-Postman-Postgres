package ru.hogwarts.school.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.hogwarts.school.model.Avatar;
import ru.hogwarts.school.service.AvatarService;

import java.io.IOException;

@RestController
@RequestMapping("/avatars")
public class AvatarController {

    private final AvatarService avatarService;

    public AvatarController(AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    @PostMapping(value = "uploadAvatar/{studentId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadAvatar(@PathVariable Long studentId, @RequestParam MultipartFile multipartFile) throws IOException {
        avatarService.uploadStudentAvatar(studentId, multipartFile);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{studentId}/miniature")
    public ResponseEntity<byte[]> getMiniature(@PathVariable Long studentId) {
        byte[] miniatureBytes = avatarService.getAvatarMiniature(studentId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(avatarService.getMediaType(studentId));
        headers.setContentLength(miniatureBytes.length);
        return new ResponseEntity<>(miniatureBytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/{studentId}/avatarPicture")
    public ResponseEntity<byte[]> getAvatarPicture(@PathVariable Long studentId) throws IOException {
        byte[] miniatureBytes = avatarService.getAvatarFile(studentId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(avatarService.getMediaType(studentId));
        headers.setContentLength(miniatureBytes.length);
        return new ResponseEntity<>(miniatureBytes, headers, HttpStatus.OK);
    }
}