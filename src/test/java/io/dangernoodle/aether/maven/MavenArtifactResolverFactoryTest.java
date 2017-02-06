package io.dangernoodle.aether.maven;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.dangernoodle.aether.maven.MavenArtifactResolverFactory;


public class MavenArtifactResolverFactoryTest
{
    private MavenArtifactResolverFactory factory;

    private String localRepository;

    private String remoteRepository;

    @Test
    public void testSetupLocalRepository()
    {
        givenALocalRepository();
        whenSetupResolverFactory();
        thenLocalRepositoryIsConfigured();
        thenRemoteRepositoryIsNull();
    }

    @Test
    public void testSetupRemoteRepository()
    {
        givenALocalRepository();
        givenARemoteRepository();
        whenSetupResolverFactory();
        thenLocalRepositoryIsConfigured();
        thenRemoteRepositoryIsConfigured();
    }

    private void givenALocalRepository()
    {
        localRepository = "/local/path";
    }

    private void givenARemoteRepository()
    {
        remoteRepository = "http://remote";
    }

    private void thenLocalRepositoryIsConfigured()
    {
        assertThat(factory.session.getLocalRepository().getBasedir().toString(), is(equalTo(localRepository)));
    }

    private void thenRemoteRepositoryIsConfigured()
    {
        assertThat(factory.remote, notNullValue());
        assertThat(factory.remote.getUrl(), is(equalTo(remoteRepository)));
        // must be 'default' unless we provide our own
        assertThat(factory.remote.getContentType(), is(equalTo("default")));
    }

    private void thenRemoteRepositoryIsNull()
    {
        assertThat(factory.remote, nullValue());
    }

    private void whenSetupResolverFactory()
    {
        factory = new MavenArtifactResolverFactory(localRepository, remoteRepository);
    }
}
