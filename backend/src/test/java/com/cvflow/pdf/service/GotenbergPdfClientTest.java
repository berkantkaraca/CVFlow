package com.cvflow.pdf.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cvflow.common.exception.PdfGenerationException;
import com.cvflow.pdf.config.PdfProperties;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class GotenbergPdfClientTest {
    private HttpServer server;
    private GotenbergPdfClient client;
    private volatile int status = 200;
    private volatile byte[] body = "%PDF-1.4 fake".getBytes(StandardCharsets.UTF_8);
    private volatile String receivedContentType;

    @BeforeEach
    void setup() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/forms/chromium/convert/html", exchange -> {
            receivedContentType = exchange.getRequestHeaders().getFirst("Content-Type");
            exchange.getResponseHeaders().add("Content-Type", "application/pdf");
            exchange.sendResponseHeaders(status, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        String baseUrl = "http://localhost:" + server.getAddress().getPort();
        client = new GotenbergPdfClient(RestClient.builder().build(), new PdfProperties(baseUrl));
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    @Test
    void convertsHtmlToPdf() {
        byte[] pdf = client.convertHtmlToPdf("<html><body>hi</body></html>");

        assertArrayEquals(body, pdf);
        assertTrue(receivedContentType.startsWith("multipart/form-data"));
    }

    @Test
    void mapsErrorStatusToPdfException() {
        status = 500;

        PdfGenerationException ex =
                assertThrows(PdfGenerationException.class, () -> client.convertHtmlToPdf("<html></html>"));
        assertEquals("PDF_GENERATION_FAILED", ex.getCode());
    }

    @Test
    void mapsEmptyDocumentToPdfException() {
        body = new byte[0];

        assertThrows(PdfGenerationException.class, () -> client.convertHtmlToPdf("<html></html>"));
    }
}
