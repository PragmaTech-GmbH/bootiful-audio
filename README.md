# Bootiful Audio: Speech-to-Text Demo

A demonstration application showcasing Spring AI integration with OpenAI's Whisper model for speech-to-text transcription, built as a native GraalVM application with Vaadin frontend.

## Educational Purpose

This repository serves as an educational resource for developers looking to understand:

1. **Spring AI Integration** - How to leverage Spring AI to connect with OpenAI services
2. **GraalVM Native Images** - Building efficient, fast-starting native applications
3. **Vaadin Web Applications** - Creating interactive web UIs with Java
4. **Browser Audio APIs** - Capturing and processing audio in a browser environment
5. **Spring Security** - Implementing authentication in web applications
6. **DevOps Automation** - GitHub Actions and Fly.io deployment integration

## Features

- Speech recording directly in the browser
- Real-time audio transcription using OpenAI's Whisper model
- Secure authentication flow
- Responsive UI built with Vaadin
- Optimized for performance with GraalVM native compilation

## Technology Stack

- **Spring Boot 3** - Application framework
- **Spring AI** - AI service integration
- **GraalVM** - Native image compilation
- **Vaadin** - Web UI framework
- **OpenAI Whisper** - Audio transcription model
- **GitHub Actions** - CI/CD pipeline
- **Fly.io** - Cloud deployment

## Getting Started

### Prerequisites

- Java 24
- Maven
- OpenAI API key

### Environment Variables

The application requires the following environment variables:

```
SPRING_AI_OPENAI_API_KEY=your_openai_api_key
APP_USER=your_username
APP_PASSWORD=your_password
```

### Running Locally

1. Clone the repository
2. Set up environment variables
3. Run the application:

```bash
./mvnw spring-boot:run
```

For native image compilation:

```bash
./mvnw -Pnative,production -B spring-boot:build-image
```

## Architecture

The application follows a simple architecture:

1. **Frontend**: Vaadin-based UI that captures audio using browser APIs
2. **Controller/Service Layer**: Processes captured audio and communicates with OpenAI
3. **Integration Layer**: Spring AI integration with OpenAI Whisper API

## Deployment

The application is configured for deployment to Fly.io using GitHub Actions. The workflow is defined in `.github/workflows/build-deploy.yml`.

## Contributing

This project is for educational purposes. Feel free to fork and experiment with the codebase to learn more about the technologies involved.
