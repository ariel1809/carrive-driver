package com.manage.carrivedriver.feignclient;

import com.manage.carrive.dto.MessageDto;
import com.manage.carrive.response.MessageResponse;
import com.manage.carrivedriver.configuration.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "carrive-message", url = "http://localhost:8086", configuration = FeignConfig.class)
public interface MessageFeignClient {

    @PostMapping("/message/init-conversation")
    ResponseEntity<MessageResponse> initConversation(@RequestParam("id_receiver") String idReceiver);

    @PostMapping("/message/list-conversations")
    ResponseEntity<MessageResponse> listConversationsByUser();

    @PostMapping("/message/send-message")
    ResponseEntity<MessageResponse> sendMessage(@RequestParam("id_conversation") String idConversation, @RequestBody MessageDto message);

}
