package io.dangernoodle.aether.maven;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.version.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dangernoodle.aether.ResolutionException;


public class MavenArtifactVersionResolver
{
    private static final Logger logger = LoggerFactory.getLogger(MavenArtifactVersionResolver.class);

    private final RemoteRepository remote;

    private final RepositorySystemSession session;

    private final RepositorySystem system;

    MavenArtifactVersionResolver(RepositorySystem system, RepositorySystemSession session, RemoteRepository remote)
    {
        this.system = system;
        this.session = session;
        this.remote = remote;
    }

    public Version resolveLatestRelease(String groupId, String artifactId) throws ResolutionException
    {
        logger.debug("resolving latest release for [{}:{}]", groupId, artifactId);
        return resolve(groupId, artifactId, version -> !isSnapshot(version));
    }

    public Version resolveLatestSnapshot(String groupId, String artifactId) throws ResolutionException
    {
        logger.debug("resolving latest snapshot for [{}:{}]", groupId, artifactId);
        return resolve(groupId, artifactId, version -> isSnapshot(version));
    }

    private DefaultArtifact createArtifact(String groupId, String artifactId)
    {
        return new DefaultArtifact(groupId, artifactId, null, "[0,)");
    }

    private VersionRangeRequest createVersionRangeRequest(String groupId, String artifactId)
    {
        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact(createArtifact(groupId, artifactId));
        rangeRequest.addRepository(remote);

        return rangeRequest;
    }

    private boolean filterException(Exception e)
    {
        return (e instanceof MetadataNotFoundException);
    }

    private List<Exception> filterExceptions(List<Exception> exceptions)
    {
        return exceptions.stream()
                         .map(this::logException)
                         .filter(e -> !filterException(e))
                         .collect(Collectors.toList());
    }

    private Version find(List<Version> versions, Predicate<Version> filter)
    {
        return versions.stream()
                       .filter(filter::test)
                       .sorted(Version::compareTo)
                       .reduce((v1, v2) -> v2)
                       .orElse(null);
    }

    private boolean isSnapshot(Version version)
    {
        return version.toString().endsWith("-SNAPSHOT");
    }

    private Exception logException(Exception e)
    {
        logger.debug("{} - {}", e.getClass().getName(), e.getMessage());
        return e;
    }

    private Version resolve(String groupId, String artifactId, Predicate<Version> filter) throws ResolutionException
    {
        VersionRangeRequest rangeRequest = createVersionRangeRequest(groupId, artifactId);
        try
        {
            VersionRangeResult rangeResult = system.resolveVersionRange(session, rangeRequest);
            List<Exception> exceptions = filterExceptions(rangeResult.getExceptions());

            if (!exceptions.isEmpty())
            {
                throw new ResolutionException(exceptions);
            }

            return find(rangeResult.getVersions(), filter);

        }
        catch (VersionRangeResolutionException e)
        {
            throw new ResolutionException(e);
        }
    }
}
