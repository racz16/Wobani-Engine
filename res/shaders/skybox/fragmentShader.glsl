#version 420 core

out vec4 color;

in vec3 textureCoordinates;

uniform samplerCube cubeMap;

void main(){    
    color = texture(cubeMap, textureCoordinates);
}