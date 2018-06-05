package wobani.toolbox;

import java.util.*;
import org.lwjgl.system.*;
import wobani.toolbox.annotation.*;

public class OpenGlError {

    /**
     * OpenGL error source.
     */
    public enum OpenGlErrorSource {
	/**
	 * API.
	 */
	API(0x8246),
	/**
	 * Window system.
	 */
	WINDOW_SYSTEM(0x8247),
	/**
	 * Shader compiler.
	 */
	SHADER_COMPILER(0x8248),
	/**
	 * Third party.
	 */
	THIRD_PARTY(0x8249),
	/**
	 * Application.
	 */
	APPLICATION(0x824A),
	/**
	 * Other.
	 */
	OTHER(0x824B);

	/**
	 * Source's OpenGL code.
	 */
	private final int code;

	/**
	 * Initializes a new OpenGlErrorSource to the given value.
	 *
	 * @param code source's OpenGL code
	 */
	private OpenGlErrorSource(int code) {
	    this.code = code;
	}

	/**
	 * Returns the source's OpenGL code.
	 *
	 * @return the source's OpenGL code
	 */
	public int getCode() {
	    return code;
	}

	/**
	 * Returns the OpenGlErrorSource of the given OpenGL code.
	 *
	 * @param code OpenGL error source
	 *
	 * @return the OpenGlErrorSource of the given OpenGL code
	 *
	 * @throws IllegalArgumentException the given parameter is not an error
	 *                                  source
	 */
	@NotNull
	public static OpenGlErrorSource valueOf(int code) {
	    for (OpenGlErrorSource oes : OpenGlErrorSource.values()) {
		if (oes.getCode() == code) {
		    return oes;
		}
	    }
	    throw new IllegalArgumentException("The given parameter is not an error source");
	}
    }

    /**
     * OpenGL error type.
     */
    public enum OpenGlErrorType {
	/**
	 * Error.
	 */
	ERROR(0x824C),
	/**
	 * Deprecated behívior.
	 */
	DEPRECATED_BEHAVIOR(0x824D),
	/**
	 * Undefined behavior.
	 */
	UNDEFINED_BEHAVIOR(0x824E),
	/**
	 * Portability.
	 */
	PORTABILITY(0x824F),
	/**
	 * Performance.
	 */
	PERFORMANCE(0x8250),
	/**
	 * Other.
	 */
	OTHER(0x8251),
	/**
	 * Marker.
	 */
	MARKER(0x8268);

	/**
	 * Type's OpenGL code.
	 */
	private final int code;

	/**
	 * Initializes a new OpenGlErrorType to the given value.
	 *
	 * @param code type's OpenGL code
	 */
	private OpenGlErrorType(int code) {
	    this.code = code;
	}

	/**
	 * Returns the type's OpenGL code.
	 *
	 * @return the type's OpenGL code
	 */
	public int getCode() {
	    return code;
	}

	/**
	 * Returns the OpenGlErrorType of the given OpenGL code.
	 *
	 * @param code OpenGL error type
	 *
	 * @return the OpenGlErrorType of the given OpenGL code
	 *
	 * @throws IllegalArgumentException the given parameter is not an error
	 *                                  type
	 */
	@NotNull
	public static OpenGlErrorType valueOf(int code) {
	    for (OpenGlErrorType oes : OpenGlErrorType.values()) {
		if (oes.getCode() == code) {
		    return oes;
		}
	    }
	    throw new IllegalArgumentException("The given parameter is not an error type");
	}
    }

    /**
     * OpenGL error severity.
     */
    public enum OpenGlErrorSeverity {
	/**
	 * High severity.
	 */
	HIGH(0x9146),
	/**
	 * Medium severity.
	 */
	MEDIUM(0x9147),
	/**
	 * Low severity.
	 */
	LOW(0x9148),
	/**
	 * Notification.
	 */
	NOTIFICATION(0x826B);

	/**
	 * Severity's OpenGL code.
	 */
	private final int code;

