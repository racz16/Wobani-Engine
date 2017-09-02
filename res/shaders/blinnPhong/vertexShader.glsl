#version 420 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordinates;
layout (location = 2) in vec3 normal;
layout (location = 3) in vec3 tangent;

out vec3 normalF;
out vec3 fragmentPositionF;
out vec2 textureCoordinatesF;
out vec4 fragmentPositionLightSpace;
out vec3 viewPositionF;
out mat3 TBN;
out mat3 inverseModelMatrix3x3F;
out mat4 shadowProjectionViewMatrixF;

layout (std140, binding = 2) uniform Matrices {
    mat4 viewMatrix;                                //0
    mat4 projectionMatrix;                          //64
};                                                  //128

uniform vec3 viewPosition;
uniform float useNormalMap;
uniform mat4 modelMatrix;
uniform mat3 inverseModelMatrix3x3;
uniform mat4 shadowProjectionViewMatrix;

void main(){
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0f);
    fragmentPositionF = vec3(modelMatrix * vec4(position, 1.0f));
    normalF = normalize(normal * inverseModelMatrix3x3);
    textureCoordinatesF = textureCoordinates;
    fragmentPositionLightSpace = shadowProjectionViewMatrix * vec4(fragmentPositionF, 1.0);
    shadowProjectionViewMatrixF = shadowProjectionViewMatrix;
    viewPositionF = viewPosition;
    if(useNormalMap == 1){
        vec3 tangentColumn = normalize(mat3(modelMatrix) * tangent);
        vec3 normalColumn = normalize(mat3(modelMatrix) * normal);
        tangentColumn = normalize(tangentColumn - dot(tangentColumn, normalColumn) * normalColumn);
        vec3 bitangentColumn = cross(normalColumn, tangentColumn);
        TBN = mat3(tangentColumn, bitangentColumn, normalColumn);
        inverseModelMatrix3x3F = inverseModelMatrix3x3;
    }

} 