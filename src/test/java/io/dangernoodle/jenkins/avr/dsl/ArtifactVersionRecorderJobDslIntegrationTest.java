package io.dangernoodle.jenkins.avr.dsl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.FreeStyleProject;
import io.dangernoodle.jenkins.avr.ArtifactVersionRecorder;
import javaposse.jobdsl.plugin.ExecuteDslScripts;
import jenkins.model.Jenkins;


public class ArtifactVersionRecorderJobDslIntegrationTest
{
    private static final String JOB_DSL_SCRIPT = "job-dsl/job-dsl.groovy";
    private static final String JOB_NAME = "artifact-version-recorder";

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private FreeStyleProject generated;

    private Jenkins jenkins;

    private FreeStyleProject seedJob;

    @Test
    public void testJobDsl() throws Exception
    {
        givenASeedJob();
        whenBuildSeedJob();
        thenJobWasCreated();
        thenRecorderWasConfigured();
    }

    private ExecuteDslScripts createDslScriptBuilder() throws IOException
    {
        ExecuteDslScripts dslScript = new ExecuteDslScripts();

        dslScript.setUseScriptText(true);
        dslScript.setScriptText(IOUtils.toString(getClass().getClassLoader().getResourceAsStream(JOB_DSL_SCRIPT)));

        return dslScript;
    }

    private void givenASeedJob() throws IOException
    {
        seedJob = jenkinsRule.createFreeStyleProject();
        seedJob.getBuildersList().add(createDslScriptBuilder());
    }

    private void thenJobWasCreated()
    {
        assertThat(jenkins.getJobNames(), hasItem(JOB_NAME));
        generated = jenkins.getItemByFullName(JOB_NAME, FreeStyleProject.class);
    }

    private void thenRecorderWasConfigured()
    {
        assertThat(generated.getPublishersList().size(), is(equalTo(1)));
        assertThat(generated.getPublishersList().get(0), instanceOf(ArtifactVersionRecorder.class));
    }

    private void whenBuildSeedJob() throws Exception
    {
        jenkinsRule.buildAndAssertSuccess(seedJob);
        jenkins = jenkinsRule.getInstance();
    }
}
