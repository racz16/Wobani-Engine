package wobani.toolbox;

import org.lwjgl.system.*;
import wobani.toolbox.annotation.*;

import java.util.*;

/**
 Represents an OpenGL debug event. It van be an error, warning, performance tip or anything like that.
 */
public class OpenGlEvent{

    /**
     The source of the event.
     */
    private final OpenGlEventSource source;
    /**
     The type of the event.
     */
    private final OpenGlEventType type;
    /**
     The id of the event.
     */
    private final int id;
    /**
     The severity of the event.
     */
    private final OpenGlEventSeverity severity;
    /**
     The event message.
     */
    private String message;
    /**
     Initializes a new OpenGlEvent to the given values.

     @param source   the source of the event
     @param type     the type of the event
     @param id       the id of the event
     @param severity the severity of the event
     @param length   the length of the event message
     @param message  the event message's address
     */
    public OpenGlEvent(int source, int type, int id, int severity, int length, long message){
        this.source = OpenGlEventSource.valueOf(source);
        this.type = OpenGlEventType.valueOf(type);
        this.id = id;
        this.severity = OpenGlEventSeverity.valueOf(severity);
        setMessage(length, message);
    }

    /**
     Sets the event message based on the given parameters.

     @param length  message's length
     @param message message's address
     */
    private void setMessage(int length, long message){
        this.message = MemoryUtil.memUTF8Safe(MemoryUtil.memByteBuffer(message, length));
    }

    /**
     Returns the source of the event.

     @return the source of the event
     */
    @NotNull
    public OpenGlEventSource getSource(){
        return source;
    }

    /**
     Returns the type of the event.

     @return the type of the event
     */
    @NotNull
    public OpenGlEventType getType(){
        return type;
    }

    /**
     Returns the id of the event.

     @return the id of the event
     */
    public int getId(){
        return id;
    }

    /**
     Returns the severity of the event.

     @return the severity of the event
     */
    @NotNull
    public OpenGlEventSeverity getSeverity(){
        return severity;
    }

    /**
     Returns the event message.

     @return the event message
     */
    public String getMessage(){
        return message;
    }

    @Override
    public int hashCode(){
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.source);
        hash = 31 * hash + Objects.hashCode(this.type);
        hash = 31 * hash + this.id;
        hash = 31 * hash + Objects.hashCode(this.severity);
        return hash;
    }

    @Override
    public boolean equals(Object obj){
        if(this == obj){
            return true;
        }
        if(obj == null){
            return false;
        }
        if(getClass() != obj.getClass()){
            return false;
        }
        final OpenGlEvent other = (OpenGlEvent) obj;
        if(this.id != other.id){
            return false;
        }
        if(this.source != other.source){
            return false;
        }
        if(this.type != other.type){
            return false;
        }
        if(this.severity != other.severity){
            return false;
        }
        return true;
    }

    @Override
    public String toString(){
        StringBuilder res = new StringBuilder().append(OpenGlEvent.class.getSimpleName()).append("(")
                .append(" source: ").append(source).append(", type: ").append(type).append(", id: ").append(id)
                .append(", severity: ").append(severity).append(", message: ").append(message).append(")");
        return res.toString();
    }

    /**
     OpenGL event source.
     */
    public enum OpenGlEventSource{
        /**
         API.
         */
        API(0x8246), /**
         Window system.
         */
        WINDOW_SYSTEM(0x8247), /**
         Shader compiler.
         */
        SHADER_COMPILER(0x8248), /**
         Third party.
         */
        THIRD_PARTY(0x8249), /**
         Application.
         */
        APPLICATION(0x824A), /**
         Other.
         */
        OTHER(0x824B);

        /**
         Source's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new OpenGlEventSource to the given value.

         @param code source's OpenGL code
         */
        private OpenGlEventSource(int code){
            this.code = code;
        }

        /**
         Returns the OpenGlEventSource of the given OpenGL code.

         @param code OpenGL event source

         @return the OpenGlEventSource of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not an event source
         */
        @NotNull
        public static OpenGlEventSource valueOf(int code){
            for(OpenGlEventSource oes : OpenGlEventSource.values()){
                if(oes.getCode() == code){
                    return oes;
                }
            }
            throw new IllegalArgumentException("The given parameter is not an event source");
        }

        /**
         Returns the source's OpenGL code.

         @return the source's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     OpenGL event type.
     */
    public enum OpenGlEventType{
        /**
         Error.
         */
        ERROR(0x824C), /**
         Deprecated behívior.
         */
        DEPRECATED_BEHAVIOR(0x824D), /**
         Undefined behavior.
         */
        UNDEFINED_BEHAVIOR(0x824E), /**
         Portability.
         */
        PORTABILITY(0x824F), /**
         Performance.
         */
        PERFORMANCE(0x8250), /**
         Other.
         */
        OTHER(0x8251), /**
         Marker.
         */
        MARKER(0x8268);

        /**
         Type's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new OpenGlEventType to the given value.

         @param code type's OpenGL code
         */
        private OpenGlEventType(int code){
            this.code = code;
        }

        /**
         Returns the OpenGlEventType of the given OpenGL code.

         @param code OpenGL event type

         @return the OpenGlEventType of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not an event type
         */
        @NotNull
        public static OpenGlEventType valueOf(int code){
            for(OpenGlEventType oes : OpenGlEventType.values()){
                if(oes.getCode() == code){
                    return oes;
                }
            }
            throw new IllegalArgumentException("The given parameter is not an event type");
        }

        /**
         Returns the type's OpenGL code.

         @return the type's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

    /**
     OpenGL event severity.
     */
    public enum OpenGlEventSeverity{
        /**
         High severity.
         */
        HIGH(0x9146), /**
         Medium severity.
         */
        MEDIUM(0x9147), /**
         Low severity.
         */
        LOW(0x9148), /**
         Notification.
         */
        NOTIFICATION(0x826B);

        /**
         Severity's OpenGL code.
         */
        private final int code;

        /**
         Initializes a new OpenGlEventSeverity to the given value.

         @param code severity's OpenGL code
         */
        private OpenGlEventSeverity(int code){
            this.code = code;
        }

        /**
         Returns the OpenGlEventSeverity of the given OpenGL code.

         @param code OpenGL event severity

         @return the OpenGlEventSeverity of the given OpenGL code

         @throws IllegalArgumentException the given parameter is not an event severity
         */
        @NotNull
        public static OpenGlEventSeverity valueOf(int code){
            for(OpenGlEventSeverity oes : OpenGlEventSeverity.values()){
                if(oes.getCode() == code){
                    return oes;
                }
            }
            throw new IllegalArgumentException("The given parameter is not an event severity");
        }

        /**
         Returns the severity's OpenGL code.

         @return the severity's OpenGL code
         */
        public int getCode(){
            return code;
        }
    }

}
