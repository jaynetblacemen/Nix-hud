#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;
uniform float Phosphor;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 distinct = texture(DiffuseSampler, texCoord);
    vec4 past = texture(PrevSampler, texCoord);

    fragColor = mix(distinct, past, Phosphor);
}
