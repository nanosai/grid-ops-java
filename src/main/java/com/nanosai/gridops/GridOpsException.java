package com.nanosai.gridops;

/**
 * Created by jjenkov on 08/05/2017.
 */
public class GridOpsException extends RuntimeException {

    public GridOpsException() {
        super();
    }

    public GridOpsException(String message) {
        super(message);
    }

    public GridOpsException(String message, Throwable cause) {
        super(message, cause);
    }

    public GridOpsException(Throwable cause) {
        super(cause);
    }

    protected GridOpsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
