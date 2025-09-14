package com.llm.chats;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class ChatController {
    private final ChatClient chatClient;

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/v1/chats")
    public Object chat(@RequestBody UserInput userInput) {
        log.info("userInput message : {}", userInput);
        var requestSpec = chatClient
                .prompt()
                .user(userInput.prompt());
        log.info("requestSpec : {}", requestSpec);
        var responseSpec = requestSpec.call();
        log.info("responseSpec : {}", responseSpec);
        String content = responseSpec.content();
        log.info("content : {}", content);
        return content;
    }
}
