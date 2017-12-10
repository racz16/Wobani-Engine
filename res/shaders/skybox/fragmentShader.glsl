#version 420 core

out vec4 color;

in vec3 textureCoordinates;

uniform samplerCube cubeMap;
uniform bool isThereCubeMap;

void main(){
    if(isThereCubeMap){
        color = texture(cubeMap, textureCoordinates);
    }else{
        color = vec4(0.5f, 0.5f, 0.5f, 1);
    }
}