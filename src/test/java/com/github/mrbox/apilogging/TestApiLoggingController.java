package com.github.mrbox.apilogging;

import lombok.Data;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 用于测试ApiLoggingAspect切面
 */
@Controller
@RequestMapping("/test-apilogging")
public class TestApiLoggingController {

    @GetMapping("/get")
    @ResponseBody
    public String getRequest(@RequestParam String paramA, @RequestParam String paramB) {
        return "Success: " + paramA + ", " + paramB;
    }

    @PostMapping("/form")
    @ResponseBody
    public String postFormRequest(String username, String password) {
        return "Form Submitted: " + username;
    }

    @PostMapping("/json")
    @ResponseBody
    public TestData postJsonRequest(@RequestBody TestData data) {
        TestData response = new TestData();
        response.setField1(data.getField1() + "_processed");
        response.setField2(data.getField2() + "_processed");
        return response;
    }

    @PostMapping("/upload")
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file, @RequestParam("metadata") String metadata)
            throws IOException {
        return "File uploaded: " + file.getOriginalFilename() + ", size: " + file.getSize() + ", metadata: " + metadata;
    }

    @GetMapping("/download")
    @ResponseBody
    public ResponseEntity<Resource> fileDownload() {
        byte[] content = "Test download content".getBytes();
        ByteArrayResource resource = new ByteArrayResource(content);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=test-download.txt")
                .body(resource);
    }

    @GetMapping("/exception")
    @ResponseBody
    public String throwException() {
        throw new CustomTestException("接口异常");
    }

    @GetMapping("/error-status")
    @ResponseBody
    public ResponseEntity<String> errorStatus() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body("Forbidden access");
    }

    @GetMapping("/status/{code}")
    @ResponseBody
    public ResponseEntity<String> customStatus(@PathVariable int code) {
        return ResponseEntity.status(code)
                .body("Status code: " + code);
    }

    @PostMapping("test-void-response")
    public void aVoid(@RequestBody TestData data) {

    }

    @GetMapping("/void-response")
    @ResponseBody
    public void voidResponse() {
    }

    @PostMapping("/test-data-response")
    @ResponseBody
    public TestData testDataResponse(@RequestBody TestData data) {
        return data;
    }

    @PostMapping("/test-method-filter")
    @ResponseBody
    public String testMethodFilter() {
        return "ok";
    }

    @Data
    public static class TestData {
        private String field1;
        private String field2;
        private String sensitiveField;
    }

    public static class CustomTestException extends RuntimeException {
        public CustomTestException(String message) {
            super(message);
        }
    }
}
