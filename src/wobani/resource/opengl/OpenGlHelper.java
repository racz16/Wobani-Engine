package wobani.resource.opengl;

import org.joml.*;
import wobani.resource.*;
import wobani.resource.opengl.buffer.*;
import wobani.resource.opengl.texture.*;

public class OpenGlHelper{

    /**
     Invalid id signs that the OpenGL Object is not available.
     */
    public static final int INVALID_ID = -1;

    public static void exceptionIfNull(Object... objects){
        for(Object obj : objects){
            if(obj == null){
                throw new RuntimeException();
            }
        }
    }

    public static void exceptionIfNotUsable(Resource resource){
        if(!resource.isUsable()){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotAvailable(OpenGlObject object){
        if(object.getId() == INVALID_ID){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfAllocated(TextureBase texture){
        if(texture.isAllocated()){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfAllocatedAndImmutable(BufferObject bufferObject){
        if(bufferObject.isAllocated() && bufferObject.isImmutable()){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotAllocated(TextureBase texture){
        if(!texture.isAllocated()){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotAllocated(BufferObject texture){
        if(!texture.isAllocated()){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfAreaExceedsFromSize(Vector2i area, Vector2i areaOffset, Vector2i size){
        if(area.x < 0 || area.y < 0 || areaOffset.x < 0 || areaOffset.y < 0 || size.x < 0 || size.y < 0){
            throw new RuntimeException();
        }
        if(area.x + areaOffset.x > size.x || area.y + areaOffset.y > size.y){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfFormatAndInternalFormatNotCompatible(Texture.TextureInternalFormat internalFormat, Texture.TextureFormat format){
        if(format.getColorChannelCount() != internalFormat.getColorChannelCount() ||
                format.getAttachmentSlot() != internalFormat.getAttachmentSlot()){
            throw new IllegalArgumentException("The format isn't compatible with the internal format");
        }
    }

    public static void exceptionIfAnyLowerThan(Vector2i vec, int limit){
        if(vec.x < limit || vec.y < limit){
            throw new RuntimeException();
        }
    }
}
