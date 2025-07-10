package io.jenkins.plugins.buildkite;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.Step;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.StepDescriptor;
import org.jenkinsci.plugins.workflow.steps.StepExecution;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Set;
import java.util.logging.Logger;

public class BuildkiteStep extends Step {
    private static final Logger LOGGER = Logger.getLogger(BuildkiteStep.class.getName());

    private final String organization;
    private final String pipeline;
    private final String credentialsId;

    @DataBoundConstructor
    public BuildkiteStep(String organization, String pipeline, String credentialsId) {
        this.organization = organization;
        this.pipeline = pipeline;
        this.credentialsId = credentialsId;
    }

    @Override
    public StepExecution start(StepContext context) throws Exception {
        return new BuildkiteStepExecution(this, context);
    }

    public String getOrganization() {
        return this.organization;
    }

    public String getPipeline() {
        return this.pipeline;
    }

    public String getCredentialsId() {
        return credentialsId;
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return Set.of(TaskListener.class, Run.class);
        }

        @Override
        public String getFunctionName() {
            return "buildkite";
        }
    }
}
