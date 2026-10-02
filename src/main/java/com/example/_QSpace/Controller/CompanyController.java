package com.example._QSpace.Controller;

import com.example._QSpace.Entity.Company;
import com.example._QSpace.Repository.CompanyRepo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/")
@CrossOrigin(origins = "*")
public class CompanyController {

    private static final Logger logger = LoggerFactory.getLogger(CompanyController.class);
    private static final String BACKEND_BASE_URL = "http://localhost:8080";

    private final CompanyRepo repo;
    private final HttpClient httpClient;

    public CompanyController(CompanyRepo repo) {
        this.repo = repo;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @PostMapping("register")
    public ResponseEntity<Map<String, String>> registerCompany(
            @RequestParam("companyName") String companyName,
            @RequestParam("companyEmail") String companyEmail,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam("deviceId") String deviceId,
            @RequestParam(value = "companyLogo", required = false) MultipartFile logoFile) {

        String logoPath = null;

        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String uploadDir = System.getProperty("user.dir") + "/uploads/";
                File dir = new File(uploadDir);
                if (!dir.exists()) dir.mkdirs();

                String fileName = System.currentTimeMillis() + "_" + logoFile.getOriginalFilename();
                File dest = new File(uploadDir + fileName);
                logoFile.transferTo(dest);

                logoPath = "/uploads/" + fileName;
            } catch (IOException e) {
                logger.error("Failed to upload company logo", e);
            }
        }

        Company company = new Company();
        company.setCompanyName(companyName.trim());
        company.setCompanyEmail(companyEmail.trim());
        company.setPhoneNumber(phoneNumber.trim());
        company.setDeviceId(deviceId.trim());
        company.setCompanyLogo(logoPath);

        repo.save(company);

        // Call 3QS_backend: 1. POST /tenant  2. GET /tenant/{companyName}/qr?deviceId={deviceId}
        registerTenantOnBackend(companyName.trim(), companyEmail.trim(), deviceId.trim());
        byte[] qrBytes = fetchQrFromBackend(companyName.trim(), deviceId.trim());

        Map<String, String> response = new HashMap<>();
        response.put("message", "Company and Device registered successfully");
        response.put("companyName", companyName.trim());
        response.put("deviceId", deviceId.trim());

        if (qrBytes != null && qrBytes.length > 0) {
            String base64Qr = "data:image/png;base64," + Base64.getEncoder().encodeToString(qrBytes);
            response.put("qrCode", base64Qr);
        } else {
            response.put("qrUrl", BACKEND_BASE_URL + "/tenant/" + URLEncoder.encode(companyName.trim(), StandardCharsets.UTF_8).replace("+", "%20") + "/qr?deviceId=" + URLEncoder.encode(deviceId.trim(), StandardCharsets.UTF_8));
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("api/qr")
    public ResponseEntity<?> getQrImage(
            @RequestParam("companyName") String companyName,
            @RequestParam("deviceId") String deviceId) {

        byte[] qrBytes = fetchQrFromBackend(companyName.trim(), deviceId.trim());
        if (qrBytes != null && qrBytes.length > 0) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(qrBytes, headers, HttpStatus.OK);
        }

        Map<String, String> err = new HashMap<>();
        err.put("error", "Unable to retrieve QR code from backend. Ensure 3QS_backend is running on port 8080.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    private void registerTenantOnBackend(String companyName, String companyEmail, String deviceId) {
        try {
            String jsonPayload = String.format("{\"companyName\":\"%s\",\"emailId\":\"%s\",\"deviceId\":\"%s\"}",
                    escapeJson(companyName), escapeJson(companyEmail), escapeJson(deviceId));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BACKEND_BASE_URL + "/tenant"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload, StandardCharsets.UTF_8))
                    .timeout(Duration.ofSeconds(6))
                    .build();

            HttpResponse<String> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("3QS_backend POST /tenant response code: {}", httpResponse.statusCode());
        } catch (Exception e) {
            logger.warn("Could not register tenant on 3QS_backend at {}: {}", BACKEND_BASE_URL, e.getMessage());
        }
    }

    private byte[] fetchQrFromBackend(String companyName, String deviceId) {
        try {
            String encodedCompany = URLEncoder.encode(companyName, StandardCharsets.UTF_8).replace("+", "%20");
            String encodedDevice = URLEncoder.encode(deviceId, StandardCharsets.UTF_8);
            String qrUrl = BACKEND_BASE_URL + "/tenant/" + encodedCompany + "/qr?deviceId=" + encodedDevice;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(qrUrl))
                    .header("Accept", "image/png")
                    .GET()
                    .timeout(Duration.ofSeconds(6))
                    .build();

            HttpResponse<byte[]> httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (httpResponse.statusCode() == 200 && httpResponse.body() != null && httpResponse.body().length > 0) {
                return httpResponse.body();
            } else {
                logger.warn("3QS_backend GET QR returned HTTP {}: {}", httpResponse.statusCode(), httpResponse.uri());
            }
        } catch (Exception e) {
            logger.warn("Could not fetch QR code from 3QS_backend: {}", e.getMessage());
        }
        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}