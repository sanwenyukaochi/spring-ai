package com.llm.chats;

import com.llm.dto.UserInput;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
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
    @Value("classpath:/prompt-templates/java-coding-assistant.st")
    private Resource systemTemplateMessage;

    @Value("classpath:/prompt-templates/coding-assistant.st")
    private Resource systemText;


    public PromptController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    @PostMapping("/v1/prompts")
    public Object prompt(@RequestBody @Valid UserInput userInput) {
        log.info("userInput message : {}", userInput);
        String systemMessage = """
                    你真是个好帮手，能解答 Java 相关的问题。
                    如有其他问题，请用幽默的方式回答“我不知道”！
                """;
        SystemMessage sysMessage = new SystemMessage(systemTemplateMessage);
        UserMessage userMessage = new UserMessage(userInput.prompt());
        Prompt promptMessage = new Prompt(List.of(sysMessage, 
               // new UserMessage("我的名字是?"),
               // new AssistantMessage("我不知道"),
               // new AssistantMessage("我的名字是小宋"),
                userMessage));
        ChatClient.CallResponseSpec call = chatClient.prompt(promptMessage).call();
        return call.content();
    }

    @PostMapping("/v1/prompts/{language}")
    public Object promptsByLanguage(@PathVariable String language, @RequestBody @Valid UserInput userInput) {
        log.info("userInput : {}, language : {}", userInput, language);
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message sysMessage = systemPromptTemplate.createMessage(Map.of("language", language));
        log.info("sysMessage : {}", sysMessage);
        UserMessage userMessage = new UserMessage(userInput.prompt());
        Prompt promptMessage = new Prompt(List.of(sysMessage, userMessage));
        ChatClient.CallResponseSpec call = chatClient.prompt(promptMessage).call();
        return call.content();
    }
}
