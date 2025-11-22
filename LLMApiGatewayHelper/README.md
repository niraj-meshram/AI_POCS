# LLMApiGatewayHelper

LLMApiGatewayHelper is a Spring Boot service that converts natural language API descriptions into OpenAPI 3.x specifications, validates and repairs supplied specs, and drafts API gateway policy recommendations (Apigee, Spring Cloud Gateway, Kong, …) by delegating to OpenAI’s official Java SDK.

## Features

- **OpenAPI generation** – POST `/api/specs/generate` with a plain-English description and receive structured YAML created by an LLM.
- **Validation & auto-repair** – POST `/api/specs/validate` with existing YAML and optionally let the LLM attempt a self-repair pass when parser errors are detected.
- **Gateway policy drafts** – POST `/api/specs/policies` to receive Markdown-formatted policy suggestions grouped per gateway.
- **Health/status** – GET `/api/status` for lightweight service introspection.

## Prerequisites

- Java 17+
- Gradle (to regenerate the wrapper JAR – see below)
- An OpenAI API key with access to chat-completion models (set `OPENAI_API_KEY`). Optional org/project IDs (`OPENAI_ORG_ID`) are also respected.

## Setup & Running

1. Regenerate the missing wrapper binary once (Gradle cannot download files in this environment):
   ```bash
   gradle wrapper
   # or on Windows
   gradlew.bat wrapper
   ```
2. Export environment variables (or edit `src/main/resources/application.yml`):
   ```bash
   set OPENAI_API_KEY=sk-xxx        # Windows PowerShell
   setx OPENAI_ORG_ID org-123       # optional
   ```
3. Start the service:
   ```bash
   ./gradlew bootRun      # Linux/macOS
   .\gradlew.bat bootRun  # Windows
   ```
4. Hit the endpoints with any HTTP client (examples shown with `curl`):
   ```bash
   curl -X POST http://localhost:8080/api/specs/generate \
        -H "Content-Type: application/json" \
        -d '{"description":"Payments API with capture, refund, and webhooks","domainContext":"PCI compliant card processor","autoRepair":true}'
   ```

## Configuration

Key properties live under the `openai` prefix (see `application.yml`):

| Property | Description | Default |
| --- | --- | --- |
| `openai.api-key` | API key, pulled from `OPENAI_API_KEY` if unset. | `""` |
| `openai.default-model` | Model slug used when requests omit `targetModel`. | `gpt-4o-mini` |
| `openai.temperature` | Sampling temperature. | `0.2` |
| `openai.max-completion-tokens` | Safety ceiling for completion tokens. | `2048` |
| `openai.request-timeout-seconds` | HTTP timeout passed to the SDK. | `45` |

When the SDK is not configured, deterministic fallbacks are returned so developers can continue iterating offline, albeit with simplified stub content.

## Testing

Unit tests live under `src/test/java`. Run them after the wrapper is regenerated:

```bash
./gradlew test
```

> **Note**: Tests and builds were not executed inside this environment because the Gradle wrapper JAR cannot be downloaded here. Generate it locally before running builds.***
