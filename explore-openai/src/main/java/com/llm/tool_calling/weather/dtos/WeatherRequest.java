package com.llm.tool_calling.weather.dtos;

import org.springframework.ai.tool.annotation.ToolParam;

public record WeatherRequest(@ToolParam(description = "城市或国家的名称")String city) {
}
