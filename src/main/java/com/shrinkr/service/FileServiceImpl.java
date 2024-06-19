package com.shrinkr.service;

import com.shrinkr.dto.FileDto;
import com.shrinkr.utils.DataBucketUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;

@Service
public class FileServiceImpl implements FileService {

    private final DataBucketUtil dataBucketUtil;

    @Autowired
    public FileServiceImpl(DataBucketUtil dataBucketUtil) {
        this.dataBucketUtil = dataBucketUtil;
    }

    public String uploadFile(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        if(originalFileName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Original file name is null.");
        }

        Path path = new File(originalFileName).toPath();

        try {
            String contentType = Files.probeContentType(path);
            FileDto fileDto = dataBucketUtil.uploadFile(file, originalFileName, contentType);

            return fileDto.getFileUrl();
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while uploading a file.");
        }
    }

    public void deleteFile(String imageUrl) {
        dataBucketUtil.deleteImage(imageUrl);
    }
}
