package com.deepthink.org.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.deepthink.org.dto.SiteRequest;
import com.deepthink.org.dto.SiteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

@Service
public class SiteGeneratorService {

    private final WebClient gemini;
    private final ObjectMapper mapper = new ObjectMapper();
    private final Path projectsRoot = Paths.get("projects");

    @Value("${gemini.api.key}")
    private String geminiKey;

    public SiteGeneratorService(WebClient.Builder builder) {
        this.gemini = builder
            .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent")
            .build();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(projectsRoot);
        } catch (IOException e) {
            System.err.println("Could not create projects root: " + e.getMessage());
        }
    }

    public SiteResponse generateSite(SiteRequest req) {
        String rawResponse = null;
        int maxRetries = 3;
        Duration backoff = Duration.ofSeconds(1);

        // Enhanced prompt to ensure only JSON is returned
        String prompt = "Generate a fully responsive website based on the following prompt using HTML, CSS, Bootstrap, and JavaScript along with 5-10 online images. "
                + "Your response must be a single JSON object with exactly three keys: 'indexHtml', 'stylesCss', and 'scriptsJs', each containing the corresponding code as a string. "
                + "Ensure that the response contains no additional text, comments, or explanations. The JSON object should be the only content in your response, starting with '{' and ending with '}'. "
                + "Prompt: " + req.prompt();

        // Fetch response from Gemini API
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                rawResponse = gemini.post()
                    .uri("?key=" + geminiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of(
                        "contents", List.of(
                            Map.of(
                                "role", "user",
                                "parts", List.of(
                                    Map.of("text", prompt)
                                )
                            )
                        ),
                        "generationConfig", Map.of(
                            "temperature", 0.2,
                            "candidateCount", 1,
                            "maxOutputTokens", 10000
                        )
                    ))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
                break;
            } catch (WebClientResponseException e) {
                if (e instanceof WebClientResponseException.TooManyRequests) {
                    try { Thread.sleep(backoff.toMillis()); } catch (InterruptedException ignored) {}
                    backoff = backoff.multipliedBy(2);
                } else {
                    String errorHtml = """
                        <html><body>
                          <h2>⚠️ API Error</h2>
                          <p>Error: %s - %s</p>
                        </body></html>
                    """.formatted(e.getStatusCode(), e.getMessage());
                    return new SiteResponse(null, null, errorHtml, "API error: " + e.getMessage());
                }
            }
        }

        if (rawResponse == null) {
            String errorHtml = """
                <html><body>
                  <h2>⚠️ Service Unavailable</h2>
                  <p>Please try again in a minute.</p>
                </body></html>
            """;
            return new SiteResponse(null, null, errorHtml, "Service unavailable after retries");
        }

        // Parse the response envelope
        JsonNode envelope;
        try {
            envelope = mapper.readTree(rawResponse);
        } catch (IOException e) {
            String errorHtml = """
                <html><body>
                  <h2>⚠️ Parsing Error</h2>
                  <p>Could not parse API response.</p>
                </body></html>
            """;
            return new SiteResponse(null, null, errorHtml, "Failed to parse Gemini envelope: " + e.getMessage());
        }

        // Check for finishReason
        String finishReason = envelope.path("candidates").path(0).path("finishReason").asText();
        if ("MAX_TOKENS".equals(finishReason)) {
            String errorHtml = """
                <html><body>
                  <h2>⚠️ Response Truncated</h2>
                  <p>The generated website is too large. Try simplifying your prompt.</p>
                </body></html>
            """;
            return new SiteResponse(null, null, errorHtml, "Response truncated due to token limit");
        }

        // Extract the text from candidates[0].content.parts[0]
        JsonNode parts = envelope.path("candidates").path(0).path("content").path("parts");
        if (!parts.isArray() || parts.size() == 0) {
            String errorHtml = """
                <html><body>
                  <h2>⚠️ Invalid Response</h2>
                  <p>No content found in API response.</p>
                </body></html>
            """;
            return new SiteResponse(null, null, errorHtml, "Unexpected Gemini response shape");
        }

        String fragment = parts.get(0).path("text").asText();
        if (fragment.isEmpty()) {
            String errorHtml = """
                <html><body>
                  <h2>⚠️ Empty Response</h2>
                  <p>The API returned no content.</p>
                </body></html>
            """;
            return new SiteResponse(null, null, errorHtml, "Empty content in response");
        }

        // Extract JSON from the fragment
        String jsonText = extractJson(fragment);

        // Parse the JSON
        JsonNode siteNode;
        try {
            siteNode = mapper.readTree(jsonText);
        } catch (IOException e) {
            String errorHtml = """
                <html><body>
                  <h2>⚠️ Generation Failed</h2>
                  <p>We couldn't generate the website right now. Please try a simpler prompt or check back later.</p>
                </body></html>
            """;
            return new SiteResponse(null, null, errorHtml, "Failed to parse site JSON: " + e.getMessage());
        }

        // Validate and extract fields
        String html = getMandatory(siteNode, "indexHtml");
        String css = getMandatory(siteNode, "stylesCss");
        String js = getMandatory(siteNode, "scriptsJs");

        // Persist files
        String projectId = UUID.randomUUID().toString();
        Path proj = projectsRoot.resolve(projectId);
        try {
            Files.createDirectories(proj);
            Files.writeString(proj.resolve("index.html"), html);
            Files.writeString(proj.resolve("styles.css"), css);
            Files.writeString(proj.resolve("scripts.js"), js);
        } catch (IOException e) {
            String errorHtml = """
                <html><body>
                  <h2>⚠️ File Error</h2>
                  <p>Could not save generated files.</p>
                </body></html>
            """;
            return new SiteResponse(null, null, errorHtml, "Failed writing project files: " + e.getMessage());
        }

        // Create ZIP
        Path zip = projectsRoot.resolve(projectId + ".zip");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
            for (String fname : List.of("index.html", "styles.css", "scripts.js")) {
                zos.putNextEntry(new ZipEntry(fname));
                zos.write(Files.readAllBytes(proj.resolve(fname)));
                zos.closeEntry();
            }
        } catch (IOException e) {
            String errorHtml = """
                <html><body>
                  <h2>⚠️ ZIP Error</h2>
                  <p>Could not create ZIP file.</p>
                </body></html>
            """;
            return new SiteResponse(null, null, errorHtml, "Failed creating ZIP: " + e.getMessage());
        }

        String zipUrl = "/api/projects/" + projectId + ".zip";
        return new SiteResponse(projectId, zipUrl, html, null);
    }

    private String extractJson(String text) {
        // Remove possible code fences
        text = text.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
        
        // Find the first '{' and the last '}'
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        
        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end + 1);
        } else {
            return text; // Fallback to original text
        }
    }

    private String getMandatory(JsonNode node, String key) {
        JsonNode v = node.get(key);
        if (v == null || v.isNull() || v.asText().isEmpty()) {
            throw new RuntimeException("Gemini JSON missing required key: " + key);
        }
        return v.asText();
    }

    public Resource loadZipAsResource(String filename) {
        try {
            Path zip = projectsRoot.resolve(filename);
            return new UrlResource(zip.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not load ZIP resource", e);
        }
    }
}