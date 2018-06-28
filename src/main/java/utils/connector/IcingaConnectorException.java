package utils.connector;

/**
 * Exception thrown by the IcingaConnector Class
 */
public class IcingaConnectorException extends Exception {

    public IcingaConnectorException(String s) {
        super(s);
    }

    public IcingaConnectorException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public IcingaConnectorException(Throwable throwable) {
        super(throwable);
    }
}
