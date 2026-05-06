package ai.patchpilot.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClaudeServiceTest {

    private ClaudeService service;

    @BeforeEach
    void setUp() {
        // Construct with a dummy key — no HTTP calls are made in these tests
        service = new ClaudeService("dummy-key", new ObjectMapper());
    }

    @Test
    void stripsJsonFenceWithLanguageTag() {
        String input = "```json\n{\"tryThisFirst\":\"reboot\",\"causes\":[]}\n```";
        String result = service.extractJson(input);
        assertEquals("{\"tryThisFirst\":\"reboot\",\"causes\":[]}", result);
    }

    @Test
    void stripsPlainFenceWithNoLanguageTag() {
        String input = "```\n{\"tryThisFirst\":\"reboot\",\"causes\":[]}\n```";
        String result = service.extractJson(input);
        assertEquals("{\"tryThisFirst\":\"reboot\",\"causes\":[]}", result);
    }

    @Test
    void passesThroughUnfencedJson() {
        String input = "{\"tryThisFirst\":\"reboot\",\"causes\":[]}";
        String result = service.extractJson(input);
        assertEquals(input, result);
    }

    @Test
    void trimsLeadingAndTrailingWhitespace() {
        String input = "   \n  {\"tryThisFirst\":\"reboot\",\"causes\":[]}  \n  ";
        String result = service.extractJson(input);
        assertEquals("{\"tryThisFirst\":\"reboot\",\"causes\":[]}", result);
    }

    @Test
    void trimsWhitespaceInsideFences() {
        String input = "  ```json  \n  {\"key\":\"value\"}  \n  ```  ";
        String result = service.extractJson(input);
        assertEquals("{\"key\":\"value\"}", result);
    }

    @Test
    void handlesNullInput() {
        assertNull(service.extractJson(null));
    }

    @Test
    void handlesEmptyStringInput() {
        assertEquals("", service.extractJson(""));
    }

    @Test
    void handlesFenceCaseInsensitive() {
        String input = "```JSON\n{\"key\":\"value\"}\n```";
        String result = service.extractJson(input);
        assertEquals("{\"key\":\"value\"}", result);
    }
}