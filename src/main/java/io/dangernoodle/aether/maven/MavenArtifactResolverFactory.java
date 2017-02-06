package io.dangernoodle.aether.maven;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;


public class MavenArtifactResolverFactory
{
    // visible for testing
    final RemoteRepository remote;

    // visible for testing
    final RepositorySystemSession session;

    // visible for testing
    final RepositorySystem system;

    public MavenArtifactResolverFactory(String localRepository)
    {
        this(localRepository, null);
    }

    public MavenArtifactResolverFactory(String localRepository, String remoteRepository)
    {
        this.system = createRepositorySystem();

        this.remote = createRemoteRepository(remoteRepository);
        this.session = createRepositorySession(localRepository);
    }

    public MavenArtifactVersionResolver createArtifactVersionResolver()
    {
        return new MavenArtifactVersionResolver(system, session, remote);
    }

    private RemoteRepository createRemoteRepository(String remoteRepository)
    {
        if (remoteRepository == null)
        {
            return null;
        }

        return new Builder("nexus", "default", remoteRepository).build();
    }

    private RepositorySystemSession createRepositorySession(String basedir)
    {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        // if the 'RemoteRepository' is configured, this will force it to always be checked
        session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);

        LocalRepository local = new LocalRepository(basedir);
        LocalRepositoryManager localManager = system.newLocalRepositoryManager(session, local);

        session.setLocalRepositoryManager(localManager);

        return session;
    }

    private RepositorySystem createRepositorySystem()
    {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

        return locator.getService(RepositorySystem.class);
    }
}
