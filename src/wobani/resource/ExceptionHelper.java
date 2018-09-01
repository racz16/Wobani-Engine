package wobani.resource;

import org.joml.*;
import wobani.resource.opengl.*;
import wobani.resource.opengl.buffer.*;
import wobani.resource.opengl.fbo.*;
import wobani.resource.opengl.texture.*;

public class ExceptionHelper{

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

    public static void exceptionIfAllocated(Rbo rbo){
        if(rbo.isAllocated()){
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

    public static void exceptionIfNotInsideClosedInterval(int left, int right, int value){
        if(left > value || right < value){
            throw new RuntimeException("The value isn't inside the interval");
        }
    }

    public static void exceptionIfNotInsideOpenInterval(int left, int right, int value){
        if(left >= value || right <= value){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotInsideLeftOpenRightClosedInterval(int left, int right, int value){
        if(left >= value || right < value){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotInsideLeftClosedRightOpenInterval(int left, int right, int value){
        if(left > value || right <= value){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotInsideClosedInterval(int start, int end, Vector2i value){
        if(value.x < start || value.y < start || value.x > end || value.y > end){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotHdrColor(Vector3f color){
        if(color.x < 0 || color.y < 0 || color.z < 0){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotLdrColor(Vector3f color){
        if(color.x < 0 || color.y < 0 || color.z < 0 || color.x > 1 || color.y > 1 || color.z > 1){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfNotLdrColor(Vector4f color){
        if(color.x < 0 || color.y < 0 || color.z < 0 || color.w < 0 || color.x > 1 || color.y > 1 || color.z > 1 || color.w > 1){
            throw new RuntimeException();
        }
    }

    public static void exceptionIfLower(int limit, int value){
        if(isLower(limit, value)){
            throw new IllegalArgumentException();
        }
    }

    public static void exceptionIfLower(long limit, long value){
        if(value < limit){
            throw new IllegalArgumentException();
        }
    }

    public static void exceptionIfLowerOrEquals(int limit, int value){
        if(isLowerOrEquals(limit, value)){
            throw new IllegalArgumentException();
        }
    }

    public static boolean isLower(int limit, int value){
        return value < limit;
    }

    public static boolean isLowerOrEquals(int limit, int value){
        return value <= limit;
    }

    public static void exceptionIfAnyLowerThan(Vector2i vec, int limit){
        if(vec.x < limit || vec.y < limit){
            throw new RuntimeException();
        }
    }
}
