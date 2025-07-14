package io.jenkins.plugins.buildkite.step;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Set;
import java.util.logging.Logger;

public class BuildkiteStep extends Step {
    private static final Logger LOGGER = Logger.getLogger(BuildkiteStep.class.getName());

    private final String organization;
    private final String pipeline;
    private final String credentialsId;
    private String branch;
    private String commit;
    private String message;

    @DataBoundConstructor
    public BuildkiteStep(String organization, String pipeline, String credentialsId) {
        // Required fields
        this.organization = organization;
        this.pipeline = pipeline;
        this.credentialsId = credentialsId;

        // Required fields with defaults
        // If specified in the `buildkite(…)`, they are overridden
        // in the @DataBoundSetter set* methods below.
        this.branch = "main";
        this.commit = "HEAD";
    }

    @Override
    public StepExecution start(StepContext context) {
        if (this.message == null) {
            String fullDisplayName;

            try {
                fullDisplayName = context.get(Run.class).getFullDisplayName();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.message = String.format("Triggered by Jenkins build \"%s\"", fullDisplayName);
        }

        return new BuildkiteStepExecution(this, context);
    }

    public String getOrganization() {
        return organization;
    }

    public String getPipeline() {
        return pipeline;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    public String getBranch() {
        return branch;
    }

    public String getCommit() {
        return commit;
    }

    public String getMessage() {
        return message;
    }

    @DataBoundSetter
    public void setBranch(String branch) {
        if (branch == null || branch.trim().isEmpty()) return;

        this.branch = branch;
    }

    @DataBoundSetter
    public void setCommit(String commit) {
        if (commit == null || commit.trim().isEmpty()) return;

        this.commit = commit;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = message;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(
                    TaskListener.class, // Used in BuildkiteStepExecution.run
                    Run.class // Used in BuildkiteStep.start
            );
        }

        @Override
        public String getFunctionName() {
            return "buildkite";
        }
    }
}
