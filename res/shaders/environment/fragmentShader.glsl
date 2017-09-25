#version 420 core

struct Material {
    bool isThereDiffuseMap;
    sampler2D diffuse;
    vec2 diffuseTile;
    vec2 diffuseOffset;
    vec3 diffuseColor;
}; 

struct Light {              //base alignment        alignment offset
    vec3 position;          //16                    0
    vec3 direction;         //16                    16
    vec3 attenuation;       //16                    32
    vec3 ambient;           //16                    48
    vec3 diffuse;           //16                    64
    vec3 specular;          //16                    80
    vec2 cutOff;            //8                     96
    int type;               //4                     104
    bool active;            //4                     108
};                          //                      112

#define DIRECTIONAL_LIGHT 0
#define lightNumber 16
layout (std140, binding = 1) uniform LightSources {
    Light lights[lightNumber];                      //i * 112
    Light directionalLight;                         //1792
    int maxLightSources;                            //1904
};                                                  //1908

in vec3 normalF;
in vec2 textureCoordinatesF;

out vec4 color;

uniform Material material;
uniform bool gamma;

//lighting
vec3 calculateLight(vec3 materialDiffuseColor, vec3 normalVector);
vec3 calculateDiffuseColor(vec3 materialDiffuseColor, vec3 lightDiffuseColor, vec3 normalVector, vec3 lightDirection);
//data collection
vec3 getDiffuseColor(vec2 textureCoordinates);

void main(){
    //collecting data
    vec3 normalVector = normalize(normalF);
    vec3 diffuseColor = getDiffuseColor(textureCoordinatesF);
    //directional light
    vec3 result = calculateLight(diffuseColor, normalVector);
    color = vec4(result, 1);
    //color = vec4(1,0,0,1);
}

//
//lighting----------------------------------------------------------------------
//
vec3 calculateLight(vec3 materialDiffuseColor, vec3 normalVector){
    Light light = directionalLight;

    vec3 diffuse = calculateDiffuseColor(materialDiffuseColor, light.diffuse, normalVector, light.direction);
    vec3 ambient = vec3(0.2f, 0.2f, 0.2f);
    return ambient + diffuse;
}

vec3 calculateDiffuseColor(vec3 materialDiffuseColor, vec3 lightDiffuseColor, vec3 normalVector, vec3 lightDirection){
    float diffuseStrength = max(dot(normalVector, -lightDirection), 0.0);
    vec3 diffuse = lightDiffuseColor * diffuseStrength * materialDiffuseColor;
    return diffuse;
}


//
//data collection---------------------------------------------------------------
//
vec3 getDiffuseColor(vec2 textureCoordinates){
    vec3 diffuse;
    if(material.isThereDiffuseMap){
        vec4 tex = texture(material.diffuse, textureCoordinates * material.diffuseTile + material.diffuseOffset);
        if(tex.a == 0){
            discard;
        }
        diffuse = tex.rgb;
    }else{
        if(gamma){
            diffuse = pow(material.diffuseColor, vec3(2.2f));
        }else{
            diffuse = material.diffuseColor;
        }
    }
    return diffuse;
}