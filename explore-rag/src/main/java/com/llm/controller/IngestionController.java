package com.llm.controller;

import com.llm.service.IngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
public class IngestionController {

    private static final Logger log = LoggerFactory.getLogger(IngestionController.class);

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping("/api/v1/files/ingest")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam("ingestType") String ingestType) {
        log.info("文件名称 : {}", file.getOriginalFilename());
        try {
            // 处理文件内容
            byte[] fileContent = file.getBytes();
            // 将内容导入矢量数据库
            ingestionService.ingest(fileContent, file.getOriginalFilename(), ingestType);
            return ResponseEntity.ok("文件上传并处理成功.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("无法处理文件.");
        }
    }
}
