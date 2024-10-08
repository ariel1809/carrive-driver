package com.manage.carrivedriver.controller;

import com.manage.carrive.response.DriverResponse;
import com.manage.carrivedriver.file.serviceImpl.FileStorageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("driver/files")
public class DriverFileController {

    @Autowired
    private FileStorageServiceImpl service;

    // Endpoint pour uploader un les documents personnels d'un conducteur
    @PostMapping("upload-personal")
    public ResponseEntity<DriverResponse> uploadPersonalFiles(@RequestParam("driver_license") MultipartFile driverLicense, @RequestParam("proof_identity") MultipartFile proofIdentity) {
        return service.uploadFile(driverLicense, proofIdentity);
    }

    // Endpoint pour télécharger un document perso d'un conducteur
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        try {
            Path filePath = service.loadPersonalFileAsResource(fileName);
            Resource resource = new UrlResource(filePath.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (Exception ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
