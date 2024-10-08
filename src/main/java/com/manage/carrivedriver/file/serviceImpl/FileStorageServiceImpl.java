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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageServiceImpl implements FileStorageServiceApi {

    private final Logger logger = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    private final Path personalFile;
    private final PersonalDocumentRepository personalDocumentRepository;

    // Injecter le chemin du fichier à partir des propriétés
    @Autowired
    public FileStorageServiceImpl(PersonalDocumentRepository personalDocumentRepository,
                                  @Value("${file.storage.location1}") String uploadDir) {
        this.personalDocumentRepository = personalDocumentRepository;
        this.personalFile = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // Créer le répertoire s'il n'existe pas
            Files.createDirectories(this.personalFile);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public FileStorageServiceImpl(Path personalFile, PersonalDocumentRepository personalDocumentRepository) {
        this.personalFile = personalFile;
        this.personalDocumentRepository = personalDocumentRepository;
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

            PersonalDocument personalDocument = new PersonalDocument();
            personalDocument.setDriver(driver);
//            PersonalDocument personalDocument = personalDocumentRepository.findByDriver(driver).orElse(null);
//            if (personalDocument == null) {
//                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
//                driverResponse.setData(null);
//                driverResponse.setMessage("personalDocument is null");
//                return new ResponseEntity<>(driverResponse, HttpStatus.BAD_REQUEST);
//            }

            String driverLicenseName = storePersonalFile(driverLicense, driver.getId());
            if (driverLicenseName != null) {
                String driverLicenseDownload = "/driver/files/download/" + driverLicenseName;
                personalDocument.setDriverLicenseName(driverLicenseName);
                personalDocument.setDriverLicenseDownload(driverLicenseDownload);
                personalDocument.setDriverLicenseValid(false);
                personalDocument = personalDocumentRepository.save(personalDocument);
            }

            String proofIdentityName = storePersonalFile(proofIdentity, driver.getId());
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

    public String storePersonalFile(MultipartFile file, String userId) {
        // Nom du fichier original
        String fileName = file.getOriginalFilename();

        try {
            // Vérifier que le nom de fichier n'est pas nul ou vide
            if (fileName != null && !fileName.isEmpty()) {
                // Créer un répertoire pour l'utilisateur (avec l'ID) s'il n'existe pas
                Path userDirectory = this.personalFile.resolve(userId).normalize();
                if (!Files.exists(userDirectory)) {
                    Files.createDirectories(userDirectory);  // Créer le dossier avec l'ID de l'utilisateur
                }

                // Chemin complet vers le fichier de l'utilisateur
                Path targetLocation = userDirectory.resolve(fileName);

                // Copier le fichier dans le dossier de l'utilisateur
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

                return fileName;
            } else {
                throw new RuntimeException("File name is empty");
            }

        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + fileName + ". Please try again!", ex);
        }
    }

    /**
     * Méthode pour télécharger un fichier pour un utilisateur
     */
    public Resource loadPersonalFileAsResource(String userId, String fileName) throws IOException {
        // Vérifier les valeurs de userId et fileName
        logger.info("Fetching file for userId: {} with fileName: {}", userId, fileName);

        // Chemin vers le répertoire de l'utilisateur
        Path userDirectory = this.personalFile.resolve(userId).normalize();
        Path filePath = userDirectory.resolve(fileName).normalize();

        logger.info("Looking for file at path: {}", filePath);  // Afficher le chemin du fichier

        // Vérifier si le fichier existe
        if (Files.exists(filePath)) {
            logger.info("File found at: {}", filePath);  // Afficher si le fichier est trouvé
            return new FileSystemResource(filePath.toFile());
        } else {
            logger.error("File not found at path: {}", filePath);  // Afficher un message d'erreur si le fichier n'existe pas
            throw new FileNotFoundException("File not found " + fileName);
        }
    }
}
