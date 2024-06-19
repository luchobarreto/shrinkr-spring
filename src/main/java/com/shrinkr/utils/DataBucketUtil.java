package com.shrinkr.utils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import com.shrinkr.dto.FileDto;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class DataBucketUtil {

    @Value("${shrinkr.app.gcpConfigFile}")
    private String gcpConfigFile;

    @Value("${shrinkr.app.gcpProjectId}")
    private String gcpProjectId;

    @Value("${shrinkr.app.gcpBucketId}")
    private String gcpBucketId;

    @Value("${shrinkr.app.gcpDirectoryName}")
    private String gcpDirectoryName;

    public FileDto uploadFile(MultipartFile multipartFile, String fileName, String contentType) {
        try {
            byte[] fileData = FileUtils.readFileToByteArray(convertFile(multipartFile));

            InputStream inputStream = new ClassPathResource(gcpConfigFile).getInputStream();

            StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(GoogleCredentials.fromStream(inputStream)).build();

            Storage storage = options.getService();
            Bucket bucket = storage.get(gcpBucketId, Storage.BucketGetOption.fields());

            String id = UUID.randomUUID().toString();
            Blob blob = bucket.create(gcpDirectoryName + "/" + fileName + "-" + id + checkFileExtension(fileName), fileData, contentType);
            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

            if(blob != null) {
                return new FileDto(blob.getName(), getPublicUrl(blob));
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while storing data to GCS.");
        }

        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while storing data to GCS.");
    }

    public void deleteImage(String imageUrl) {
        String imageName = getImageName(imageUrl);
        System.out.println(imageName);
        try {
            if(imageName != null) {
                InputStream inputStream = new ClassPathResource(gcpConfigFile).getInputStream();

                StorageOptions options = StorageOptions.newBuilder().setProjectId(gcpProjectId).setCredentials(GoogleCredentials.fromStream(inputStream)).build();
                Storage storage = options.getService();
                storage.delete(BlobId.of(gcpBucketId, "images/" + imageName));
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image url");
            }
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error has occurred while deleting the file.");
        }
    }

    private File convertFile(MultipartFile file) {
        try {
            if(file.getOriginalFilename() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Original file name is null");
            }
            File convertedFile = new File(file.getOriginalFilename());
            FileOutputStream outputStream = new FileOutputStream(convertedFile);
            outputStream.write(file.getBytes());
            outputStream.close();
            return convertedFile;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error has occurred while converting the file.");
        }
    }

    private String checkFileExtension(String fileName) {
        if(fileName != null && fileName.contains(".")) {
            String[] extensionList = {".png", ".jpg", ".jpeg", ".webp"};

            for(String extension: extensionList) {
                if(fileName.endsWith(extension)) {
                    return extension;
                }
            }
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type.");
    }

    private String getPublicUrl(Blob blob) {
        return String.format("https://storage.googleapis.com/%s/%s", blob.getBucket(), blob.getName());
    }

    private String getImageName(String imageUrl) {
        try {
            String urlWithoutProtocol = imageUrl.replace("https?://storage\\\\.googleapis\\\\.com/", "");
            String[] parts = urlWithoutProtocol.split("/");

            String imageName = parts[parts.length - 1];
            imageName = URLDecoder.decode(imageName, StandardCharsets.UTF_8);

            return imageName;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to extract image name from public URL.");
        }
    }
}
