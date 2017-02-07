package io.dangernoodle.jenkins.avr.dsl;

import hudson.Extension;
import io.dangernoodle.jenkins.avr.ArtifactVersionRecorder;
import javaposse.jobdsl.dsl.helpers.publisher.PublisherContext;
import javaposse.jobdsl.plugin.ContextExtensionPoint;
import javaposse.jobdsl.plugin.DslExtensionMethod;


@Extension(optional = true)
public class ArtifactVersionRecorderJobDslExtension extends ContextExtensionPoint
{
    @DslExtensionMethod(context = PublisherContext.class)
    public Object recordArtifactVersion()
    {
        return new ArtifactVersionRecorder();
    }
}
