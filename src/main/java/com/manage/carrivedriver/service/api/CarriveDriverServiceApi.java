package com.manage.carrivedriver.service.api;

import com.manage.carrive.response.DriverResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface CarriveDriverServiceApi {

    ResponseEntity<DriverResponse> logout();
}
