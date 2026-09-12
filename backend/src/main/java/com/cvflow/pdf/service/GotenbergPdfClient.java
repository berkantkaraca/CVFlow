package com.cvflow.pdf.service;

import com.cvflow.common.exception.PdfGenerationException;
import com.cvflow.pdf.config.PdfProperties;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GotenbergPdfClient {
    private final RestClient restClient;
    private final PdfProperties properties;

    public GotenbergPdfClient(RestClient pdfRestClient, PdfProperties properties) {
        this.restClient = pdfRestClient;
        this.properties = properties;
    }

    public byte[] convertHtmlToPdf(String html) {
        String boundary = "cvflow-" + UUID.randomUUID().toString().replace("-", "");
        byte[] multipart = multipartBody(boundary, html);
        try {
            byte[] pdf = restClient
                    .post()
                    .uri(properties.baseUrl() + "/forms/chromium/convert/html")
                    .contentType(MediaType.parseMediaType("multipart/form-data; boundary=" + boundary))
                    .body(multipart)
                    .retrieve()
                    .onStatus(
                            status -> status.isError(),
                            (request, response) -> {
                                throw new PdfGenerationException(
                                        "PDF service responded with status " + response.getStatusCode());
                            })
                    .body(byte[].class);
            if (pdf == null || pdf.length == 0) {
                throw new PdfGenerationException("PDF service returned an empty document");
            }
            return pdf;
        } catch (PdfGenerationException ex) {
            throw ex;
        } catch (RestClientException ex) {
            throw new PdfGenerationException("PDF service is unavailable");
        }
    }

    private byte[] multipartBody(String boundary, String html) {
        String partHeader = "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; name=\"files\"; filename=\"index.html\"\r\n"
                + "Content-Type: text/html\r\n\r\n";
        String closing = "\r\n--" + boundary + "--\r\n";
        byte[] header = partHeader.getBytes(StandardCharsets.UTF_8);
        byte[] content = html.getBytes(StandardCharsets.UTF_8);
        byte[] footer = closing.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[header.length + content.length + footer.length];
        System.arraycopy(header, 0, body, 0, header.length);
        System.arraycopy(content, 0, body, header.length, content.length);
        System.arraycopy(footer, 0, body, header.length + content.length, footer.length);
        return body;
    }
}
