import org.joml.*;
import java.nio.*;
import java.lang.Math;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.BufferUtils.*;

public class SpriteRenderer {
	private Shader shader;
	private int quadVAO;
	
	SpriteRenderer(Shader shader) { // Constructor. 
		this.shader = shader;
		initRenderData();
	}
	
	void DrawSprite(Texture2D texture, Vector2f position, Vector2f size, float rotate, Vector3f colour) {		
		this.shader.Use();
		Matrix4f model = new Matrix4f(); // Initialised as identity matrix
		model.translate(position.x, position.y, 0.0f); // Translate to screen position
		
		model.translate(0.5f * size.x, 0.5f * size.y, 0.0f); // Moving origin to top left so that rotations work well
		model.rotate((float) Math.toRadians(rotate), 0.0f, 0.0f, 1.0f); // Rotation is done around z axis (think of 2D sprites with the right hand system)
		model.translate(-0.5f * size.x, -0.5f * size.y, 0.0f); // Moving sprite back
		
		model.scale(size.x, size.y, 1.0f); // Scalling sprite
		
		this.shader.SetMat4f("model", model, false); // Tutorial has omitted useShader since this block starts with the use method		
		// render textured quad
		this.shader.SetVector3f("spriteColour", colour, false);
		
		glActiveTexture(GL_TEXTURE0);
		texture.Bind();
		
		glBindVertexArray(this.quadVAO);
		//glDrawArrays(GL_TRIANGLES, 0, 6);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0); // This needed because I'm using an EBO now as opposed to duplicating vertices
		glBindVertexArray(0);
	}
	
	void DrawSprite(Texture2D texture, Vector2f position) { // Absolutely stupid that I have to use overloaded methods instead of just being able to specify a default parameter
		Vector2f size = new Vector2f(10.0f, 10.0f);
		float rotate = 0.0f;
		Vector3f colour = new Vector3f(1.0f);
		DrawSprite(texture, position, size, rotate, colour);
	}
	
	private void initRenderData() {
		int VBO, EBO;
		float vertices[] = {
		        // pos      // tex
		        0.0f, 1.0f, 0.0f, 1.0f,
		        1.0f, 0.0f, 1.0f, 0.0f,
		        0.0f, 0.0f, 0.0f, 0.0f, 

		        //0.0f, 1.0f, 0.0f, 1.0f,
		        1.0f, 1.0f, 1.0f, 1.0f,
		        //1.0f, 0.0f, 1.0f, 0.0f // Can reduce from 6 to 4 vertices by using an EBO (quad otherwise made of 2 triangles)

		};
		FloatBuffer verticesBuffer = createFloatBuffer(vertices.length);
		verticesBuffer.put(vertices).flip();
		
		int[] indices = {
				0,1,2, // first triangle
				0,1,3  // second triangle
		};
		IntBuffer vertexIndices = createIntBuffer(indices.length);
		vertexIndices.put(indices).flip();
		
		this.quadVAO = glGenVertexArrays();
		glBindVertexArray(this.quadVAO);
		
		VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, VBO);	
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);	
		
		EBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, EBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, vertexIndices, GL_STATIC_DRAW);
		
		// !!!!!!!!!!!!
		glVertexAttribPointer(0, 4, GL_FLOAT, false, 4*Float.BYTES, 0); // The error which stumped me for so long was that I wrote ".SIZE" instead of ".BYTES"!!!!
		// ^^^^^^^^^^^^
		glEnableVertexAttribArray(0);
		
		// Unbinding buffers
		glBindVertexArray(0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);	
	}
}
