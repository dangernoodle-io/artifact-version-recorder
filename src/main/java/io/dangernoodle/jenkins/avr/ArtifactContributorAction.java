package io.dangernoodle.jenkins.avr;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributingAction;


@Extension
public class ArtifactContributorAction implements EnvironmentContributingAction
{
    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env)
    {
        ArtifactDataAction storage = build.getAction(ArtifactDataAction.class);
        env.putAll(storage.getEnvironment());
    }

    @Override
    public String getDisplayName()
    {
        return null;
    }

    @Override
    public String getIconFileName()
    {
        return null;
    }

    @Override
    public String getUrlName()
    {
        return null;
    }
}
