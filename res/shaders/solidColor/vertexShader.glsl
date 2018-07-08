#version 420 core

layout (location = 0) in vec3 position;

uniform mat4 modelMatrix;

layout (std140, binding = 1) uniform Matrices {
    mat4 viewMatrix;                                //0
    mat4 projectionMatrix;                          //64
};                                                  //128

void main(){
    gl_Position =  projectionMatrix * viewMatrix * modelMatrix * vec4(position, 1.0f);
}  