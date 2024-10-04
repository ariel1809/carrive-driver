package com.manage.carrivedriver.service.impl;

import com.manage.carrive.entity.Driver;
import com.manage.carrive.enumeration.CodeResponseEnum;
import com.manage.carrive.response.DriverResponse;
import com.manage.carrivedriver.security.JwtRequestFilter;
import com.manage.carrivedriver.service.api.CarriveDriverServiceApi;
import com.manage.carriveutility.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CarriveDriverServiceImpl implements CarriveDriverServiceApi {

    private final Logger logger = LoggerFactory.getLogger(CarriveDriverServiceImpl.class);

    @Autowired
    private DriverRepository driverRepository;

    @Override
    public ResponseEntity<DriverResponse> logout() {
        DriverResponse driverResponse = new DriverResponse();

        try {

            Driver driver = JwtRequestFilter.driver;
            if (driver != null) {
                if (!driver.getIsConnected()){
                    driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    driverResponse.setData(null);
                    driverResponse.setMessage("driver is already closed");
                    return new ResponseEntity<>(driverResponse, HttpStatus.OK);
                }else {
                    driver.setIsConnected(false);
                    driver.setToken(null);
                    driver = driverRepository.save(driver);
                }
            }else {
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setMessage("driver is null");
                driverResponse.setData(null);
                return new ResponseEntity<>(driverResponse, HttpStatus.OK);
            }
            driverResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
            driverResponse.setMessage("success");
            driverResponse.setData(driver);
            return new ResponseEntity<>(driverResponse, HttpStatus.OK);

        }catch (Exception e){
            logger.error(e.getMessage());
            driverResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            driverResponse.setMessage(e.getMessage());
            driverResponse.setData(null);
            return new ResponseEntity<>(driverResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
