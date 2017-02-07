package io.dangernoodle.jenkins.avr.dsl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.Before;
import org.junit.Test;

import javaposse.jobdsl.plugin.ContextExtensionPoint;


public class ArtifactVersionRecorderJobDslExtensionTest
{
    private ArtifactVersionRecorderJobDslExtension extension;

    private Object object;

    @Before
    public void before()
    {
        extension = new ArtifactVersionRecorderJobDslExtension();
    }

    @Test
    public void testIsContextExtensionPoint()
    {
        // probably not really necessary, but just to be sure...
        assertThat(extension, instanceOf(ContextExtensionPoint.class));
    }

    @Test
    public void testPublisherIsCorrect()
    {
        whenRecordArtifactInvoked();
        thenArtifactVersionRecorderReturned();
    }

    private void thenArtifactVersionRecorderReturned()
    {
        assertThat(object, notNullValue());
    }

    private void whenRecordArtifactInvoked()
    {
        object = extension.recordArtifactVersion();
    }
}
