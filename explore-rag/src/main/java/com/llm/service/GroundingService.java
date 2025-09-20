package com.llm.service;


import com.llm.dtos.GroundingRequest;
import com.llm.dtos.GroundingResponse;
import org.apache.commons.lang3.StringUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GroundingService {

    private static final Logger log = LoggerFactory.getLogger(GroundingService.class);

    private final ChatClient chatClient;

    private String handbookContent;

    @Value("classpath:/prompt-templates/RAG-Prompt.st")
    private Resource ragPrompt;

    private final PgVectorStore vectorStore;

    @Value("classpath:/prompt-templates/RAG-QA-Prompt.st")
    private Resource ragQAPrompt;

    public GroundingService(ChatClient.Builder chatClientBuilder,
                            @Qualifier("qaVectorStore") PgVectorStore vectorStore) {
        this.chatClient = chatClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    public GroundingResponse grounding(GroundingRequest groundingRequest) {
        PromptTemplate promptTemplate = new PromptTemplate(ragPrompt);
        Message promptMessage = promptTemplate.createMessage(
                Map.of("input", groundingRequest.prompt(),
                        "context", handbookContent));
        Prompt prompt = new Prompt(List.of(promptMessage));
        String response = chatClient.prompt(prompt).call().content();
        return new GroundingResponse(response);
    }

    @PostConstruct
    public void init() throws IOException {
        Path filePath = Paths.get("explore-rag/src/main/resources/docs/technova-handbook.txt");
        handbookContent = Files.readString(filePath);
    }

    public GroundingResponse retrieveAnswer(GroundingRequest groundingRequest) {
        List<Document> results = vectorStore
                .doSimilaritySearch(SearchRequest.builder()
                        .query(groundingRequest.prompt())
                        .build());

        log.info("results size : {} ", results.size());

        String context = results.stream()
                .filter(Objects::nonNull)
                .filter(result -> result.getScore() != null && result.getScore() > 0.8)
                .limit(2)
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        log.info("context :  {} ", context);

        if (StringUtils.isNotEmpty(context)) {
            log.info("Matched context :  {} ", context);

            PromptTemplate promptTemplate = new PromptTemplate(ragQAPrompt);
            Message promptMessage = promptTemplate.createMessage(
                    Map.of("input", groundingRequest.prompt(),
                            "context", context));

            Prompt prompt = new Prompt(List.of(promptMessage));
            String response = chatClient.prompt(prompt)
                    .call()
                    .content();
            log.info("response : {} ", response);
            return new GroundingResponse(response);

        } else {
            log.info("没有相关上下文，因此发送默认响应");
            return new GroundingResponse("抱歉，未找到相关信息");
        }

    }
}
