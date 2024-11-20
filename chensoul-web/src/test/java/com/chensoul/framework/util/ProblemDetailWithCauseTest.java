package com.chensoul.framework.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import org.junit.jupiter.api.Test;

class ProblemDetailWithCauseTest {

    @Test
    void testProblemDetailWithCauseBuilder() {
        // Create a cause
        ProblemDetailWithCause cause = ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                .withStatus(400)
                .withTitle("Bad Request")
                .withDetail("Invalid input")
                .withType(URI.create("about:blank"))
                .build();

        // Create a main problem detail with a cause
        ProblemDetailWithCause problemDetailWithCause = ProblemDetailWithCause.ProblemDetailWithCauseBuilder.instance()
                .withStatus(500)
                .withTitle("Internal Server Error")
                .withDetail("Something went wrong")
                .withType(URI.create("about:blank"))
                .withCause(cause)
                .build();

        // Verify main problem detail
        assertEquals(500, problemDetailWithCause.getStatus());
        assertEquals("Internal Server Error", problemDetailWithCause.getTitle());
        assertEquals("Something went wrong", problemDetailWithCause.getDetail());
        assertEquals(URI.create("about:blank"), problemDetailWithCause.getType());
        assertNotNull(problemDetailWithCause.getCause());

        // Verify the cause
        assertEquals(400, problemDetailWithCause.getCause().getStatus());
        assertEquals("Bad Request", problemDetailWithCause.getCause().getTitle());
        assertEquals("Invalid input", problemDetailWithCause.getCause().getDetail());
        assertEquals(URI.create("about:blank"), problemDetailWithCause.getCause().getType());
    }
}
