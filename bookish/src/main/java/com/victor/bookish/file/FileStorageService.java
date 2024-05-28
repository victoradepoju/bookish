package com.victor.bookish.file;

import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j // logger using Lombok
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${application.file.upload.photos-output-path}")
    private String fileUploadPath;

    public String savefile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull Integer userId
    ) {

        final String fileUploadSubPath = "user" + File.separator + userId;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    private String uploadFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String fileUploadSubPath
    ) {

        final String finalUploadPath =
                fileUploadPath +
                        File.separator +
                            fileUploadSubPath;

        File targetFolder = new File(finalUploadPath);

        if(!targetFolder.exists()) {
            boolean folderCreated = targetFolder.mkdirs();
            if(!folderCreated) {
                log.warn("Failed to create the target folder");
                return null;
            }
        }

        final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());

        // looks like => .upload/users/1/12343834.jpg
        String targetFilePath = finalUploadPath +
                File.separator +
                System.currentTimeMillis() +
                "." +
                fileExtension;

        Path targetPath = Paths.get(targetFilePath);

        try {
            Files.write(targetPath, sourceFile.getBytes());
            log.info("File saved to {}", targetFilePath);
            return targetFilePath;
        } catch (IOException e) {
            log.error("File was not saved", e);
        }

        return null;
    }

    private String getFileExtension(String originalFilename) {
        if(originalFilename == null || originalFilename.isEmpty()) {
            return "";
        }
        // something.jpg
        int lastDotIndex = originalFilename.lastIndexOf(".");

        if(lastDotIndex == -1) {
            // meaning the file does not have an extension
            return "";
        }

        return originalFilename.substring(lastDotIndex + 1).toLowerCase();
    }
}
