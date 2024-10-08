package com.manage.carrivedriver.service.api;

import com.manage.carrive.response.DriverResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface CarriveDriverServiceApi {

    ResponseEntity<DriverResponse> logout();
    ResponseEntity<DriverResponse> sendDocumentPersonal(MultipartFile driverLicense, MultipartFile proofIdentity);
}
