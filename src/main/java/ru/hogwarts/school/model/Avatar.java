package ru.hogwarts.school.model;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Objects;

@Entity
public class Avatar {
    @Id
    Long avatarId;

    @OneToOne
    @JoinColumn(name = "student_id")
    Student student;

    String filePath;
    String fileName;
    String mediaType;
    long fileSize;

    @Lob
    byte[] data;

    public Avatar() {
    }

    public Avatar(Long avatarId, Student student, String filePath, String mediaType, long fileSize, byte[] data) {
        this.avatarId = avatarId;
        this.student = student;
        this.filePath = filePath;
        this.mediaType = mediaType;
        this.fileSize = fileSize;
        this.data = data;
    }

    public Long getAvatarId() {
        return this.avatarId;
    }

    public Student getStudent() {
        return student;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMediaType() {
        return mediaType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public byte[] getData() {
        return data;
    }

    public void setAvatarId(Long avatarId) {
        this.avatarId = avatarId;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Avatar avatar = (Avatar) object;
        return fileSize == avatar.fileSize && Objects.equals(avatarId, avatar.avatarId) && Objects.equals(student, avatar.student) && Objects.equals(filePath, avatar.filePath) && Objects.equals(fileName, avatar.fileName) && Objects.equals(mediaType, avatar.mediaType) && Objects.deepEquals(data, avatar.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(avatarId, student, filePath, fileName, mediaType, fileSize, Arrays.hashCode(data));
    }

    @Override
    public String toString() {
        return "Avatar{" +
                "id=" + avatarId +
                ", student=" + student +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", mediaType='" + mediaType + '\'' +
                ", fileSize=" + fileSize +
                ", data=" + Arrays.toString(data) +
                '}';
    }
}