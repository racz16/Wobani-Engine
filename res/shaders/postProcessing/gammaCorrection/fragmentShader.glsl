#version 330 core

out vec4 color;
in vec2 textureCoordinatesF;

uniform sampler2D image;
uniform float gamma;

void main(){
    color = texture(image, textureCoordinatesF);
    color = vec4(pow(color.rgb, vec3(1.0f / gamma)), 1);
} 