package com.manage.carrivedriver.file.serviceApi;

import com.manage.carrive.response.DriverResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface FileStorageServiceApi {
    ResponseEntity<DriverResponse> uploadFile(MultipartFile driverLicense, MultipartFile proofIdentity);
}
