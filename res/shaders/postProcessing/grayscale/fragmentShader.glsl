#version 330 core

out vec4 color;
in vec2 textureCoordinatesF;

uniform sampler2D image;

void main(){
    color = texture(image, textureCoordinatesF);
    float average = 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;
    color = vec4(average, average, average, 1.0);
} 