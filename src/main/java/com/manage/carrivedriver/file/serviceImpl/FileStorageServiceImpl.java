package com.manage.carrivedriver.file.serviceImpl;

import com.manage.carrive.entity.Driver;
import com.manage.carrive.entity.PersonalDocument;
import com.manage.carrive.enumeration.CodeResponseEnum;
import com.manage.carrive.response.DriverResponse;
import com.manage.carrivedriver.file.serviceApi.FileStorageServiceApi;
import com.manage.carrivedriver.security.JwtRequestFilter;
import com.manage.carriveutility.repository.PersonalDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageServiceImpl implements FileStorageServiceApi {

    private final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    @Autowired
    private PersonalDocumentRepository personalDocumentRepository;

    private Path personalFile;

    public void FileStorageService(@Value("${file.storage.location1}") String uploadDir) {
        this.personalFile = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.personalFile);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public FileStorageServiceImpl(Path personalFile) {
        this.personalFile = personalFile;
    }

    @Override
    public ResponseEntity<DriverResponse> uploadFile(MultipartFile driverLicense, MultipartFile proofIdentity) {
        DriverResponse driverResponse = new DriverResponse();
        try {

            Driver driver = JwtRequestFilter.driver;
            if (driver == null) {
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setData(null);
                driverResponse.setMessage("driver is null");
                return new ResponseEntity<>(driverResponse, HttpStatus.BAD_REQUEST);
            }
            if (proofIdentity == null) {
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setData(null);
                driverResponse.setMessage("proofIdentity is null");
                return new ResponseEntity<>(driverResponse, HttpStatus.BAD_REQUEST);
            }
            if (driverLicense == null) {
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setData(null);
                driverResponse.setMessage("driverLicense is null");
                return new ResponseEntity<>(driverResponse, HttpStatus.BAD_REQUEST);
            }
            if (!Objects.equals(driverLicense.getContentType(), "application/pdf") && !Objects.requireNonNull(driverLicense.getContentType()).startsWith("image/")){
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setData(null);
                driverResponse.setMessage("driverLicense is not image or pdf");
                return new ResponseEntity<>(driverResponse, HttpStatus.BAD_REQUEST);
            }
            if (!Objects.equals(proofIdentity.getContentType(), "application/pdf") && !Objects.requireNonNull(proofIdentity.getContentType()).startsWith("image/")){
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setData(null);
                driverResponse.setMessage("proofIdentity is not image or pdf");
                return new ResponseEntity<>(driverResponse, HttpStatus.BAD_REQUEST);
            }

            PersonalDocument personalDocument = personalDocumentRepository.findByDriver(driver).orElse(null);
            if (personalDocument == null) {
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setData(null);
                driverResponse.setMessage("personalDocument is null");
                return new ResponseEntity<>(driverResponse, HttpStatus.BAD_REQUEST);
            }

            String driverLicenseName = storePersonalFile(driverLicense);
            if (driverLicenseName != null) {
                String driverLicenseDownload = "/driver/files/download/" + driverLicenseName;
                personalDocument.setDriverLicenseName(driverLicenseName);
                personalDocument.setDriverLicenseDownload(driverLicenseDownload);
                personalDocument.setDriverLicenseValid(false);
                personalDocument = personalDocumentRepository.save(personalDocument);
            }

            String proofIdentityName = storePersonalFile(proofIdentity);
            if (proofIdentityName != null) {
                String driverLicenseDownload = "/driver/files/download/" + proofIdentityName;
                personalDocument.setProofIdentityName(proofIdentityName);
                personalDocument.setProofIdentityDownload(driverLicenseDownload);
                personalDocument.setProofIdentityIsValid(false);
                personalDocument = personalDocumentRepository.save(personalDocument);
            }

            driverResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
            driverResponse.setData(personalDocument);
            driverResponse.setMessage("success");
            return new ResponseEntity<>(driverResponse, HttpStatus.OK);

        }catch (Exception e){
            logger.error(e.getMessage());
            driverResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            driverResponse.setMessage(e.getMessage());
            driverResponse.setData(null);
            return new ResponseEntity<>(driverResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public String storePersonalFile(MultipartFile file) {
        // Nom du fichier original
        String fileName = file.getOriginalFilename();

        try {
            // Chemin cible pour stocker le fichier
            if (fileName != null && !fileName.isEmpty()) {
                Path targetLocation = this.personalFile.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }
            return fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    public Path loadPersonalFileAsResource(String fileName) {
        return this.personalFile.resolve(fileName).normalize();
    }
}
