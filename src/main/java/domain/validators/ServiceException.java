package domain.validators;

public class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
}