	/**
	 * Initializes a new OpenGlErrorSeverity to the given value.
	 *
	 * @param code severity's OpenGL code
	 */
	private OpenGlErrorSeverity(int code) {
	    this.code = code;
	}

	/**
	 * Returns the severity's OpenGL code.
	 *
	 * @return the severity's OpenGL code
	 */
	public int getCode() {
	    return code;
	}

	/**
	 * Returns the OpenGlErrorSeverity of the given OpenGL code.
	 *
	 * @param code OpenGL error severity
	 *
	 * @return the OpenGlErrorSeverity of the given OpenGL code
	 *
	 * @throws IllegalArgumentException the given parameter is not an error
	 *                                  severity
	 */
	@NotNull
	public static OpenGlErrorSeverity valueOf(int code) {
	    for (OpenGlErrorSeverity oes : OpenGlErrorSeverity.values()) {
		if (oes.getCode() == code) {
		    return oes;
		}
	    }
	    throw new IllegalArgumentException("The given parameter is not an error severity");
	}
    }

    /**
     * The source of the error.
     */
    private final OpenGlErrorSource source;
    /**
     * The type of the error.
     */
    private final OpenGlErrorType type;
    /**
     * The id of the error.
     */
    private final int id;
    /**
     * The severity of the error.
     */
    private final OpenGlErrorSeverity severity;
    /**
     * The error message.
     */
    private String message;

    /**
     * Initializes a new OpenGlError to the given values.
     *
     * @param source   the source of the error
     * @param type     the type of the error
     * @param id       the id of the error
     * @param severity the severity of the error
     * @param length   the length of the error message
     * @param message  the error message's address
     */
    public OpenGlError(int source, int type, int id, int severity, int length, long message) {
	this.source = OpenGlErrorSource.valueOf(source);
	this.type = OpenGlErrorType.valueOf(type);
	this.id = id;
	this.severity = OpenGlErrorSeverity.valueOf(severity);
	setMessage(length, message);
    }

    /**
     * Sets the error message based on the given parameters.
     *
     * @param length  message's length
     * @param message message's address
     */
    private void setMessage(int length, long message) {
	this.message = MemoryUtil.memUTF8Safe(MemoryUtil.memByteBuffer(message, length));
    }

    /**
     * Returns the source of the error.
     *
     * @return the source of the error
     */
    @NotNull
    public OpenGlErrorSource getSource() {
	return source;
    }

    /**
     * Returns the type of the error.
     *
     * @return the type of the error
     */
    @NotNull
    public OpenGlErrorType getType() {
	return type;
    }

    /**
     * Returns the id of the error.
     *
     * @return the id of the error
     */
    public int getId() {
	return id;
    }

    /**
     * Returns the severity of the error.
     *
     * @return the severity of the error
     */
    @NotNull
    public OpenGlErrorSeverity getSeverity() {
	return severity;
    }

    /**
     * Returns the error message.
     *
     * @return the error message
     */
    public String getMessage() {
	return message;
    }

    @Override
    public int hashCode() {
	int hash = 5;
	hash = 31 * hash + Objects.hashCode(this.source);
	hash = 31 * hash + Objects.hashCode(this.type);
	hash = 31 * hash + this.id;
	hash = 31 * hash + Objects.hashCode(this.severity);
	return hash;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final OpenGlError other = (OpenGlError) obj;
	if (this.id != other.id) {
	    return false;
	}
	if (this.source != other.source) {
	    return false;
	}
	if (this.type != other.type) {
	    return false;
	}
	if (this.severity != other.severity) {
	    return false;
	}
	return true;
    }

    @Override
    public String toString() {
	StringBuilder res = new StringBuilder()
		.append(OpenGlError.class.getSimpleName()).append("(")
		.append(" source: ").append(source)
		.append(", type: ").append(type)
		.append(", id: ").append(id)
		.append(", severity: ").append(severity)
		.append(", message: ").append(message)
		.append(")");
	return res.toString();
    }

}
