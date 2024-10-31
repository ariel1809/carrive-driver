package com.manage.carrivedriver.service.impl;

import com.manage.carrive.dto.ItineraryDto;
import com.manage.carrive.entity.Driver;
import com.manage.carrive.entity.Itinerary;
import com.manage.carrive.entity.Passenger;
import com.manage.carrive.enumeration.CodeResponseEnum;
import com.manage.carrive.enumeration.StatusItineraryEnum;
import com.manage.carrive.response.DriverResponse;
import com.manage.carrivedriver.security.JwtRequestFilter;
import com.manage.carrivedriver.service.api.CarriveDriverServiceApi;
import com.manage.carriveutility.repository.DriverRepository;
import com.manage.carriveutility.repository.ItineraryRepository;
import com.manage.carriveutility.repository.PackageRepository;
import com.manage.carriveutility.repository.PassengerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarriveDriverServiceImpl implements CarriveDriverServiceApi {

    private final Logger logger = LoggerFactory.getLogger(CarriveDriverServiceImpl.class);
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private ItineraryRepository itineraryRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private PassengerRepository passengerRepository;

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

    @Override
    public ResponseEntity<DriverResponse> createItinerary(ItineraryDto itinerary) {
        DriverResponse driverResponse = new DriverResponse();
        try {

            Driver driver = JwtRequestFilter.driver;
            Itinerary itinerarySaved;
            if (driver != null) {
                if(itinerary == null){
                    driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    driverResponse.setMessage("itinerary is null");
                    driverResponse.setData(null);
                    return new ResponseEntity<>(driverResponse, HttpStatus.OK);
                }
                if (itinerary.getStartCity() == null){
                    driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    driverResponse.setMessage("startCity is null");
                    driverResponse.setData(null);
                    return new ResponseEntity<>(driverResponse, HttpStatus.OK);
                }
                if (itinerary.getDestinationCity() == null){
                    driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    driverResponse.setMessage("destinationCity is null");
                    driverResponse.setData(null);
                    return new ResponseEntity<>(driverResponse, HttpStatus.OK);
                }
                if (itinerary.getCapacity() == null){
                    driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    driverResponse.setMessage("capacity is null");
                    driverResponse.setData(null);
                    return new ResponseEntity<>(driverResponse, HttpStatus.OK);
                }
                if (itinerary.getAcceptedPackage() == null){
                    driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    driverResponse.setMessage("acceptedPackage is null");
                    driverResponse.setData(null);
                    return new ResponseEntity<>(driverResponse, HttpStatus.OK);
                }

                itinerarySaved = new Itinerary();
                itinerarySaved.setCreatedBy(driver);
                itinerarySaved.setCreatedAt(LocalDateTime.now(zoneId));
                itinerarySaved.setStartDate(itinerary.getStartDate());
                itinerarySaved.setStartTime(itinerary.getStartTime());
                itinerarySaved.setCapacity(itinerary.getCapacity());
                itinerarySaved.setDistance(itinerary.getDistance());
                itinerarySaved.setAcceptedPackage(itinerary.getAcceptedPackage());
                itinerarySaved.setStartCity(itinerary.getStartCity());
                itinerarySaved.setDestinationCity(itinerary.getDestinationCity());
                itinerarySaved.setStartDate(itinerary.getStartDate());
                itinerarySaved.setTariff(itinerary.getTariff());
                itinerarySaved.setIsPublished(itinerary.getIsPublished());
                itinerarySaved.setStatus(StatusItineraryEnum.CREATED);
                itinerarySaved = itineraryRepository.save(itinerarySaved);
            }else {
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setMessage("itinerary is null");
                driverResponse.setData(null);
                return new ResponseEntity<>(driverResponse, HttpStatus.OK);
            }

            driverResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
            driverResponse.setMessage("success");
            driverResponse.setData(itinerarySaved);
            return new ResponseEntity<>(driverResponse, HttpStatus.OK);

        }catch (Exception e){
            logger.error(e.getMessage());
            driverResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            driverResponse.setMessage(e.getMessage());
            driverResponse.setData(null);
            return new ResponseEntity<>(driverResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<DriverResponse> listAllUsers() {
        DriverResponse driverResponse = new DriverResponse();
        try {

            Driver driver = JwtRequestFilter.driver;
            if (driver != null) {
                List<Object> users = new ArrayList<>(); // Initialisation de la liste
                List<Driver> drivers = driverRepository.findAll();
                List<Passenger> passengers = passengerRepository.findAll();

                users.addAll(drivers);
                users.addAll(passengers);
                users.remove(driver);
                if (users.isEmpty()){
                    driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    driverResponse.setMessage("list users is empty");
                    driverResponse.setData(null);
                }else {
                    driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    driverResponse.setMessage("list users");
                    driverResponse.setData(users);
                }
            }else {
                driverResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                driverResponse.setMessage("driver not found");
                driverResponse.setData(null);
            }
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
