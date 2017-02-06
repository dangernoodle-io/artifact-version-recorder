package io.dangernoodle.aether.maven;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RemoteRepository.Builder;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResolutionException;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;
import org.eclipse.aether.version.Version;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.dangernoodle.aether.ResolutionException;
import io.dangernoodle.aether.maven.MavenArtifactVersionResolver;


public class MavenArtifactVersionResolverTest
{
    private static final GenericVersionScheme scheme = new GenericVersionScheme();

    private Version actualVersion;

    private ArgumentCaptor<VersionRangeRequest> captor;

    private String expectedVersion;

    @Mock
    private Exception mockException;

    private RemoteRepository mockRemoteRepository;

    @Mock
    private RepositorySystemSession mockSession;

    @Mock
    private RepositorySystem mockSystem;

    private MavenArtifactVersionResolver resolver;

    private List<String> testVersions;

    private Exception thrownException;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);

        mockRemoteRepository = new Builder("", "", "").build();
        captor = ArgumentCaptor.forClass(VersionRangeRequest.class);

        resolver = new MavenArtifactVersionResolver(mockSystem, mockSession, mockRemoteRepository);
    }

    @Test
    public void testResolveLatestRelease() throws Exception
    {
        givenMixedVersions();
        givenALatestReleaseVersion();
        givenAVersionRangeRequest();
        whenResolveLatestRelease();
        thenCorrectVersionIsResolved();
    }

    @Test
    public void testResolveLatestRelease_snapshotValuesOnly() throws Exception
    {
        givenSnapshotValuesOnly();
        givenAVersionRangeRequest();
        whenResolveLatestRelease();
        thenNoVersionIsResolved();
    }

    @Test
    public void testResolveLatestRelease_versionRangeParseError() throws Exception
    {
        givenMixedVersions();
        givenAVersionRangeError();
        whenResolveLatestRelease();
        thenVersionRangeErrorIsThrown();
    }

    @Test
    public void testResolveLatestRelease_versionRangeResolutionError() throws Exception
    {
        givenMixedVersions();
        givenAVersionParseError();
        whenResolveLatestRelease();
        thenVersionParseErrorIsThrown();
    }

    @Test
    public void testResolveLatestSnapshot() throws Exception
    {
        givenMixedVersions();
        givenALatestSnapshotVersion();
        givenAVersionRangeRequest();
        whenResolveLatestSnapshot();
        thenCorrectVersionIsResolved();
    }

    @Test
    public void testResolveLatestSnapshot_localMetadataNotFound() throws Exception
    {
        givenMixedVersions();
        givenALatestSnapshotVersion();
        givenLocalMetadataNotFound();
        givenAVersionRangeRequest();
        whenResolveLatestSnapshot();
        thenCorrectVersionIsResolved();
    }


    @Test
    public void testResolveLatestSnapshot_releaseValuesOnly() throws Exception
    {
        givenReleaseValuesOnly();
        givenAVersionRangeRequest();
        whenResolveLatestSnapshot();
        thenNoVersionIsResolved();
    }

    @Test
    public void testResolveLatestSnapshot_versionRangeResolutionError() throws Exception
    {
        givenMixedVersions();
        givenAVersionParseError();
        whenResolveLatestSnapshot();
        thenVersionParseErrorIsThrown();
    }

    @Test
    public void testResolveReleaseSnapshot_localMetadataNotFound() throws Exception
    {
        givenMixedVersions();
        givenALatestReleaseVersion();
        givenLocalMetadataNotFound();
        givenAVersionRangeRequest();
        whenResolveLatestRelease();
        thenCorrectVersionIsResolved();
    }

    @Test
    public void testResolveSnapshotRelease_versionRangeParseError() throws Exception
    {
        givenMixedVersions();
        givenAVersionRangeError();
        whenResolveLatestSnapshot();
        thenVersionRangeErrorIsThrown();
    }

    private List<Version> createVersions(List<String> toCreate) throws InvalidVersionSpecificationException
    {
        List<Version> versions = new ArrayList<>(toCreate.size());
        for (String version : toCreate)
        {
            versions.add(scheme.parseVersion(version));
        }

        return versions;
    }

    private List<String> getReleaseVersions()
    {
        return Arrays.asList("1.0.0", "0.4.0", "1.2.3", "0.1.0", "0.1.4");
    }

    private List<String> getSnapshotVersions()
    {
        return Arrays.asList("1.0.0-SNAPSHOT", "0.4.0-SNAPSHOT", "1.2.2-SNAPSHOT", "1.5.0-SNAPSHOT", "1.2.3-SNAPSHOT");
    }

    private void givenALatestReleaseVersion()
    {
        expectedVersion = "1.2.3";
    }

    private void givenALatestSnapshotVersion()
    {
        expectedVersion = "1.5.0-SNAPSHOT";
    }

    private void givenAVersionParseError() throws VersionRangeResolutionException
    {
        VersionRangeResolutionException exception = mock(VersionRangeResolutionException.class);
        when(mockSystem.resolveVersionRange(eq(mockSession), any())).thenThrow(exception);
    }

    private void givenAVersionRangeError() throws VersionRangeResolutionException
    {
        captor = ArgumentCaptor.forClass(VersionRangeRequest.class);

        VersionRangeResult result = new VersionRangeResult(new VersionRangeRequest());
        result.addException(mockException);

        when(mockSystem.resolveVersionRange(eq(mockSession), any())).thenReturn(result);
    }

    private void givenAVersionRangeRequest() throws VersionRangeResolutionException, InvalidVersionSpecificationException
    {
        captor = ArgumentCaptor.forClass(VersionRangeRequest.class);

        VersionRangeResult result = new VersionRangeResult(new VersionRangeRequest());
        result.setVersions(createVersions(testVersions));

        when(mockSystem.resolveVersionRange(eq(mockSession), any())).thenReturn(result);
    }

    private void givenLocalMetadataNotFound() throws VersionRangeResolutionException, InvalidVersionSpecificationException
    {
        VersionRangeResult result = new VersionRangeResult(new VersionRangeRequest());
        result.setVersions(createVersions(testVersions));

        result.addException(mock(MetadataNotFoundException.class));

        when(mockSystem.resolveVersionRange(eq(mockSession), any())).thenReturn(result);
    }

    private void givenMixedVersions()
    {
        List<String> mixed = new ArrayList<>();
        mixed.addAll(getReleaseVersions());
        mixed.addAll(getSnapshotVersions());

        testVersions = mixed;
    }

    private void givenReleaseValuesOnly()
    {
        testVersions = getReleaseVersions();
    }

    private void givenSnapshotValuesOnly()
    {
        testVersions = getSnapshotVersions();
    }

    private void thenCorrectVersionIsResolved() throws VersionRangeResolutionException
    {
        assertThat(actualVersion.toString(), is(equalTo(expectedVersion)));
        verify(mockSystem).resolveVersionRange(eq(mockSession), captor.capture());

        VersionRangeRequest request = captor.getValue();

        assertThat(request.getRepositories().size(), is(equalTo(1)));
        assertThat(request.getRepositories().get(0), is(equalTo(mockRemoteRepository)));

        Artifact artifact = request.getArtifact();
        assertThat("groupId", is(equalTo(artifact.getGroupId())));
        assertThat("artifactId", is(equalTo(artifact.getArtifactId())));
    }

    private void thenNoVersionIsResolved()
    {
        assertThat(actualVersion, nullValue());
    }

    private void thenVersionParseErrorIsThrown()
    {
        assertThat(thrownException, instanceOf(ResolutionException.class));
        ResolutionException exception = (ResolutionException) thrownException;

        assertThat(exception.getExceptions().size(), is(equalTo(1)));
        assertThat(exception.getExceptions().iterator().next(), instanceOf(VersionRangeResolutionException.class));
    }

    private void thenVersionRangeErrorIsThrown()
    {
        assertThat(thrownException, instanceOf(ResolutionException.class));
        ResolutionException exception = (ResolutionException) thrownException;

        assertThat(exception.getExceptions().size(), is(equalTo(1)));
        assertThat(exception.getExceptions().iterator().next(), is(equalTo(mockException)));
    }

    private void whenResolveLatestRelease()
    {
        try
        {
            actualVersion = resolver.resolveLatestRelease("groupId", "artifactId");
        }
        catch (Exception e)
        {
            thrownException = e;
        }
    }

    private void whenResolveLatestSnapshot()
    {
        try
        {
            actualVersion = resolver.resolveLatestSnapshot("groupId", "artifactId");
        }
        catch (Exception e)
        {
            thrownException = e;
        }
    }
}
