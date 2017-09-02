#version 330 core

layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordinates;

out vec2 textureCoordinatesF;

void main(){
    textureCoordinatesF = textureCoordinates;
    gl_Position =  vec4(position, 1.0f);
}  