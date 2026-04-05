package com.generic.pdf_reader.controller;

import com.generic.pdf_reader.entity.PDFDocument;
import com.generic.pdf_reader.service.PDFService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
public class PDFController {

    private final PDFService pdfService;

    public PDFController(PDFService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping("/api/pdfs")
    @ResponseBody
    public List<PDFDocument> getAllPDFs() {
        return pdfService.getAllPDFs();
    }

    @PostMapping("/api/pdfs")
    public ResponseEntity<PDFDocument> uploadPDF(@RequestParam("file") MultipartFile file) throws IOException {
        if (!file.getContentType().equals("application/pdf")) {
            return ResponseEntity.badRequest().build();
        }
        PDFDocument doc = pdfService.uploadPDF(file);
        return ResponseEntity.ok(doc);
    }

    @GetMapping("/api/pdfs/{id}")
    public ResponseEntity<PDFDocument> getPDF(@PathVariable Long id) {
        return pdfService.getPDFById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/pdfs/{id}")
    public ResponseEntity<Void> deletePDF(@PathVariable Long id) {
        pdfService.deletePDF(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/pdfs/{id}/file")
    public ResponseEntity<ByteArrayResource> serveFile(@PathVariable Long id) {
        return pdfService.getPDFById(id)
                .map(doc -> {
                    ByteArrayResource resource = new ByteArrayResource(doc.getPdfData());
                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getName() + "\"")
                            .contentType(MediaType.APPLICATION_PDF)
                            .contentLength(doc.getPdfData().length)
                            .body(resource);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
