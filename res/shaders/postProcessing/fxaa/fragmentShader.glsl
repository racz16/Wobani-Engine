#version 330 core

out vec4 color;
in vec2 textureCoordinatesF;

uniform sampler2D image;

void main(){
    vec2 texCoordOffset = 1.0 / textureSize(image, 0);
    float R_fxaaSpanMax = 8.0;
    float R_fxaaReduceMin = 1.0 / 128.0;
    float R_fxaaReduceMul = 1.0 / 8.0;
	
    vec3 luma = vec3(0.2126, 0.7152, 0.0722);	
    float lumaTL = dot(luma, texture(image, textureCoordinatesF + (vec2(-1.0, -1.0) * texCoordOffset)).xyz);
    float lumaTR = dot(luma, texture(image, textureCoordinatesF + (vec2(1.0, -1.0) * texCoordOffset)).xyz);
    float lumaBL = dot(luma, texture(image, textureCoordinatesF + (vec2(-1.0, 1.0) * texCoordOffset)).xyz);
    float lumaBR = dot(luma, texture(image, textureCoordinatesF + (vec2(1.0, 1.0) * texCoordOffset)).xyz);
    float lumaM  = dot(luma, texture(image, textureCoordinatesF).xyz);

    vec2 dir;
    dir.x = -((lumaTL + lumaTR) - (lumaBL + lumaBR));
    dir.y = ((lumaTL + lumaBL) - (lumaTR + lumaBR));
	
    float dirReduce = max((lumaTL + lumaTR + lumaBL + lumaBR) * (R_fxaaReduceMul * 0.25), R_fxaaReduceMin);
    float inverseDirAdjustment = 1.0/(min(abs(dir.x), abs(dir.y)) + dirReduce);
	
    dir = min(vec2(R_fxaaSpanMax, R_fxaaSpanMax), 
          max(vec2(-R_fxaaSpanMax, -R_fxaaSpanMax), dir * inverseDirAdjustment)) * texCoordOffset;

    vec3 result1 = (1.0/2.0) * (texture(image, textureCoordinatesF.xy + (dir * vec2(1.0/3.0 - 0.5))).xyz +
                                texture(image, textureCoordinatesF.xy + (dir * vec2(2.0/3.0 - 0.5))).xyz);

    vec3 result2 = result1 * (1.0/2.0) + (1.0/4.0) * (texture(image, textureCoordinatesF.xy + (dir * vec2(0.0/3.0 - 0.5))).xyz +
                                                      texture(image, textureCoordinatesF.xy + (dir * vec2(3.0/3.0 - 0.5))).xyz);

    float lumaMin = min(lumaM, min(min(lumaTL, lumaTR), min(lumaBL, lumaBR)));
    float lumaMax = max(lumaM, max(max(lumaTL, lumaTR), max(lumaBL, lumaBR)));
    float lumaResult2 = dot(luma, result2);
	
    if(lumaResult2 < lumaMin || lumaResult2 > lumaMax){
        color = vec4(result1, 1.0);
    }else{
        color = vec4(result2, 1.0);
    }
}