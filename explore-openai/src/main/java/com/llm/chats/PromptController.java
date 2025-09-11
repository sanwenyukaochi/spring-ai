package com.llm.chats;

import com.llm.dto.UserInput;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PromptController {


    private static final Logger log = LoggerFactory.getLogger(PromptController.class);
    private final ChatClient chatClient;


    @Value("classpath:/prompt-templates/coding-assistant.st")
    private Resource systemText;


    public PromptController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    @PostMapping("/v1/prompts")
    public Object prompt(@RequestBody @Valid UserInput userInput) {
        log.info("userInput message : {}", userInput);
        String systemMessage = """
                    你好
                """;
        SystemMessage sysMessage = new SystemMessage(systemMessage);
        UserMessage userMessage = new UserMessage(userInput.prompt());
        Prompt promptMessage = new Prompt(List.of(sysMessage, userMessage));
        ChatClient.CallResponseSpec call = chatClient.prompt(promptMessage).call();
        return call.content();
    }
}
