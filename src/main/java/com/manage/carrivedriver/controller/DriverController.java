package com.manage.carrivedriver.controller;

import com.manage.carrive.dto.ItineraryDto;
import com.manage.carrive.dto.MessageDto;
import com.manage.carrive.response.DriverResponse;
import com.manage.carrive.response.MessageResponse;
import com.manage.carrivedriver.security.JwtRequestFilter;
import com.manage.carrivedriver.service.impl.CarriveDriverServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("driver")
public class DriverController {

    public static String token = JwtRequestFilter.jwtToken;

    @Autowired
    private CarriveDriverServiceImpl service;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostMapping("logout")
    private ResponseEntity<DriverResponse> logout() {
        return service.logout();
    }

    @PreAuthorize("customAuthorizationService.hasPermission('INVESTOR')")
    @PostMapping("create-itinerary")
    private ResponseEntity<DriverResponse> createItinerary(@RequestBody ItineraryDto itinerary) {
        return service.createItinerary(itinerary);
    }

    // Méthode pour créer les en-têtes avec le token
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        return headers;
    }

    // Méthode pour envoyer la requête et formater la réponse
    private ResponseEntity<MessageResponse> sendRequest(String url, HttpEntity<?> entity) {
        try {
            ResponseEntity<Object> response = restTemplate.exchange(url, HttpMethod.POST, entity, Object.class);
            return ResponseEntity.ok(new MessageResponse("success", response.getStatusCodeValue(), response.getBody()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponse("Erreur lors de la requête", HttpStatus.INTERNAL_SERVER_ERROR.value(), null));
        }
    }

    @PostMapping("init-conversation")
    private ResponseEntity<MessageResponse> initConversation(@RequestParam("id_receiver") String idReceiver) {
        String url = "http://localhost:8086/message/init-conversation?id_receiver=" + idReceiver;
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        return sendRequest(url, entity);
    }

    @PostMapping("send-message")
    private ResponseEntity<MessageResponse> sendMessage(
            @RequestParam("idConversation") String idConversation,
            @RequestBody MessageDto message) {

        String url = "http://localhost:8086/message/send-message?idConversation=" + idConversation;
        HttpEntity<MessageDto> entity = new HttpEntity<>(message, createHeaders());
        return sendRequest(url, entity);
    }

    @PostMapping("list-conversation")
    private ResponseEntity<MessageResponse> listConversation() {
        String url = "http://localhost:8086/message/list-conversations";
        HttpEntity<String> entity = new HttpEntity<>(createHeaders());
        return sendRequest(url, entity);
    }
}
