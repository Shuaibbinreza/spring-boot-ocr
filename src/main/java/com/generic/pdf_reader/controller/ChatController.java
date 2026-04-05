package com.generic.pdf_reader.controller;

import com.generic.pdf_reader.service.ChatService;
import com.generic.pdf_reader.service.PDFService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;
    private final PDFService pdfService;

    public ChatController(ChatService chatService, PDFService pdfService) {
        this.chatService = chatService;
        this.pdfService = pdfService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        Long pdfId = Long.parseLong(request.get("pdfId"));
        String message = request.get("message");

        return pdfService.getPDFById(pdfId)
                .map(pdf -> {
                    String response = chatService.chatWithPDF(pdf.getContent(), message);
                    return ResponseEntity.ok(Map.of("response", response));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
