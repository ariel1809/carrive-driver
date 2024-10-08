package com.manage.carrivedriver.controller;

import com.manage.carrive.entity.Driver;
import com.manage.carrive.response.DriverResponse;
import com.manage.carrivedriver.file.serviceImpl.FileStorageServiceImpl;
import com.manage.carrivedriver.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

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
        Driver driver = JwtRequestFilter.driver;
        try {
            // Charger le fichier pour l'utilisateur spécifique
            Resource resource = service.loadPersonalFileAsResource(driver.getId(), fileName);

            // Déterminer le type de contenu (mimetype)
            String contentType = Files.probeContentType(resource.getFile().toPath());

            // Si le type de contenu est inconnu, le définir comme binaire
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        }
    }
}
