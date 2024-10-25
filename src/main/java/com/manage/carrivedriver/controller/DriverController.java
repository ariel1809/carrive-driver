package com.manage.carrivedriver.controller;

import com.manage.carrive.dto.ItineraryDto;
import com.manage.carrive.response.DriverResponse;
import com.manage.carrivedriver.service.impl.CarriveDriverServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("driver")
public class DriverController {

    @Autowired
    private CarriveDriverServiceImpl service;

    @PostMapping("logout")
    private ResponseEntity<DriverResponse> logout() {
        return service.logout();
    }

    @PreAuthorize("customAuthorizationService.hasPermission('INVESTOR')")
    @PostMapping("create-itinerary")
    private ResponseEntity<DriverResponse> createItinerary(@RequestBody ItineraryDto itinerary) {
        return service.createItinerary(itinerary);
    }
}
