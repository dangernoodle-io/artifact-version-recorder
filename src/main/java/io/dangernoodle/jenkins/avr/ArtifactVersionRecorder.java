package io.dangernoodle.jenkins.avr;

import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import io.dangernoodle.aether.ResolvedArtifact;
import io.dangernoodle.jenkins.avr.parsers.MavenArtifactVersionParser;
import jenkins.tasks.SimpleBuildStep;


public class ArtifactVersionRecorder extends Recorder implements SimpleBuildStep
{
    @DataBoundConstructor
    public ArtifactVersionRecorder()
    {
    }

    @Override
    public Descriptor getDescriptor()
    {
        return (Descriptor) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService()
    {
        return BuildStepMonitor.NONE;
    }

    @Override
    public void perform(Run<?, ?> build, FilePath workspace, Launcher launcher, TaskListener listener)
    {
        Result result = build.getResult();
        if (result == null || !result.isBetterOrEqualTo(Result.SUCCESS))
        {
            listener.getLogger().println("Build failed, artifact version retrieval skipped");
            return;
        }

        addVersionContributorAction(build);
        ArtifactDataAction storage = createStorageAction(build);

        try
        {
            ResolvedArtifact resolved = workspace.child("pom.xml").act(new MavenArtifactVersionParser());
            listener.getLogger().println(resolved);

            storage.addVersionNumber("RELEASED_VERSION", resolved.getReleased());
            storage.addVersionNumber("SNAPSHOT_VERSION", resolved.getSnapshot());
        }
        catch (Exception e)
        {
            listener.getLogger().println(e);
        }
    }

    private ArtifactContributorAction addVersionContributorAction(Run<?, ?> build)
    {
        ArtifactContributorAction action = build.getAction(ArtifactContributorAction.class);
        if (action == null)
        {
            action = new ArtifactContributorAction();
            build.addAction(action);
        }

        return action;
    }

    private ArtifactDataAction createStorageAction(Run<?, ?> build)
    {
        ArtifactDataAction action = build.getAction(ArtifactDataAction.class);
        if (action == null)
        {
            action = new ArtifactDataAction();
            build.addAction(action);
        }

        return action;
    }

    @Extension
    public static final class Descriptor extends BuildStepDescriptor<Publisher>
    {
//        public Descriptor()
//        {
//            load();
//        }

//        @Override
//        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
//        {
//            req.bindJSON(this, formData);
//            save();
//
//            return super.configure(req, formData);
//        }

        @Override
        public String getDisplayName()
        {
            return "Artifact Version Recorder";
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> aClass)
        {
            return true;
        }
    }
}
