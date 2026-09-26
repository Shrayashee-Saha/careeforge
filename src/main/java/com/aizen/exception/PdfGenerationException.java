package com.aizen.exception;

/**
 * Checked exception thrown by PdfService when Apache PDFBox fails to
 * render or save a resume/cover-letter document (I/O error, stream
 * corruption, disk full, etc.).
 */
public class PdfGenerationException extends Exception {

    public PdfGenerationException(String message) {
        super(message);
    }

    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
