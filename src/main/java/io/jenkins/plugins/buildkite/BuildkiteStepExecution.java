package io.jenkins.plugins.buildkite;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.workflow.steps.StepContext;
import org.jenkinsci.plugins.workflow.steps.SynchronousNonBlockingStepExecution;

class BuildkiteStepExecution extends SynchronousNonBlockingStepExecution<Void> {
    private transient final BuildkiteStep step;

    protected BuildkiteStepExecution(@NonNull BuildkiteStep step, @NonNull StepContext context) {
        super(context);
        this.step = step;
    }

    @Override
    protected Void run() throws Exception {
        TaskListener listener = getContext().get(TaskListener.class);

        listener.getLogger().println("Hello Buildkite!");
        listener.getLogger().println(String.format("Organization: %s", this.step.getOrganization()));
        listener.getLogger().println(String.format("Pipeline: %s", this.step.getPipeline()));
        listener.getLogger().println(String.format("Credentials Id: %s", this.step.getCredentialsId()));

        return null;
    }
}
