package com.llm.tool_calling.currency;

import com.llm.tool_calling.currency.dtos.CurrencyRequest;
import com.llm.tool_calling.currency.dtos.CurrencyResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CurrencyTools {
    private static final Logger log = LoggerFactory.getLogger(CurrencyTools.class);

    private final RestClient restClient;
    private final CurrencyExchangeConfigProperties currencyExchangeConfigProperties;

    public CurrencyTools(RestClient restClient, CurrencyExchangeConfigProperties currencyExchangeConfigProperties) {
        this.restClient = RestClient.create(currencyExchangeConfigProperties.baseUrl());
        this.currencyExchangeConfigProperties = currencyExchangeConfigProperties;
    }

//    @Tool(description = "获取最新货币汇率")
    @Tool(description = "获取最新的货币汇率。对于多种货币转换，请使用逗号分隔符号值")
    public CurrencyResponse getCurrencyRates(CurrencyRequest currencyRequest) {
        log.info("调用 RestClient CurrencyTools - getCurrencyRates: {}", currencyRequest);
        
        try {
            CurrencyResponse response = restClient
                    .get()
                    .uri("/latest.json?app_id={key}&base={base}&symbols={symbols}",
                            currencyExchangeConfigProperties.apiKey(),
                            currencyRequest.base(),
                            currencyRequest.symbols())
                    .retrieve()
                    .body(CurrencyResponse.class);

            log.info("response : {}", response);
            return response;

        } catch (Exception e) {
            log.error("获取货币汇率时出错 : ", e);
            throw  e;
        }

    }
}