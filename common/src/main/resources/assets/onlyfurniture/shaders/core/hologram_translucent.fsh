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

vec3 toLinear(vec3 c) { return pow(c, vec3(2.2)); }
vec3 toSRGB(vec3 c) { return pow(c, vec3(1.0 / 2.2)); }

vec3 linearToOklab(vec3 c) {
    const mat3 lmsMat = mat3(
    0.4122214708, 0.2119034982, 0.0883024619,
    0.5363325363, 0.6806995451, 0.2817188376,
    0.0514459929, 0.1073969566, 0.6299787005
    );
    vec3 lms = lmsMat * c;
    lms = pow(lms, vec3(1.0 / 3.0));
    const mat3 oklabMat = mat3(
    0.2104542553, 1.9779984951, 0.0259040371,
    0.7936177850, -2.4285922050, 0.7827717662,
    -0.0040720468, 0.4505937099, -0.8086757660
    );
    return oklabMat * lms;
}

vec3 oklabToLinear(vec3 c) {
    const mat3 oklabToLMS = mat3(
    1.0, 1.0, 1.0,
    0.3963377774, -0.1055613458, -0.0894841775,
    0.2158037573, -0.0638541728, -1.2914855480
    );
    vec3 lms = oklabToLMS * c;
    lms = lms * lms * lms;
    const mat3 lmsToRGB = mat3(
    4.0767416621, -1.2684380046, -0.0041960863,
    -3.3077115913, 2.6097574011, -0.7034186147,
    0.2309699292, -0.3413193965, 1.7076147010
    );
    return lmsToRGB * lms;
}

vec3 tintOklab(vec3 texRGB, vec3 vertexRGB, float tintStrength) {
    // Convert only the texture to linear (vertex color is already linear in many pipelines)
    vec3 texLab = linearToOklab(toLinear(texRGB));
    vec3 vertLab = linearToOklab(vertexRGB);

    // Keep texture lightness
    float L = texLab.x;

    // Blend chroma (a,b)
    vec2 texAB = texLab.yz;
    vec2 vertAB = vertLab.yz;

    // Optional: normalize chroma length to avoid hue distortion
    float texLen = length(texAB);
    float vertLen = length(vertAB);
    if (vertLen > 1e-5) vertAB *= texLen / vertLen;

    vec2 mixedAB = mix(texAB, vertAB, tintStrength);

    // Blend lightness instead of multiply
    float vertexLuma = dot(vertexRGB, vec3(0.299, 0.587, 0.114));
    L = mix(L, vertexLuma, tintStrength * 0.3);

    vec3 outLab = vec3(L, mixedAB);
    return toSRGB(oklabToLinear(outLab));
}

void main() {
    vec3 tex = texture(Sampler0, texCoord0).rgb;
    vec3 vcol = vertexColor.rgb;
    vec4 color = vec4(tintOklab(tex, vcol, 1.0), vertexColor.a) * ColorModulator;

    if (color.a < 0.1) discard;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
