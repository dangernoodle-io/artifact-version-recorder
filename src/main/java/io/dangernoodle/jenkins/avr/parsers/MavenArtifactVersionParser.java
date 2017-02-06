package io.dangernoodle.jenkins.avr.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.eclipse.aether.version.Version;

import hudson.remoting.VirtualChannel;
import io.dangernoodle.aether.ResolutionException;
import io.dangernoodle.aether.ResolvedArtifact;
import io.dangernoodle.aether.maven.MavenArtifactResolverFactory;
import io.dangernoodle.aether.maven.MavenArtifactVersionResolver;
import jenkins.MasterToSlaveFileCallable;


public class MavenArtifactVersionParser extends MasterToSlaveFileCallable<ResolvedArtifact>
{
    private static final String m2Repository = System.getProperty("user.home") + "/.m2/repository";

    private static final long serialVersionUID = -1554628113491297031L;

    @Override
    public ResolvedArtifact invoke(File file, VirtualChannel channel) throws IOException, InterruptedException
    {
        try
        {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));

            MavenArtifactResolverFactory factory = new MavenArtifactResolverFactory(m2Repository);
            MavenArtifactVersionResolver resolver = factory.createArtifactVersionResolver();

            // it'd be nice if the `aether` classes were serializable, but they aren't
            ResolvedArtifact version = new ResolvedArtifact();

            version.setGroupId(model.getGroupId());
            version.setArtifactId(model.getArtifactId());

            version.setReleased(resolveLatestRelease(resolver, model));
            version.setSnapshot(resolveSnapshotRelease(resolver, model));

            return version;
        }
        catch (XmlPullParserException e)
        {
            throw new IOException(e);
        }
        catch (ResolutionException e)
        {
            // TODO: handle this better?
            throw new IOException(e);
        }
    }

    private String resolveSnapshotRelease(MavenArtifactVersionResolver resolver, Model model) throws ResolutionException
    {
        Version snapshot = resolver.resolveLatestSnapshot(model.getGroupId(), model.getArtifactId());
        return (snapshot == null) ? null : snapshot.toString();
    }

    private String resolveLatestRelease(MavenArtifactVersionResolver resolver, Model model) throws ResolutionException
    {
        Version released = resolver.resolveLatestRelease(model.getGroupId(), model.getArtifactId());
        return (released == null) ? null : released.toString();
    }
}
