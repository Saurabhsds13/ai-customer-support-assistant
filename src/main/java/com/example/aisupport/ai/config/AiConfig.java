package com.example.aisupport.ai.config;

import org.springframework.context.annotation.Configuration;

/**
 * AI configuration class.
 * <p>
 * Spring AI auto-configures the ChatModel bean based on the starter
 * on the classpath (e.g., spring-ai-openai-spring-boot-starter).
 * Provider-specific settings (API key, model name, temperature) are
 * externalized in application.yml under spring.ai.openai.*.
 * <p>
 * To switch providers (e.g., from OpenAI to Anthropic):
 * 1. Replace the Maven starter dependency
 * 2. Update the spring.ai.* properties in application.yml
 * 3. No Java code changes required — the ChatModel interface stays the same
 */
@Configuration
public class AiConfig {
    // Spring AI auto-configuration handles ChatModel bean creation.
    // This class exists as a placeholder for future AI-specific beans
    // (e.g., custom retry templates, rate limiters, circuit breakers).
}
