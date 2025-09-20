package com.llm.image;

import com.llm.dto.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * https://platform.openai.com/docs/api-reference/images/create
 */
@RestController
public class ImageController {
    private static final Logger log = LoggerFactory.getLogger(ImageController.class);
    public OpenAiImageModel openAiImageModel;

    public ImageController(OpenAiImageModel openAiImageModel) {
        this.openAiImageModel = openAiImageModel;
    }

    @PostMapping("/v1/images")
    public ImageResponse images(@RequestBody UserInput userInput) {
        log.info("userInput消息提示是: {} ", userInput);
        ImageResponse response = openAiImageModel.call(new ImagePrompt(userInput.prompt()));
        return response;
    }

}
