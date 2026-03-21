package com.techpulse.pdfservice.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.techpulse.pdfservice.client.impl.GotenbergRestClient;
import com.techpulse.pdfservice.config.GotenbergConfig;
import com.techpulse.pdfservice.config.WebClientConfig;
import com.techpulse.pdfservice.exception.GotenbergClientException;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

class GotenbergRestClientTest {

    private static WireMockServer wireMock;
    private GotenbergRestClient client;

    private static final byte[] FAKE_PDF = "%PDF-1.4 test".getBytes();

    @BeforeAll
    static void startWireMock() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
    }

    @AfterAll
    static void stopWireMock() {
        wireMock.stop();
    }

    @BeforeEach
    void setup() {
        wireMock.resetAll();
        GotenbergConfig config = new GotenbergConfig();
        config.setBaseUrl("http://localhost:" + wireMock.port());
        config.setTimeoutSeconds(10);

        WebClient webClient = new WebClientConfig().webClient();
        client = new GotenbergRestClient(webClient, config);
    }

    @Test
    @DisplayName("convertHtmlToPdf — calls Chromium route and returns bytes")
    void convertHtmlToPdf_success() {
        wireMock.stubFor(post(urlEqualTo("/forms/chromium/convert/html"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/pdf")
                        .withBody(FAKE_PDF)));

        byte[] result = client.convertHtmlToPdf("<h1>Hello</h1>", "A4", "1cm", "1cm", "1cm", "1cm", false);

        assertThat(result).isEqualTo(FAKE_PDF);
        wireMock.verify(postRequestedFor(urlEqualTo("/forms/chromium/convert/html")));
    }


    @Test
    @DisplayName("convertHtmlToPdf — throws GotenbergClientException on 5xx")
    void convertHtmlToPdf_5xx_throwsException() {
        wireMock.stubFor(post(urlEqualTo("/forms/chromium/convert/html"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        assertThatThrownBy(() -> client.convertHtmlToPdf("<h1>Fail</h1>", "A4", "1cm", "1cm", "1cm", "1cm", false))
                .isInstanceOf(GotenbergClientException.class)
                .hasMessageContaining("5xx");
    }


    @Test
    @DisplayName("convertHtmlToPdf — throws GotenbergClientException on 4xx")
    void convertHtmlToPdf_4xx_throwsException() {
        wireMock.stubFor(post(urlEqualTo("/forms/chromium/convert/html"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Bad Request")));

        assertThatThrownBy(() -> client.convertHtmlToPdf("", "A4", "1cm", "1cm", "1cm", "1cm", false))
                .isInstanceOf(GotenbergClientException.class)
                .hasMessageContaining("4xx");
    }


    @Test
    @DisplayName("convertOfficeToPdf — calls LibreOffice route and returns bytes")
    void convertOfficeToPdf_success() {
        wireMock.stubFor(post(urlEqualTo("/forms/libreoffice/convert"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/pdf")
                        .withBody(FAKE_PDF)));

        byte[] result = client.convertOfficeToPdf("content".getBytes(), "test.docx");

        assertThat(result).isEqualTo(FAKE_PDF);
        wireMock.verify(postRequestedFor(urlEqualTo("/forms/libreoffice/convert")));
    }

    @Test
    @DisplayName("convertOfficeToPdf — throws GotenbergClientException on 4xx")
    void convertOfficeToPdf_4xx_throwsException() {
        wireMock.stubFor(post(urlEqualTo("/forms/libreoffice/convert"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Unsupported format")));

        assertThatThrownBy(() -> client.convertOfficeToPdf("bad".getBytes(), "bad.xyz"))
                .isInstanceOf(GotenbergClientException.class)
                .hasMessageContaining("4xx");
    }

    @Test
    @DisplayName("mergePdfs — calls PDF engines merge route and returns bytes")
    void mergePdfs_success() {
        wireMock.stubFor(post(urlEqualTo("/forms/pdfengines/merge"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/pdf")
                        .withBody(FAKE_PDF)));

        byte[] result = client.mergePdfs(
                List.of(FAKE_PDF, FAKE_PDF),
                List.of("a.pdf", "b.pdf"));

        assertThat(result).isEqualTo(FAKE_PDF);
        wireMock.verify(postRequestedFor(
                urlEqualTo("/forms/pdfengines/merge")));
    }

    @Test
    @DisplayName("mergePdfs — throws GotenbergClientException on 5xx")
    void mergePdfs_5xx_throwsException() {
        wireMock.stubFor(post(urlEqualTo("/forms/pdfengines/merge"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Server error")));

        assertThatThrownBy(() -> client.mergePdfs(
                List.of(FAKE_PDF, FAKE_PDF),
                List.of("a.pdf", "b.pdf")))
                .isInstanceOf(GotenbergClientException.class)
                .hasMessageContaining("5xx");
    }
}

