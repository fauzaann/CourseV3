package com.belajar.course.config.storage;

import com.belajar.course.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final Set<String> ALLOWED_FILE_TYPES = Set.of("application/pdf", "video/mp4");

    private final Path uploadBasePath;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadBasePath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadBasePath);
        } catch (IOException e) {
            throw new RuntimeException("Gagal membuat direktori upload", e);
        }
    }

    public String storeImage(MultipartFile file, String subDir) throws IOException {
        validateFileType(file, ALLOWED_IMAGE_TYPES);
        return store(file, subDir);
    }

    public String storeFile(MultipartFile file, String subDir) throws IOException {
        validateFileType(file, ALLOWED_FILE_TYPES);
        return store(file, subDir);
    }

    private String store(MultipartFile file, String subDir) throws IOException {
        Path dirPath = uploadBasePath.resolve(subDir);
        Files.createDirectories(dirPath);

        String ext = getExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID() + ext;
        Path targetPath = dirPath.resolve(fileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File tersimpan: {}", targetPath);

        return "/uploads/" + subDir + "/" + fileName;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.startsWith("/uploads/")) {
            try {
                Path filePath = uploadBasePath.resolve(fileUrl.substring("/uploads/".length()));
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                log.warn("Gagal menghapus file: {}", fileUrl);
            }
        }
    }

    private void validateFileType(MultipartFile file, Set<String> allowedTypes) {
        if (!allowedTypes.contains(file.getContentType())) {
            throw new BadRequestException("Tipe file tidak diizinkan: " + file.getContentType());
        }
    }

    private String getExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }
}