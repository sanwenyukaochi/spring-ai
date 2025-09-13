package com.llm.prompt_engineering;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class PromptTypesController {

    private static final Logger log = LoggerFactory.getLogger(TravelAssistantController.class);
    private final ChatClient chatClient;

    public PromptTypesController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Value("classpath:/prompt-templates/prompt_types/few_shot.st")
    private Resource fewShotPrompt;

    @Value("classpath:/prompt-templates/prompt_types/multi_step_prompt_1.st")
    private Resource multiStep1;

    @Value("classpath:/prompt-templates/prompt_types/multi_step_prompt_2.st")
    private Resource multiStep2;


    @PostMapping("/v1/prompt_types/zero_shot")
    public String zeroShot(@RequestBody UserInput userInput) {
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

    //   happy
    // unhappy

    @PostMapping("/v1/prompt_types/few_shot")
    public String fewShot(@RequestBody UserInput userInput) {
        log.info("userInput : {} ", userInput);

        String fewShotExamples = """
                提示：“产品到货很快，运行完美，超出我的预期！”
                答案：满意
                
                提示：“质量好，发货快，与描述完全一致——强烈推荐！”
                答案：满意
                
                提示：“商品到货时已损坏，完全无法使用——非常令人失望！”
                答案：不满意
                
                提示：“包装不当导致产品损坏，完全无法使用。”
                答案：不满意
                
                """;

        SystemPromptTemplate  systemPromptTemplate = new SystemPromptTemplate(fewShotPrompt);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("few_shot_prompts", fewShotExamples));
        Prompt promptMessage = new Prompt(
                List.of(
                        systemMessage,
                        new UserMessage(userInput.prompt())
                )
        );

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt(promptMessage);

        ChatClient.CallResponseSpec responseSpec = requestSpec.call();
        return responseSpec.content();
    }

    @PostMapping("/v1/prompt_types/cot")
    public String cot(@RequestBody UserInput userInput) {
        log.info("userInput : {} ", userInput);

        var promptMessage = new Prompt(
                List.of(
                        new UserMessage(userInput.prompt())
                )
        );
        var requestSpec = chatClient.prompt(promptMessage);

        var responseSpec = requestSpec.call();
        return responseSpec.content();
    }

    @PostMapping("/v1/prompt_types/multi_step")
    public String multistep_1(@RequestBody UserInput userInput) {
        log.info("userInput : {} ", userInput);
        PromptTemplate promptTemplate = new PromptTemplate(multiStep1);
//        PromptTemplate promptTemplate = new PromptTemplate(multiStep2);
        var message = promptTemplate.createMessage(Map.of("input", userInput.prompt()));
        log.info("prompt : {} ",message.getText());
        var promptMessage = new Prompt(
                List.of(message)
        );
        var requestSpec = chatClient.prompt(promptMessage);

        var responseSpec = requestSpec.call();
        return responseSpec.content();
    }
}
