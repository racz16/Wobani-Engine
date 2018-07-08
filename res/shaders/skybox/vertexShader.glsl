#version 420 core
layout (location = 0) in vec3 position;

out vec3 textureCoordinates;

layout (std140, binding = 1) uniform Matrices {
    mat4 viewMatrix;                                //0
    mat4 projectionMatrix;                          //64
};                                                  //128

void main(){
    mat4 view = mat4(mat3(viewMatrix));
    textureCoordinates = position;
    gl_Position = projectionMatrix * view * vec4(position, 1.0);
    gl_Position = gl_Position.xyww;
}   