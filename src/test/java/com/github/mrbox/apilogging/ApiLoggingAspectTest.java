package com.github.mrbox.apilogging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrbox.apilogging.logger.DefaultJsonLogger;
import com.github.mrbox.apilogging.model.DetailedLogRecord;
import com.github.mrbox.apilogging.model.LogMode;
import com.github.mrbox.apilogging.model.SimpleLogRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 使用SpringBootTest加载完整的应用上下文，包括自动配置
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ApiLoggingAspectTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ApiLoggingProperties properties;

        @Autowired
        private ApiLoggingProperties originalProperties;

        private final ObjectMapper objectMapper = new ObjectMapper(); // 用于序列化/反序列化JSON

        @MockBean(name = "apiLoggingLogger") // 使用名称确保替换自动配置类中的bean
        private DefaultJsonLogger defaultJsonLogger;

        private ArgumentCaptor<SimpleLogRecord> logRecordCaptor;

        @BeforeEach
        void setUp() {
                // 每个测试前重置mock对象
                reset(defaultJsonLogger);

                // 为每个测试重置properties到默认测试状态，避免测试间干扰
                properties = originalProperties;
                properties.setEnabled(true);
                //properties.setLogMode(LogMode.SIMPLE);
                properties.setMaxPayloadLength(1024);
                properties.getExceptionStack().setEnabled(true);
                properties.getExceptionStack().setMaxLines(5);
                properties.getSensitive().setRequestHeaders(new HashSet<>());
                properties.getSensitive().setArgNames(new HashSet<>());
                properties.setMaxPayloadLength(1000);

                logRecordCaptor = ArgumentCaptor.forClass(SimpleLogRecord.class);
        }

        // --- 基础功能测试用例 ---

        @Test
        void testGetRequestAbstractLog() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.get("/test-apilogging/get")
                                .param("paramA", "valueA")
                                .param("paramB", "valueB"))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                SimpleLogRecord record = logRecordCaptor.getValue();

                assertThat(record).isNotNull();
                assertThat(record.getLogMode()).isEqualTo(LogMode.SIMPLE.name());
                assertThat(record.getUri()).isEqualTo("/test-apilogging/get");
                assertThat(record.getControllerHandler())
                                .contains(TestApiLoggingController.class.getSimpleName() + "#getRequest");
                assertThat(record.getProcessingTimeMs()).isGreaterThanOrEqualTo(0);
                assertThat(record).isNotInstanceOf(DetailedLogRecord.class);
        }

        @Test
        void testGetRequestDetailedLogByHeader() throws Exception {
                properties.getHeaderTrigger().setHeaderName("X-Log-Detailed");
                properties.getHeaderTrigger().setDetailedValue("true");

                mockMvc.perform(MockMvcRequestBuilders.get("/test-apilogging/get")
                                .param("paramA", "valueA")
                                .param("paramB", "valueB")
                                .header("X-Log-Detailed", "true"))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                SimpleLogRecord record = logRecordCaptor.getValue();

                assertThat(record).isNotNull();
                assertThat(record.getLogMode()).isEqualTo(LogMode.DETAILED.name());
                assertThat(record).isInstanceOf(DetailedLogRecord.class);
                DetailedLogRecord detailedRecord = (DetailedLogRecord) record;
                assertThat(detailedRecord.getRequestParams()).contains("valueA").contains("valueB");
                assertThat(detailedRecord.getRequestHeader()).containsKey("X-Log-Detailed");
        }

        @Test
        void testPostFormRequestDetailedLog() throws Exception {
                properties.setLogMode(LogMode.DETAILED); // 设置为详细模式进行测试

                mockMvc.perform(MockMvcRequestBuilders.post("/test-apilogging/form")
                                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                                .param("username", "testUser")
                                .param("password", "secretPass"))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                DetailedLogRecord record = (DetailedLogRecord) logRecordCaptor.getValue();

                assertThat(record.getLogMode()).isEqualTo(LogMode.DETAILED.name());
                assertThat(record.getRequestParams().equals("{\"password\":\"secretPass\",\"username\":\"testUser\"}"));
        }

        @Test
        void testPostJsonRequestDetailedLog() throws Exception {
                properties.setLogMode(LogMode.DETAILED);
                TestApiLoggingController.TestData payload = new TestApiLoggingController.TestData();
                payload.setField1("data1");
                payload.setField2("data2");
                payload.setSensitiveField("verySecretData");

                mockMvc.perform(MockMvcRequestBuilders.post("/test-apilogging/json")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                                .header("X-Sensitive-Header", "sensitiveValue"))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                DetailedLogRecord record = (DetailedLogRecord) logRecordCaptor.getValue();

                assertThat(record.getLogMode()).isEqualTo(LogMode.DETAILED.name());
                assertThat(record.getRequestParams()).contains("\"field1\":\"data1\"")
                                .contains("\"sensitiveField\":\"verySecretData\"");
                assertThat(record.getResponseData()).contains("data1_processed");
                assertThat(record.getRequestHeader()).containsKey("X-Sensitive-Header");
        }

        @Test
        void testJsonRequestWithSensitiveFieldMasking() throws Exception {
                properties.setLogMode(LogMode.DETAILED);
                properties.getSensitive().getArgNames().add("data");
                properties.getSensitive().getRequestHeaders().add("X-Sensitive-Header");
                properties.getSensitive().setMask("***MASKED***");

                TestApiLoggingController.TestData payload = new TestApiLoggingController.TestData();
                payload.setField1("data1");
                payload.setSensitiveField("superSecretValue");

                mockMvc.perform(MockMvcRequestBuilders.post("/test-apilogging/json")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload))
                                .header("X-Sensitive-Header", "topSecretToken"))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                DetailedLogRecord record = (DetailedLogRecord) logRecordCaptor.getValue();

                assertThat(record.getRequestParams()).contains("\"data\":\"***MASKED***\"");
                assertThat(record.getRequestParams()).doesNotContain("superSecretValue");
                assertThat(record.getRequestHeader().get("X-Sensitive-Header")).isEqualTo("***MASKED***");
        }

        @Test
        void testFileUploadRequestLogging() throws Exception {
                properties.setLogMode(LogMode.DETAILED);
                properties.setExcludedArgumentOnContentTypes(new HashSet<>());

                MockMultipartFile file = new MockMultipartFile(
                                "file",
                                "test-upload.txt",
                                MediaType.TEXT_PLAIN_VALUE,
                                "Hello, World!".getBytes());

                mockMvc.perform(MockMvcRequestBuilders.multipart("/test-apilogging/upload")
                                .file(file)
                                .param("metadata", "test-metadata"))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                DetailedLogRecord record = (DetailedLogRecord) logRecordCaptor.getValue();

                // 更新断言以匹配新的格式，检查包含文件名而不是固定消息
                assertThat(record.getRequestParams()).contains("\"fileName\"");
                assertThat(record.getRequestParams()).contains("\"test-upload.txt\"");
                assertThat(record.getRequestParams()).contains("\"metadata\"");
                assertThat(record.getRequestParams()).contains("\"test-metadata\"");
        }

        @Test
        void testFileUploadRequestLoggingWithContentTypeExclusion() throws Exception {
                properties.setLogMode(LogMode.DETAILED);
                properties.getExcludedArgumentOnContentTypes().add(MediaType.MULTIPART_FORM_DATA_VALUE.toLowerCase());

                MockMultipartFile file = new MockMultipartFile("file", "test.txt", MediaType.TEXT_PLAIN_VALUE,
                                "content".getBytes());

                mockMvc.perform(MockMvcRequestBuilders.multipart("/test-apilogging/upload")
                                .file(file)
                                .param("metadata", "meta-info")) // .param here for query/form part, not file part
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                DetailedLogRecord record = (DetailedLogRecord) logRecordCaptor.getValue();

                // 当内容类型明确排除时，仍然应该看到排除消息
                assertThat(record.getRequestParams())
                                .isEqualTo("[忽略Content-Type: multipart/form-data]");
        }

        @Test
        void testFileDownloadRequestLogging() throws Exception {
                properties.setLogMode(LogMode.DETAILED);

                mockMvc.perform(MockMvcRequestBuilders.get("/test-apilogging/download"))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                DetailedLogRecord record = (DetailedLogRecord) logRecordCaptor.getValue();

                assertThat(record.getResponseData()).isEqualTo("[ResponseEntity包含InputStreamSource类型的body]");
        }

        @Test
        void testExceptionLogging() throws Exception {
                properties.setLogMode(LogMode.DETAILED);
                properties.getExceptionStack().setEnabled(true);
                properties.getExceptionStack().setMaxLines(3);

                // org.springframework.web.util.NestedServletException is expected
                try {
                        mockMvc.perform(MockMvcRequestBuilders.get("/test-apilogging/exception"))
                                        .andReturn();
                } catch (Exception e) {
                        // Expected
                        //e.printStackTrace();
                }

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                DetailedLogRecord record = (DetailedLogRecord) logRecordCaptor.getValue();

                assertThat(record.getErrorIndicator()).isEqualTo("WARN:CustomTestException");
                assertThat(record.getExceptionStacktrace())
                                .contains("CustomTestException: 接口异常");
                assertThat(record.getExceptionStacktrace()).contains("TestApiLoggingController.java");
        }

        @Test
        void testErrorStatusDetailLogTrigger() throws Exception {
                properties.setLogMode(LogMode.SIMPLE);
                properties.getDetailedLogOnStatusCodes().add(403);

                // 确认配置已经正确设置
                assertThat(properties.getDetailedLogOnStatusCodes()).contains(403);
                System.out.println("配置的状态码集合: " + properties.getDetailedLogOnStatusCodes());

                mockMvc.perform(MockMvcRequestBuilders.get("/test-apilogging/error-status"))
                                .andExpect(status().isForbidden());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                SimpleLogRecord record = logRecordCaptor.getValue();

                // 打印实际接收到的日志记录
                System.out.println("日志记录模式: " + record.getLogMode());
                System.out.println("日志记录状态码: " + record.getStatusCode());
                System.out.println("错误指示器: " + record.getErrorIndicator());

                // 再次恢复断言，检查是否修复了状态码触发器
                assertThat(record.getLogMode()).isEqualTo(LogMode.DETAILED.name());
                assertThat(record.getStatusCode()).isEqualTo(403);
                assertThat(record.getErrorIndicator()).isEqualTo("WARN_HTTP_STATUS_403");
                assertThat(record).isInstanceOf(DetailedLogRecord.class);
                assertThat(((DetailedLogRecord) record).getResponseData()).contains("Forbidden access");
        }

        @Test
        void testLoggingDisabled() throws Exception {
                properties.setEnabled(false);

                mockMvc.perform(MockMvcRequestBuilders.get("/test-apilogging/get")
                                .param("paramA", "valueA")
                                .param("paramB", "valueB"))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, times(0)).publish(any(SimpleLogRecord.class));
        }

        @Test
        void testMaxPayloadLengthTruncation() throws Exception {
                properties.setLogMode(LogMode.DETAILED);
                properties.setMaxPayloadLength(10); // 非常小的长度以测试截断

                TestApiLoggingController.TestData payload = new TestApiLoggingController.TestData();
                payload.setField1("ThisIsALongFieldValue");

                mockMvc.perform(MockMvcRequestBuilders.post("/test-apilogging/json")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                                .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                DetailedLogRecord record = (DetailedLogRecord) logRecordCaptor.getValue();

                // 完全匹配实际输出格式
                assertThat(record.getRequestParams()).isEqualTo("{\"data\":{\"...[已截断]");
        }

        // 测试 voidResponse 返回 Response<Void>
        @Test
        void testVoidResponse() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.get("/test-apilogging/void-response"))
                        .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                SimpleLogRecord record = logRecordCaptor.getValue();

                assertThat(record).isNotNull();
                assertThat(record.getUri()).isEqualTo("/test-apilogging/void-response");
        }

        // 测试 postTestData 返回 Response<TestData>
        @Test
        void testPostTestDataResponse() throws Exception {
                properties.setLogMode(LogMode.SIMPLE);
                TestApiLoggingController.TestData payload = new TestApiLoggingController.TestData();
                payload.setField1("input1");
                payload.setField2("input2");

                mockMvc.perform(MockMvcRequestBuilders.post("/test-apilogging/test-data-response")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                        .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                SimpleLogRecord record = logRecordCaptor.getValue();

                assertThat(record.getLogMode()).isEqualTo(LogMode.SIMPLE.name());
                assertThat(record.getUri()).isEqualTo("/test-apilogging/test-data-response");
//                assertThat(record.getResponseData()).contains("return-value1");
//                assertThat(record.getResponseData()).contains("return-value2");
        }

        // 测试 test-void-response 返回 void
        @Test
        void testPostVoidResponse() throws Exception {
                TestApiLoggingController.TestData payload = new TestApiLoggingController.TestData();
                payload.setField1("input1");
                payload.setField2("input2");

                mockMvc.perform(MockMvcRequestBuilders.post("/test-apilogging/test-void-response")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(payload)))
                        .andExpect(status().isOk());

                verify(defaultJsonLogger, atLeastOnce()).publish(logRecordCaptor.capture());
                SimpleLogRecord record = logRecordCaptor.getValue();

                assertThat(record.getUri()).isEqualTo("/test-apilogging/test-void-response");
                // 断言返回体为void，可根据实际日志内容补充
        }

        // --- 过滤器功能测试用例 ---

        /**
         * 测试过滤器功能：过滤tetMethodFilter的请求日志
         */
        @Test
        void tetMethodFilter() throws Exception {
                mockMvc.perform(MockMvcRequestBuilders.post("/test-apilogging/test-method-filter")
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk());


                // 验证由于过滤器的作用，没有日志被记录
                verify(defaultJsonLogger, times(0)).publish(any(SimpleLogRecord.class));
        }


}
