package io.jenkins.plugins.buildkite.step;

import hudson.model.Run;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BuildkiteStepTest {

    private BuildkiteStep step;

    @Mock private StepContext mockContext;
    @Mock private Run<?, ?> mockRun;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        step = new BuildkiteStep("my-org", "my-pipeline", "creds-id");
    }

    @Test
    void constructor_setsRequiredFields() {
        assertEquals("my-org", step.getOrganization());
        assertEquals("my-pipeline", step.getPipeline());
        assertEquals("creds-id", step.getCredentialsId());
    }

    @Test
    void constructor_setsDefaults() {
        assertEquals("main", step.getBranch());
        assertEquals("HEAD", step.getCommit());
        assertFalse(step.isAsync());
        assertNull(step.getMessage());
    }

    @Test
    void start_withNullMessage_returnsStepExecutionWithDefaultMessage() throws Exception {
        when(mockContext.get(Run.class)).thenReturn(mockRun);
        when(mockRun.getFullDisplayName()).thenReturn("MyJob #123");

        StepExecution result = step.start(mockContext);

        assertNotNull(result);
        assertInstanceOf(BuildkiteStepExecution.class, result);
        assertEquals("Triggered by Jenkins build \"MyJob #123\"", step.getMessage());
        verify(mockContext).get(Run.class);
        verify(mockRun).getFullDisplayName();
    }

    @Test
    void start_withCustomMessage_returnsStepExecutionWithCustomMessage() {
        step.setMessage("Custom message");

        StepExecution result = step.start(mockContext);

        assertNotNull(result);
        assertInstanceOf(BuildkiteStepExecution.class, result);
        assertEquals("Custom message", step.getMessage());
        verifyNoInteractions(mockContext);
    }

    @Test
    void setBranch_updatesValue() {
        step.setBranch("feature-branch");
        assertEquals("feature-branch", step.getBranch());
    }

    @Test
    void setBranch_ignoresNullValue() {
        step.setBranch(null);
        assertEquals("main", step.getBranch());
    }

    @Test
    void setBranch_ignoresEmptyValue() {
        step.setBranch("");
        assertEquals("main", step.getBranch());
    }

    @Test
    void setBranch_ignoresWhitespaceValue() {
        step.setBranch("   ");
        assertEquals("main", step.getBranch());
    }

    @Test
    void setCommit_updatesValue() {
        step.setCommit("abc123");
        assertEquals("abc123", step.getCommit());
    }

    @Test
    void setCommit_ignoresNullValue() {
        step.setCommit(null);
        assertEquals("HEAD", step.getCommit());
    }

    @Test
    void setCommit_ignoresEmptyValue() {
        step.setCommit("");
        assertEquals("HEAD", step.getCommit());
    }

    @Test
    void setCommit_ignoresWhitespaceValue() {
        step.setCommit("   ");
        assertEquals("HEAD", step.getCommit());
    }

    @Test
    void setMessage_updatesValue() {
        step.setMessage("Custom message");
        assertEquals("Custom message", step.getMessage());
    }

    @Test
    void setMessage_ignoresNullValue() {
        step.setMessage("Custom message");
        step.setMessage(null);
        assertEquals("Custom message", step.getMessage());
    }

    @Test
    void setAsync_updatesValue() {
        step.setAsync(true);
        assertTrue(step.isAsync());

        step.setAsync(false);
        assertFalse(step.isAsync());
    }
}
