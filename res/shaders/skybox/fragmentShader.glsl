#version 420 core

out vec4 color;

in vec3 textureCoordinates;

uniform samplerCube cubeMap;
uniform bool isThereCubeMap;

void main(){
    if(isThereCubeMap){
        color = texture(cubeMap, textureCoordinates);
        //without gamma correction:
        //color = pow(texture(cubeMap, textureCoordinates),vec4(1.0f/2.2f,1.0f/2.2f,1.0f/2.2f,1.0f));
    }else{
        color = vec4(0.5f, 0.5f, 0.5f, 1);
    }
}