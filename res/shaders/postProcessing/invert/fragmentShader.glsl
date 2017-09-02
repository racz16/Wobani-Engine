#version 330 core

out vec4 color;
in vec2 textureCoordinatesF;

uniform sampler2D image;

void main(){             
    //color = texture(image, textureCoordinatesF);
    color = vec4(vec3(1.0 - texture(image, textureCoordinatesF)), 1.0);
} 