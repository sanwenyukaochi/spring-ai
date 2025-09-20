package com.llm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IngestionService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;

    @Value("classpath:/docs/Flexora_FAQ.pdf")
    private Resource faqPdf;

    @Value("${ingestion.enabled:true}")
    private boolean ingestionEnabled;

    public IngestionService(@Qualifier(value = "qaVectorStore") PgVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {

//        ingestPDFDocs(faqPdf);
    }

    private void ingestPDFDocs(Resource pdfResource) {

        if (ingestionEnabled) {
            List<Document> docs = new PagePdfDocumentReader(pdfResource).get();
            log.info("PDF 文档内容：{}，大小：{}", docs, docs.size());
            vectorStore.add(docs);
            log.info("已成功从 pdf 中提取 {} 个文档", docs.size());
        }
    }

    public void ingest(byte[] fileContent, String fileName, String ingestType) {
        log.info("IngestionService 已调用 - 使用 fileName：{}，ingestType：{}", fileName, ingestType);
        Resource docResource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
        
        ingestPDFDocs(docResource);
        log.info("提取已成功完成.");
    }
}

