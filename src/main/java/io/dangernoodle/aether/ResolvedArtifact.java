package io.dangernoodle.aether;

import java.io.Serializable;

import com.google.common.base.Optional;


public class ResolvedArtifact implements Serializable
{
    private static final long serialVersionUID = -589322241076460473L;

    private String artifactId;

    private String groupId;

    private String released;

    private String snapshot;

    public String getArtifactId()
    {
        return valueOrBlank(artifactId);
    }

    public String getGroupId()
    {
        return valueOrBlank(groupId);
    }

    public String getReleased()
    {
        return valueOrBlank(released);
    }

    public String getSnapshot()
    {
        return valueOrBlank(snapshot);
    }

    public void setArtifactId(String artifactId)
    {
        this.artifactId = artifactId;
    }

    public void setGroupId(String groupId)
    {
        this.groupId = groupId;
    }

    public void setReleased(String released)
    {
        this.released = released;
    }

    public void setSnapshot(String snapshot)
    {
        this.snapshot = snapshot;
    }

    private String valueOrBlank(String value)
    {
        return Optional.fromNullable(value).or("");
    }

    @Override
    public String toString()
    {
        return String.format("ResolvedArtifactVersion [artifactId=%s, groupId=%s, released=%s, snapshot=%s]", artifactId, groupId,
                released, snapshot);
    }

}
