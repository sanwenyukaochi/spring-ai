package com.llm.tool_calling;

import com.llm.dto.UserInput;
import com.llm.tool_calling.currency.CurrencyTools;
import com.llm.tool_calling.currenttime.DateTimeTools;
import com.llm.tool_calling.weather.WeatherConfigProperties;
import com.llm.tool_calling.weather.WeatherToolsFunction;
import com.llm.tool_calling.weather.dtos.WeatherRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.web.bind.annotation.*;

@RestController
public class ToolCallingController {
    private static final Logger log = LoggerFactory.getLogger(ToolCallingController.class);

    private final ChatClient chatClient;

    private final CurrencyTools currencyTools;
    
    private final OpenAiChatModel openAiChatModel;

    public ToolCallingController(ChatClient.Builder builder,
                                 WeatherConfigProperties weatherConfigProperties,
                                 OpenAiChatModel openAiChatModel,
                                 CurrencyTools currencyTools) {

        ToolCallback toolCallback = FunctionToolCallback
                .builder("currentWeather", new WeatherToolsFunction(weatherConfigProperties))
                .description("获取当地天气")
                .inputType(WeatherRequest.class)
                .build();
        
        this.chatClient = builder
                .defaultSystem("您是一位乐于助人的人工智能助手，可以根据需要访问工具来回答用户的问题！")
//                .defaultToolCallbacks(toolCallback)
                .defaultToolNames("currentWeatherFunction")
                .build();
        this.openAiChatModel = openAiChatModel;
        this.currencyTools = currencyTools;
    }

    @PostMapping("/v1/tool_calling")
    public String toolCalling(@RequestBody UserInput userInput) {

        ToolCallback[] tools = ToolCallbacks.from(
                new DateTimeTools(),
                currencyTools
        );

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt()
                .user(userInput.prompt())
                .advisors(new SimpleLoggerAdvisor())
                .toolCallbacks(tools);

        log.info("requestSpec: {}", requestSpec);

        return requestSpec
                .call()
                .content();
    }

    @PostMapping("/v2/tool_calling/custom")
    public ChatResponse toolCallingCustom(@RequestBody UserInput userInput) {

//        ToolCallback[] tools = ToolCallbacks.from(new DateTimeTools());
        ToolCallingManager toolCallingManager = ToolCallingManager.builder().build();

        ChatOptions chatOptions = ToolCallingChatOptions.builder()
//                .toolCallbacks(tools)
                .internalToolExecutionEnabled(false)
                .build();
        Prompt prompt = new Prompt(userInput.prompt(), chatOptions);

        ChatResponse chatResponse = openAiChatModel.call(prompt);
        log.info(" chatResponse : {} ", chatResponse);
        while (chatResponse.hasToolCalls()) {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, chatResponse);

            prompt = new Prompt(toolExecutionResult.conversationHistory(), chatOptions);

            chatResponse = openAiChatModel.call(prompt);
        }

        return chatResponse;
    }
}
