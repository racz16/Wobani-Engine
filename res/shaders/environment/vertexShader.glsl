#version 420 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordinates;
layout (location = 2) in vec3 normal;

out vec3 normalF;
out vec2 textureCoordinatesF;

uniform mat4 modelMatrix;
uniform mat4 projectionMatrix;
uniform mat4 viewMatrix;
uniform mat3 inverseModelMatrix3x3;

void main(){
    gl_Position = projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0f);
    normalF = normalize(normal * inverseModelMatrix3x3);
    textureCoordinatesF = textureCoordinates;
} 