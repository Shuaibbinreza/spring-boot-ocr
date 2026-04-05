package com.generic.pdf_reader.service;

import com.generic.pdf_reader.entity.PDFDocument;
import com.generic.pdf_reader.repository.PDFDocumentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class PDFService {

    private final PDFDocumentRepository repository;

    public PDFService(PDFDocumentRepository repository) {
        this.repository = repository;
    }

    public PDFDocument uploadPDF(MultipartFile file) throws IOException {
        byte[] pdfData = file.getBytes();
        
        PDDocument pdDocument = Loader.loadPDF(pdfData);
        PDFTextStripper stripper = new PDFTextStripper();
        String text = stripper.getText(pdDocument);
        pdDocument.close();

        PDFDocument doc = new PDFDocument(file.getOriginalFilename(), pdfData, text);
        return repository.save(doc);
    }

    public List<PDFDocument> getAllPDFs() {
        return repository.findAllByOrderByUploadedAtDesc();
    }

    public Optional<PDFDocument> getPDFById(Long id) {
        return repository.findById(id);
    }

    public void deletePDF(Long id) {
        repository.deleteById(id);
    }
}
