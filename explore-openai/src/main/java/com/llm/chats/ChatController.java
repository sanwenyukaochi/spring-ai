package com.llm.chats;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController { 
//    private final ChatClient chatClient;
//
//    private static final Logger log = LoggerFactory.getLogger(ChatController.class);
//
//    public ChatController(ChatClient.Builder chatClientBuilder) {
//        this.chatClient = chatClientBuilder.build();
//    }
//
//    @PostMapping("/v1/chats")
//    public Object chat(@RequestBody UserInput userInput) {
//        log.info("userInput message : {}", userInput);
//        var requestSpec = chatClient
//                .prompt()
//                .user(userInput.prompt());
//        log.info("requestSpec : {}", requestSpec);
//        var responseSpec = requestSpec.call();
//        log.info("responseSpec : {}", responseSpec);
//        log.info("content : {}", responseSpec.content());
//        return responseSpec.content();
//    }
    private final OpenAiChatModel chatClient;

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    public ChatController(OpenAiChatModel chatClient) {
        this.chatClient = chatClient;
    }

    @PostMapping("/v1/chats")
    public Object chat(@RequestBody UserInput userInput) {
        log.info("userInput message : {}", userInput);
        Prompt prompt = new Prompt(new UserMessage(userInput.prompt()));
        log.info("requestSpec : {}", prompt);
        ChatResponse response = chatClient.call(prompt);
        log.info("responseSpec : {}", response);
        log.info("content : {}", response.getResult().getOutput());
        return response;
    }
}
