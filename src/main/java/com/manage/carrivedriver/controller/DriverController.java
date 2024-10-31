package com.manage.carrivedriver.controller;

import com.manage.carrive.dto.ItineraryDto;
import com.manage.carrive.dto.MessageDto;
import com.manage.carrive.response.DriverResponse;
import com.manage.carrive.response.MessageResponse;
import com.manage.carrivedriver.feignclient.MessageFeignClient;
import com.manage.carrivedriver.security.JwtRequestFilter;
import com.manage.carrivedriver.service.impl.CarriveDriverServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("driver")
public class DriverController {

    @Autowired
    private CarriveDriverServiceImpl service;

    @Autowired
    private MessageFeignClient messageFeignClient;

    @PostMapping("init-conversation")
    public ResponseEntity<MessageResponse> initConversation(@RequestParam("id_receiver") String idReceiver) {
        return messageFeignClient.initConversation(idReceiver);
    }

    @PostMapping("/list-conversations")
    public ResponseEntity<MessageResponse> listConversations() {
        return messageFeignClient.listConversationsByUser();
    }

    @PostMapping("/send-message")
    ResponseEntity<MessageResponse> sendMessage(@RequestParam("id_conversation") String idConversation, @RequestBody MessageDto message){
        return messageFeignClient.sendMessage(idConversation, message);
    }

    @PostMapping("logout")
    private ResponseEntity<DriverResponse> logout() {
        return service.logout();
    }

    @PreAuthorize("customAuthorizationService.hasPermission('INVESTOR')")
    @PostMapping("create-itinerary")
    private ResponseEntity<DriverResponse> createItinerary(@RequestBody ItineraryDto itinerary) {
        return service.createItinerary(itinerary);
    }

    @PostMapping("list-users")
    private ResponseEntity<DriverResponse> listUsers() {
        return service.listAllUsers();
    }
}
