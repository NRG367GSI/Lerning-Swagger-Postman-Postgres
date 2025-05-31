package ru.hogwarts.school.model;

import jakarta.persistence.*;

import java.util.Arrays;
import java.util.Objects;

@Entity
public class Avatar {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

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

    public Avatar(Long id, Student student, String filePath, String mediaType, long fileSize, byte[] data) {
        this.id = id;
        this.student = student;
        this.filePath = filePath;
        this.fileName = fileName;
        this.mediaType = mediaType;
        this.fileSize = fileSize;
        this.data = data;
    }

    public Long getId() {
        return id;
    }

    public String getFilePath() {
        return filePath;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getMediaType() {
        return mediaType;
    }

    public byte[] getData() {
        return data;
    }

    public Student getStudent() {
        return student;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Avatar avatar = (Avatar) o;
        return fileSize == avatar.fileSize && Objects.equals(id, avatar.id)
                && Objects.equals(filePath, avatar.filePath)
                && Objects.equals(fileName, avatar.fileName)
                && Objects.equals(mediaType, avatar.mediaType)
                && Objects.deepEquals(data, avatar.data)
                && Objects.equals(student, avatar.student);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filePath, fileName, fileSize, mediaType, Arrays.hashCode(data), student);
    }

    @Override
    public String toString() {
        return "Avatar{" +
                "id=" + id +
                ", filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", mediaType='" + mediaType + '\'' +
                ", data=" + Arrays.toString(data) +
                ", student=" + student +
                '}';
    }
}