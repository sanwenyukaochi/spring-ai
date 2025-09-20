package com.llm.service;

import com.llm.utils.RagUtiils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
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

    @Value("${ingestion.enabled:false}")
    private boolean ingestionEnabled;

    public IngestionService(@Qualifier(value = "qaVectorStore") PgVectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {

//        ingestPDFDocs(faqPdf);
    }

    public void ingest(byte[] fileContent, String fileName, String ingestType) {
        log.info("IngestionService 已调用 - 使用 fileName：{}，ingestType：{}", fileName, ingestType);
        Resource docSource = new ByteArrayResource(fileContent) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };

        String fileExtension = RagUtiils.getFileExtension(fileName);
        switch (fileExtension) {
            case "pdf" -> {
                log.info("正在导入 PDF 文件: {}", fileName);
                // 在此处实现 PDF 提取逻辑
                ingestPDFDocs(ingestType, docSource);
            }
            case "docx" -> {
                log.info("正在导入 DOCX 文件: {}", fileName);
                // 在此处实现 DOCX 提取逻辑
                ingestWordDocs(ingestType, docSource);
            }
            case "txt" -> {
                log.info("Ingesting txt file: {}", fileName);
                // 在此处实现 TXT 提取逻辑
                ingestTextDocs(ingestType, docSource);
            }
            default -> throw new IllegalArgumentException("不支持的文件类型: " + fileExtension);
        }
    }

    private void ingestPDFDocs(String ingestType, Resource pdfResource) {
        log.info("提取 PDF 文档");
        List<Document> docs = getPDFDocuments(ingestType, pdfResource);
        vectorStore.add(docs);
        log.info("已成功从 pdf 中提取 {} 个文档", docs.size());
    }

    private void ingestWordDocs(String ingestType, Resource docSource) {
        log.info("提取 DOCX 文档");
        List<Document> docs = getWordDocuments(docSource, ingestType);
        vectorStore.add(docs);
        log.info("已成功从 word 中提取 {} 个文档", docs.size());
    }

    private static List<Document> getPDFDocuments(String ingestType, Resource pdfResource) {
        try {
            return switch (ingestType) {
                case "page" -> new PagePdfDocumentReader(pdfResource).get();
                case "paragraph" -> new ParagraphPdfDocumentReader(pdfResource).get();
                default -> throw new IllegalArgumentException("提取类型无效: " + ingestType);
            };
        } catch (Exception e) {
            log.error("读取 PDF 文档时出错: {}", e.getMessage(), e);
            throw new RuntimeException("读取 PDF 文档时出错", e);
        }
    }

    private static List<Document> getWordDocuments(Resource docSource, String ingestType) {
        List<Document> docs = new TikaDocumentReader(docSource).get();
        return switch (ingestType) {
            case "token" -> {
//                TokenTextSplitter splitter = new TokenTextSplitter();
                TokenTextSplitter splitter = new TokenTextSplitter(250, 150,
                        10, 5000, true);
                yield splitter.apply(docs);
            }
            default -> docs;
        };
    }

    private void ingestTextDocs(String ingestType, Resource docSource) {
        log.info("提取文本文档");
        TextReader textReader = new TextReader(docSource);
        textReader.getCustomMetadata().put("filename", docSource.getFilename());
        List<Document> docs = textReader.read();
        vectorStore.add(docs);
        log.info("成功从文本文件中提取 {} 个文档", docs.size());
    }
}

