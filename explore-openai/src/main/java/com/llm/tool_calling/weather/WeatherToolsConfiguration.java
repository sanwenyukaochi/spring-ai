package com.llm.tool_calling.weather;

import com.llm.tool_calling.weather.dtos.WeatherRequest;
import com.llm.tool_calling.weather.dtos.WeatherResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration(proxyBeanMethods = false)
public class WeatherToolsConfiguration {

    public static final String CURRENT_WEATHER_TOOL = "currentWeatherFunction";
    
    private final WeatherConfigProperties weatherProps;

    public WeatherToolsConfiguration(WeatherConfigProperties weatherProps) {
        this.weatherProps = weatherProps;
    }

    @Bean(CURRENT_WEATHER_TOOL)
    @Description("获取给定城市的当前天气状况。")
    public Function<WeatherRequest,WeatherResponse> currentWeatherFunction(){
        return new WeatherToolsFunction(weatherProps);
    }
}
