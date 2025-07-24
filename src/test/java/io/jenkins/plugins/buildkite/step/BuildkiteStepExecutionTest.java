package io.jenkins.plugins.buildkite.step;

import hudson.model.TaskListener;
import io.jenkins.plugins.buildkite.api_client.BuildkiteApiClient;
import io.jenkins.plugins.buildkite.api_client.BuildkiteBuild;
import io.jenkins.plugins.buildkite.api_client.CreateBuildRequest;
import org.jenkinsci.plugins.workflow.steps.FlowInterruptedException;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BuildkiteStepExecutionTest {

    private BuildkiteStep step;

    @Mock private StepContext mockContext;
    @Mock private TaskListener mockListener;
    @Mock private PrintStream mockConsole;

    private BuildkiteStepExecution stepExecution;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        when(mockContext.get(TaskListener.class)).thenReturn(mockListener);
        when(mockListener.getLogger()).thenReturn(mockConsole);

        step = new BuildkiteStep("test-org", "test-pipeline", "test-creds");
        step.setBranch("main");
        step.setCommit("HEAD");
        step.setMessage("Test message");
        step.setAsync(false);

        stepExecution = new BuildkiteStepExecution(step, mockContext);
    }

    @Test
    void constructor_createsInstanceWithStepAndContext() {
        assertNotNull(stepExecution);
        // Constructor functionality is verified by successful creation
    }

    @Test
    void generateCreateBuildRequest_setsAllProperties() throws Exception {
        Method method = BuildkiteStepExecution.class.getDeclaredMethod("generateCreateBuildRequest");
        method.setAccessible(true);

        var request = (CreateBuildRequest) method.invoke(stepExecution);

        assertNotNull(request);
        assertEquals("main", request.getBranch());
        assertEquals("HEAD", request.getCommit());
        assertEquals("Test message", request.getMessage());
    }

    @Test
    void generateCreateBuildRequest_withDifferentStepValues() throws Exception {
        var customStep = new BuildkiteStep("custom-org", "custom-pipeline", "custom-creds");
        customStep.setBranch("feature");
        customStep.setCommit("abc123");
        customStep.setMessage("Custom build message");

        var customExecution = new BuildkiteStepExecution(customStep, mockContext);

        Method method = BuildkiteStepExecution.class.getDeclaredMethod("generateCreateBuildRequest");
        method.setAccessible(true);

        var request = (CreateBuildRequest) method.invoke(customExecution);

        assertEquals("feature", request.getBranch());
        assertEquals("abc123", request.getCommit());
        assertEquals("Custom build message", request.getMessage());
    }

    @Test
    void waitForBuildCompletion_buildPassesImmediately() throws Exception {
        var mockClient = mock(BuildkiteApiClient.class);
        var initialBuild = new BuildkiteBuild().setNumber(123);
        var finishedBuild = new BuildkiteBuild()
                .setNumber(123)
                .setState("passed");

        var testStepExecution = new NoSleepBuildkiteStepExecution(step, mockContext);

        when(mockClient.getBuild("test-org", "test-pipeline", 123))
                .thenReturn(finishedBuild);

        Method method = BuildkiteStepExecution.class.getDeclaredMethod(
                "waitForBuildCompletion",
                BuildkiteApiClient.class,
                BuildkiteBuild.class,
                PrintStream.class
        );
        method.setAccessible(true);

        method.invoke(testStepExecution, mockClient, initialBuild, mockConsole);

        verify(mockConsole).println("Waiting for build to finish");
        verify(mockConsole).println("  passed");
        verify(mockConsole).println("test-org/test-pipeline#123 finished with state: passed");
        verify(mockContext).onSuccess(initialBuild);
        verify(mockContext, never()).onFailure(any());
    }

    @Test
    void waitForBuildCompletion_buildFails() throws Exception {
        var mockClient = mock(BuildkiteApiClient.class);
        var initialBuild = new BuildkiteBuild().setNumber(456);
        var failedBuild = new BuildkiteBuild()
                .setNumber(456)
                .setState("failed");

        var testStepExecution = new NoSleepBuildkiteStepExecution(step, mockContext);

        when(mockClient.getBuild("test-org", "test-pipeline", 456))
                .thenReturn(failedBuild);

        Method method = BuildkiteStepExecution.class.getDeclaredMethod(
                "waitForBuildCompletion",
                BuildkiteApiClient.class,
                BuildkiteBuild.class,
                PrintStream.class
        );
        method.setAccessible(true);

        method.invoke(testStepExecution, mockClient, initialBuild, mockConsole);

        verify(mockConsole).println("Waiting for build to finish");
        verify(mockConsole).println("  failed");
        verify(mockConsole).println("test-org/test-pipeline#456 finished with state: failed");
        verify(mockContext).onFailure(any(FlowInterruptedException.class));
        verify(mockContext, never()).onSuccess(any());
    }

    @Test
    void waitForBuildCompletion_buildRunningThenPasses() throws Exception {
        var mockClient = mock(BuildkiteApiClient.class);
        var initialBuild = new BuildkiteBuild().setNumber(789);

        var testStepExecution = new NoSleepBuildkiteStepExecution(step, mockContext);

        var runningBuild = new BuildkiteBuild()
                .setNumber(789)
                .setState("running");
        var passedBuild = new BuildkiteBuild()
                .setNumber(789)
                .setState("passed");

        when(mockClient.getBuild("test-org", "test-pipeline", 789))
                .thenReturn(runningBuild)
                .thenReturn(passedBuild);

        Method method = BuildkiteStepExecution.class.getDeclaredMethod(
                "waitForBuildCompletion",
                BuildkiteApiClient.class,
                BuildkiteBuild.class,
                PrintStream.class
        );

        method.setAccessible(true);

        method.invoke(testStepExecution, mockClient, initialBuild, mockConsole);

        verify(mockConsole).println("Waiting for build to finish");
        verify(mockConsole).println("  running");
        verify(mockConsole).println("  passed");
        verify(mockConsole).println("test-org/test-pipeline#789 finished with state: passed");
        verify(mockContext).onSuccess(initialBuild);
    }

    @Test
    void printCreatingBuild_outputsCorrectMessage() throws Exception {
        Method method = BuildkiteStepExecution.class.getDeclaredMethod(
                "printCreatingBuild",
                PrintStream.class
        );
        method.setAccessible(true);

        method.invoke(stepExecution, mockConsole);

        verify(mockConsole).println("Creating build for test-org/test-pipeline on main (HEAD)");
    }

    @Test
    void printBuildCreated_outputsCorrectMessage() throws Exception {
        Method method = BuildkiteStepExecution.class.getDeclaredMethod(
                "printBuildCreated",
                BuildkiteBuild.class,
                PrintStream.class
        );
        method.setAccessible(true);

        var build = new BuildkiteBuild()
                .setNumber(123)
                .setWebUrl("https://buildkite.com/test-org/test-pipeline/builds/123");

        method.invoke(stepExecution, build, mockConsole);

        verify(mockConsole).println(
                "test-org/test-pipeline#123 created: https://buildkite.com/test-org/test-pipeline/builds/123"
        );
    }

    @Test
    void printBuildFinished_outputsCorrectMessage() throws Exception {
        Method method = BuildkiteStepExecution.class.getDeclaredMethod(
                "printBuildFinished",
                BuildkiteBuild.class,
                PrintStream.class
        );
        method.setAccessible(true);

        var build = new BuildkiteBuild()
                .setNumber(456)
                .setState("passed");

        method.invoke(stepExecution, build, mockConsole);

        verify(mockConsole).println(
                "test-org/test-pipeline#456 finished with state: passed"
        );
    }

    private static class NoSleepBuildkiteStepExecution extends BuildkiteStepExecution {
        public NoSleepBuildkiteStepExecution(BuildkiteStep step, StepContext context) {
            super(step, context);
        }

        @Override
        protected void sleepMillis(long millis) {
            // Don't sleep in tests
        }
    }
}
