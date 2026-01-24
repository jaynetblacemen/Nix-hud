#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D PrevSampler;
uniform float Phosphor;

in vec2 texCoord;

out vec4 fragColor;

void main() {
    vec4 current = texture(DiffuseSampler, texCoord);
    vec4 previous = texture(PrevSampler, texCoord);

    fragColor = mix(current, previous, Phosphor);
}
