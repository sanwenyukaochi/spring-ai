package com.llm.tool_calling.currenttime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeTools {
    
    private static final Logger log = LoggerFactory.getLogger(DateTimeTools.class);
    
    @Tool(
            description = "获取用户时区的当前日期和时间"
    )
    public String getCurrentDateTimeWithoutZone(){
        log.info("调用 DateTimeTools - getCurrentDateTime");
        return LocalDateTime.now()
                .atZone(LocaleContextHolder.getTimeZone().toZoneId())
                .toString();
    }

    @Tool(
            description = "获取指定时区的当前日期和时间"
    )
    public String getCurrentDateTime(String timeZone) {
        log.info("调用 DateTimeTools - getCurrentDateTime timeZone : {} ", timeZone);
        try{
            ZoneId zoneId = ZoneId.of(timeZone);
            ZonedDateTime zonedDateTime = ZonedDateTime.now(zoneId);
            return zonedDateTime.toString();
        }catch (Exception e){
            log.error("提供的时区无效: {}", timeZone, e);
            return "提供的时区无效: " + timeZone;
        }

    }
}
