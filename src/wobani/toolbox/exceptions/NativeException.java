package wobani.toolbox.exceptions;

import wobani.toolbox.EngineInfo.Library;
import wobani.toolbox.annotation.*;

/**
 * Signs that not the engine caused the error but one of the underlying
 * libraries.
 */
public class NativeException extends RuntimeException {

    /**
     * The causing library.
     */
    private final Library library;

    /**
     * Initializes a new NativeException to the given parameters.
     *
     * @param library the causing library
     * @param message error message
     */
    public NativeException(@Nullable Library library, @Nullable String message) {
	super(message);
	this.library = library;
    }

    /**
     * Returns the causing library.
     *
     * @return the causing library
     */
    public Library getLibrary() {
	return library;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(NativeException.class.getSimpleName()).append("(")
		.append(" causing library: ").append(library)
		.append(")");
	return res.toString();
    }

}
