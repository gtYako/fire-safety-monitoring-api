package com.firesafety.service;

import com.firesafety.dto.response.IncidentPhotoResponse;
import com.firesafety.entity.Incident;
import com.firesafety.entity.IncidentPhoto;
import com.firesafety.exception.FileStorageException;
import com.firesafety.exception.ResourceNotFoundException;
import com.firesafety.mapper.EntityMapper;
import com.firesafety.repository.IncidentPhotoRepository;
import com.firesafety.repository.IncidentRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    // Разрешаем загружать только изображения, которые можно прикрепить к инциденту.
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

    // Сервис хранит файл на диске, а метаданные фотографии сохраняет в базе.
    private final IncidentRepository incidentRepository;
    private final IncidentPhotoRepository photoRepository;
    private final EntityMapper mapper;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            // Каталог загрузок создаётся при старте приложения, если его ещё нет.
            Files.createDirectories(Paths.get(uploadDir));
            log.info("Upload directory initialized: {}", uploadDir);
        } catch (IOException e) {
            throw new FileStorageException("Could not create upload directory", e);
        }
    }

    @Transactional
    public IncidentPhotoResponse savePhoto(Long incidentId, MultipartFile file) {
        // Фотография всегда прикрепляется к существующему инциденту.
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", incidentId));

        validateFile(file);

        // Внутреннее имя делаем случайным, чтобы файлы с одинаковыми именами не перезаписывали друг друга.
        String originalName = file.getOriginalFilename();
        String extension = extractExtension(originalName);
        String storedName = UUID.randomUUID() + extension;
        Path targetPath = Paths.get(uploadDir).resolve(storedName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved: {} -> {}", originalName, storedName);
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file: " + originalName, e);
        }

        IncidentPhoto photo = IncidentPhoto.builder()
                .incident(incident)
                .originalFileName(originalName)
                .storedFileName(storedName)
                .filePath(targetPath.toString())
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();

        return mapper.toPhotoResponse(photoRepository.save(photo));
    }

    private void validateFile(MultipartFile file) {
        // Базовая защита от пустых, слишком больших и неподдерживаемых файлов.
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new FileStorageException("File size exceeds limit of 10MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new FileStorageException("Invalid file type: " + contentType + ". Allowed: JPEG, PNG, GIF, WebP");
        }
    }

    private String extractExtension(String filename) {
        // Расширение сохраняем только для удобства просмотра файла на диске.
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
