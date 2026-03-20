<div align="center">
  
# PDF Generator 📄

> Spring Boot REST API to convert HTML, Markdown, PPT, Excel and Docs to PDF using Gotenberg - stores on AWS S3 and returns download URL.

<br>

<img src="https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/Gotenberg-8-0052CC?style=for-the-badge&logo=googlechrome&logoColor=white"/>
<img src="https://img.shields.io/badge/Amazon_S3-Storage-FF9900?style=for-the-badge&logo=amazons3&logoColor=white"/>
<img src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white"/>
<img src="https://img.shields.io/badge/Kubernetes-Ready-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white"/>
<img src="https://img.shields.io/badge/Maven-3.8-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white"/>
<img src="https://img.shields.io/badge/WebFlux-WebClient-6DB33F?style=for-the-badge&logo=spring&logoColor=white"/>
<img src="https://img.shields.io/badge/REST-API-0052CC?style=for-the-badge&logo=postman&logoColor=white"/>
<img src="https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge"/>

</div>

---

## Table of contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Tech stack](#tech-stack)
- [Features](#features)
- [Project structure](#project-structure)
- [Getting started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local dev with Docker Compose](#local-dev-with-docker-compose)
  - [Environment variables](#environment-variables)
- [API reference](#api-reference)
- [How the byte[] flow works](#how-the-byte-flow-works)
- [S3 storage](#s3-storage)
- [Running tests](#running-tests)
- [Contributing](#contributing)

---

## Overview

The UI sends a file as a **byte array** in a JSON request body. The backend converts it to PDF using [Gotenberg](https://gotenberg.dev), uploads the result to **AWS S3**, and returns a **presigned download URL** - no temp files, no file system, fully stateless.
 
```
UI (byte[]) → POST /api/v1/pdf/{type} → Gotenberg → S3 → presigned URL → UI
```

---

## Architecture
```
            ┌─────────────────────────────────────────────────────────┐
            │                      UI (React)                         │
            │         File → byte[] → POST /api/v1/pdf/{type}         │
            └────────────────────────┬────────────────────────────────┘
                                     │
                                     │ JSON{fileBytes[], fileName, type}
                                     ▼
            ┌─────────────────────────────────────────────────────────┐
            │                   PdfController                         │
            │         Single controller · 6 endpoints                 │
            └────────────────────────┬────────────────────────────────┘
                                     │
                                     │
                                     ▼
            ┌──────────────────────────────────────────────────────────┐
            │                   PdfServiceImpl                         │ 
            │   orchestrates conversion + storage in one transaction   │
            ├──────────────────────┬───────────────────────────────────┤
            │   GotenbergClient    │       S3StorageServiceImpl        │
            │   (WebClient POST)   │       (AmazonS3 putObject)        │
            │  /chromium/html      │   bucket: your-pdf-bucket         │
            │  /libreoffice        │   key:    pdf/{type}/{uuid}.pdf   │
            │  /pdfengines/merge   │   returns: presigned URL (60 min) │
            └──────────────────────┴───────────────────────────────────┘
```

---

## Tech stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2, Java 17 |
| HTTP client | Spring WebFlux WebClient (non-blocking) |
| PDF engine | [Gotenberg 8](https://gotenberg.dev) (Docker) |
| Markdown parser | Flexmark 0.64 |
| Cloud storage | AWS S3 (SDK v1) |
| Local S3 dev | LocalStack 3 |
| Build | Maven |
| Tests | JUnit 5, Mockito, WireMock |
| Containerisation | Docker, Docker Compose |

---

## Features

| # | Conversion | Gotenberg route |
|---|---|---|
| 1 | HTML → PDF | `/forms/chromium/convert/html` |
| 2 | Markdown → PDF | md → HTML (Flexmark) → `/forms/chromium/convert/html` |
| 3 | PPT → PDF | `/forms/libreoffice/convert` |
| 4 | Excel → PDF | `/forms/libreoffice/convert` |
| 5 | Docs (Word) → PDF | `/forms/libreoffice/convert` |
| 6 | Merge PDFs | `/forms/pdfengines/merge` |

All conversions follow the same pattern:
1. Receive `byte[]` from UI via JSON body
2. Convert via Gotenberg
3. Upload PDF to S3
4. Return presigned URL (default 60-minute expiry)

---

## Project structure

```
pdf-generator/
├── pom.xml
├── docker-compose.yml
├── Dockerfile
├── init-s3.sh                          # LocalStack S3 bucket init
└── src/
    ├── main/java/com/yourcompany/pdfservice/
    │   ├── controller/
    │   │   └── PdfController.java           # single controller, 6 endpoints
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── PdfByteRequest.java       # byte[], fileName, type
    │   │   │   └── MergePdfByteRequest.java  # List<byte[]>, List<fileName>
    │   │   └── response/
    │   │       ├── PdfUploadResponse.java    # pdfUrl, s3Key, sizeBytes
    │   │       └── ApiResponse.java          # generic wrapper<T>
    │   ├── service/
    │   │   ├── PdfService.java               # interface
    │   │   ├── StorageService.java           # interface
    │   │   └── impl/
    │   │       ├── PdfServiceImpl.java       # orchestrates all 6 conversions
    │   │       └── S3StorageServiceImpl.java # upload + presigned URL
    │   ├── client/
    │   │   ├── GotenbergClient.java          # interface
    │   │   └── impl/
    │   │       └── GotenbergRestClient.java  # WebClient to Gotenberg
    │   ├── config/
    │   │   ├── GotenbergConfig.java
    │   │   ├── S3Config.java
    │   │   └── WebClientConfig.java
    │   ├── exception/
    │   │   ├── PdfConversionException.java
    │   │   ├── StorageException.java
    │   │   ├── GotenbergClientException.java
    │   │   └── GlobalExceptionHandler.java
    │   ├── util/
    │   │   ├── MarkdownConverter.java        # Flexmark md → HTML
    │   │   ├── FileTypeValidator.java
    │   │   └── S3KeyGenerator.java           # pdf/{type}/{uuid}-{name}.pdf
    │   └── PdfGenerationServiceApplication.java
    └── main/resources/
        ├── application.yml
        ├── application-dev.yml               # LocalStack config
        └── application-prod.yml              # real AWS config
```

---

## Getting started

### Prerequisites

- Java 17+
- Maven 3.8+
- Docker + Docker Compose

### Local dev with Docker Compose

```bash
# 1. Clone
git clone https://github.com/gajendra-ingle/pdf-byte-forge.git
cd pdf-byte-forge

# 2. Start Gotenberg + LocalStack S3
docker-compose up -d

# 3. Run the app (uses application-dev.yml with LocalStack)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The app starts on `http://localhost:8080`.
Gotenberg runs on `http://localhost:3000`.
LocalStack S3 runs on `http://localhost:4566`.

### Environment variables

| Variable | Description | Default |
|---|---|---|
| `AWS_ACCESS_KEY` | AWS access key | `test` (LocalStack) |
| `AWS_SECRET_KEY` | AWS secret key | `test` (LocalStack) |
| `AWS_REGION` | AWS region | `ap-south-1` |
| `S3_BUCKET_NAME` | S3 bucket name | `your-pdf-bucket` |
| `GOTENBERG_BASE_URL` | Gotenberg base URL | `http://localhost:3000` |

For production, set these as environment variables or in `application-prod.yml`.

---

## API reference

Base URL: `http://localhost:8080/api/v1/pdf`

All endpoints accept `Content-Type: application/json` and return:

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "pdfUrl": "https://s3.amazonaws.com/your-bucket/pdf/html/uuid-report.pdf?...",
    "s3Key": "pdf/html/a3f2c1-report.pdf",
    "fileName": "report.pdf",
    "sizeBytes": 102400,
    "contentType": "application/pdf"
  },
  "timestamp": "2025-03-20T10:30:00"
}
```

### POST `/html`

Convert raw HTML bytes to PDF.

```json
{
  "fileBytes": [60, 104, 49, 62, ...],
  "fileName": "invoice.html",
  "type": "HTML",
  "pageSize": "A4",
  "landscape": false
}
```

### POST `/markdown`

Convert Markdown bytes to PDF. Optionally inject custom CSS.

```json
{
  "fileBytes": [35, 32, 72, 101, ...],
  "fileName": "report.md",
  "type": "MARKDOWN",
  "cssContent": "body { font-family: Arial; }"
}
```

### POST `/ppt`

Convert PowerPoint file bytes to PDF.

```json
{
  "fileBytes": [...],
  "fileName": "presentation.pptx",
  "type": "PPT"
}
```

### POST `/excel`

Convert Excel file bytes to PDF.

```json
{
  "fileBytes": [...],
  "fileName": "data.xlsx",
  "type": "EXCEL"
}
```

### POST `/docs`

Convert Word document bytes to PDF.

```json
{
  "fileBytes": [...],
  "fileName": "contract.docx",
  "type": "DOCS"
}
```

### POST `/merge`

Merge multiple PDF byte arrays into one PDF.

```json
{
  "filesBytes": [[37, 80, 68, ...], [37, 80, 68, ...]],
  "fileNames": ["part1.pdf", "part2.pdf"],
  "mergedFileName": "final-report.pdf"
}
```

---

## How the byte[] flow works

The UI converts a `File` object to a byte array and sends it as JSON:

```javascript
const file = event.target.files[0];
const reader = new FileReader();

reader.onload = async () => {
  const base64   = reader.result.split(',')[1];
  const byteArray = Array.from(atob(base64), c => c.charCodeAt(0));

  const res = await fetch('/api/v1/pdf/html', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      fileBytes: byteArray,
      fileName: file.name,
      type: 'HTML',
      pageSize: 'A4'
    })
  });

  const { data } = await res.json();
  window.open(data.pdfUrl); // open presigned S3 URL
};

reader.readAsDataURL(file);
```

Spring Boot automatically deserializes `byte[]` from a JSON integer array — no multipart, no base64 decoding needed on the backend.

---

## S3 storage

PDFs are stored using a deterministic key pattern:

```
pdf/{type}/{uuid}-{sanitized-filename}.pdf
```

Examples:
```
pdf/html/3f2a1c9d-invoice.pdf
pdf/markdown/8b4e2f1a-readme.pdf
pdf/ppt/c1d2e3f4-q3-deck.pdf
pdf/merge/a9b8c7d6-final-report.pdf
```

The service returns a **presigned URL** valid for 60 minutes (configurable via `aws.s3.presign-expiration-minutes`). For long-lived public URLs, set the bucket policy to public-read and return `amazonS3.getUrl(bucket, key).toString()` instead.

---

## Running tests

```bash
# All tests
mvn test

# Only unit tests (no Docker required)
mvn test -Dgroups="unit"

# Only integration tests (requires Docker)
mvn test -Dgroups="integration"
```

Test coverage:

| Test class | Type | What it covers |
|---|---|---|
| `PdfControllerTest` | `@WebMvcTest` | All 6 endpoints, validation, response shape |
| `PdfServiceImplTest` | Mockito | All 6 service methods, error wrapping |
| `GotenbergRestClientTest` | WireMock | All 3 Gotenberg HTTP routes, 4xx/5xx handling |
| `MarkdownConverterTest` | Unit | md → HTML, custom CSS, document structure |
| `FileTypeValidatorTest` | Unit | Valid/invalid extensions, empty file |

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature`
5. Open a Pull Request

Please follow the existing code style and add tests for any new conversion type or storage provider.

