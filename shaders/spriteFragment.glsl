#version 330 core
in vec2 TexCoords;
out vec4 colour;

uniform sampler2D image;
uniform vec3 spriteColour;

void main()
{
	colour = vec4(spriteColour, 1.0f) * texture(image, TexCoords); // It seems in glsl that you needn't actually need a "f" after a float. I'll put them in anyway for clarity.
}