package io.dangernoodle.aether;

import java.util.Arrays;
import java.util.Collection;


public class ResolutionException extends Exception
{
    private static final long serialVersionUID = -6131536734899503703L;

    private final Collection<Exception> exceptions;

    public ResolutionException(Exception exception)
    {
        this(Arrays.asList(exception));
    }

    public ResolutionException(Collection<Exception> exceptions)
    {
        this.exceptions = exceptions;
    }

    public Collection<Exception> getExceptions()
    {
        return exceptions;
    }
}
