
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.logging.LogManager;

import io.dangernoodle.aether.maven.MavenArtifactResolverFactory;
import io.dangernoodle.aether.maven.MavenArtifactVersionResolver;


public class SimpleMavenVersionResolver
{
    static
    {
        InputStream inputStream = SimpleMavenVersionResolver.class.getResourceAsStream("/logging.properties");
        try
        {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) throws Exception
    {
        String groupId = requireNonNull(args[0], "please specify a groupId");
        String artifactId = requireNonNull(args[1], "please specify an artifactId");

        String remoteRepository = (args.length == 3) ? args[2] : null;

        MavenArtifactResolverFactory factory = new MavenArtifactResolverFactory("target/local-repo", remoteRepository);
        MavenArtifactVersionResolver resolver = factory.createArtifactVersionResolver();

        System.out.println("resolved version: " + resolver.resolveLatestRelease(groupId, artifactId));
    }
}
