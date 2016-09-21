package com.gizwits.openapi.sdk;

public class GizwitsException extends Exception
{
    private static final long serialVersionUID = 0L;

    public GizwitsException(String message) {
        super(message);
    }

    public GizwitsException(String message, Throwable cause) {
        super(message, cause);
    }

    public GizwitsException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}
