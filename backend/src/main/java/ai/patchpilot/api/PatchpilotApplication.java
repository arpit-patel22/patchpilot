package ai.patchpilot.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class PatchpilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(PatchpilotApplication.class, args);
    }

    /**
     * Explicitly registers an {@link ObjectMapper} bean for use across the app.
     *
     * <p>In Spring Boot 4.x with {@code spring-boot-starter-webmvc}, the
     * {@code ObjectMapper} is no longer auto-configured at the application context
     * level — only inside the web message converter chain. We register one here so
     * services like {@code ClaudeService} can inject it for parsing stored JSON.</p>
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}