package ${package}.exception;

/** Missing business resource; the global handler maps it to 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
