#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;
in vec2 texCoord1;

out vec4 fragColor;

vec3 rgb2hsl(vec3 color) {
    float maxC = max(max(color.r, color.g), color.b);
    float minC = min(min(color.r, color.g), color.b);
    float delta = maxC - minC;

    float h = 0.0;
    if (delta > 0.0) {
        if (maxC == color.r) {
            h = mod(((color.g - color.b) / delta), 6.0);
        } else if (maxC == color.g) {
            h = ((color.b - color.r) / delta) + 2.0;
        } else {
            h = ((color.r - color.g) / delta) + 4.0;
        }
        h /= 6.0;
        if (h < 0.0) h += 1.0;
    }

    float l = (maxC + minC) * 0.5;
    float s = (delta == 0.0) ? 0.0 : delta / (1.0 - abs(2.0 * l - 1.0));

    return vec3(h, s, l);
}

vec3 hsl2rgb(vec3 hsl) {
    float h = hsl.x;
    float s = hsl.y;
    float l = hsl.z;

    float c = (1.0 - abs(2.0 * l - 1.0)) * s;
    float x = c * (1.0 - abs(mod(h * 6.0, 2.0) - 1.0));
    float m = l - 0.5 * c;

    vec3 rgb;
    if (h < 1.0/6.0) rgb = vec3(c, x, 0.0);
    else if (h < 2.0/6.0) rgb = vec3(x, c, 0.0);
    else if (h < 3.0/6.0) rgb = vec3(0.0, c, x);
    else if (h < 4.0/6.0) rgb = vec3(0.0, x, c);
    else if (h < 5.0/6.0) rgb = vec3(x, 0.0, c);
    else rgb = vec3(c, 0.0, x);

    return rgb + vec3(m);
}

vec4 hologram(vec4 inputColor, vec4 targetColor) {
    vec3 hslInput = rgb2hsl(inputColor.rgb);
    hslInput.y = 0.0;
    vec3 rgbOutput = hsl2rgb(hslInput);

    return vec4(mix(rgbOutput, targetColor.rgb, 0.5f), inputColor.a * targetColor.a);
}

void main() {
    vec4 color = hologram(texture(Sampler0, texCoord0), vertexColor);

    if (color.a < 0.1) discard;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
