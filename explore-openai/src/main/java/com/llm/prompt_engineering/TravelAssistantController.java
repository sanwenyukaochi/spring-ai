package com.llm.prompt_engineering;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class TravelAssistantController {

    private static final Logger log = LoggerFactory.getLogger(TravelAssistantController.class);
    private final ChatClient chatClient;

    @Value("classpath:/prompt-templates/travel_prompt.st")
    private Resource travelPromptMessage;

    public TravelAssistantController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }


    @PostMapping("/v1/travel_assistant")
    public String prompts(@RequestBody UserInput userInput) {
        log.info("userInput : {} ", userInput);

        Prompt promptMessage = new Prompt(
                List.of(
                        new UserMessage(userInput.prompt())
                )
        );

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(promptMessage);
        
        ChatClient.CallResponseSpec responseSpec = requestSpec.call();
        return responseSpec.content();
    }

    @PostMapping("/v2/travel_assistant")
    public String promptsv2(@RequestBody UserInput userInput) {
        log.info("userInput : {} ", userInput);

        String systemMessage = """
                您是一位专业的旅行策划师，对全球目的地有着丰富的了解，
                包括文化景点、住宿和旅行物流。
                同时，您还能为家庭提供更优质的住宿选择。
                """;

        PromptTemplate promptTemplate = new PromptTemplate(travelPromptMessage);
        Message message = promptTemplate.createMessage(Map.of("context", userInput.context(), "input", userInput.prompt()));

        Prompt promptMessage = new Prompt(
                new SystemMessage(systemMessage), // Sets the role
                message
                );

        log.info("promptMessage : {} ", promptMessage);
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(promptMessage);

        ChatClient.CallResponseSpec responseSpec = requestSpec.call();
        return responseSpec.content();
    }

}
