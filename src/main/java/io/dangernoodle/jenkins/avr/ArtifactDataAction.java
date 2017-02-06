package io.dangernoodle.jenkins.avr;

import java.util.HashMap;
import java.util.Map;

import hudson.model.InvisibleAction;


public class ArtifactDataAction extends InvisibleAction
{
    private final Map<String, String> environment = new HashMap<>();

    public void addVersionNumber(String key, String version)
    {
        environment.put(key, version);
    }

    public Map<String, String> getEnvironment()
    {
        return new HashMap<>(environment);
    }
}
