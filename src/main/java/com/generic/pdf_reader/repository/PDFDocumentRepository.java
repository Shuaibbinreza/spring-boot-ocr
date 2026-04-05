package com.generic.pdf_reader.repository;

import com.generic.pdf_reader.entity.PDFDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PDFDocumentRepository extends JpaRepository<PDFDocument, Long> {
    List<PDFDocument> findAllByOrderByUploadedAtDesc();
}
