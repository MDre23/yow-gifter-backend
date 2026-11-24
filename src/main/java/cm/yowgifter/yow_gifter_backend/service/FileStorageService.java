package cm.yowgifter.yow_gifter_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path storageDirectory;

    public FileStorageService(@Value("${app.upload.dir:./uploads/donations}") String uploadDir) {
        this.storageDirectory = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'initialiser le répertoire d'upload : " + uploadDir, e);
        }
    }

    public String storeFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le fichier téléversé est vide.");
        }

        // 🛠️ VÉRIFICATION DE LA TAILLE (5 Mo max) - Ajouté ici
        long maxSize = 5 * 1024 * 1024; // 5 Mo en octets
        if (file.getSize() > maxSize) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "L'image est trop lourde. La taille maximale autorisée est de 5 Mo.");
        }

        // Vérification du format
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.startsWith("image/jpeg") && !contentType.startsWith("image/png") && !contentType.startsWith("image/webp"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format d'image non supporté. Formats acceptés : JPG, PNG ou WEBP.");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            extension = ".jpg";
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        Path targetLocation = this.storageDirectory.resolve(uniqueFileName);

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/donations/" + uniqueFileName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de l'enregistrement de l'image.", e);
        }
    }
}