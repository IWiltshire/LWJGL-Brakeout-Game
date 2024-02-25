import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import static org.lwjgl.opengl.GL33.*;

public class Shader {
	
	int ID;
	
	Shader() {
		
	}
	
	Shader Use() {
		glUseProgram(this.ID);
		return this;
	}
	
	void Compile(String vertexSource, String fragmentSource) {
		int sVertex, sFragment;
		// Vertex shader
		sVertex = glCreateShader(GL_VERTEX_SHADER); // This was the problem line!!! The issue seems to have been that I was using gl functions here before an appropriate context was made.
		glShaderSource(sVertex, vertexSource);
		glCompileShader(sVertex);
		checkCompileErrors(sVertex, "VERTEX");
		// Fragment shader
		sFragment = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(sFragment, fragmentSource);
		glCompileShader(sFragment);
		checkCompileErrors(sFragment, "FRAGMENT");
		// Shader program
		this.ID = glCreateProgram();
        glAttachShader(this.ID, sVertex);
        glAttachShader(this.ID, sFragment);
        glLinkProgram(this.ID);
        checkCompileErrors(this.ID, "PROGRAM");
        // Delete unnecessary shaders
        glDeleteShader(sVertex);
        glDeleteShader(sFragment);
	}
	
	void SetFloat(String name, float value, boolean useShader) {
		if (useShader) { // Not as useful as I expected it to be
			this.Use();
		}
		int uniformLoc = glGetUniformLocation(this.ID, name);
		glUniform1f(uniformLoc, value);	
	}
	
	void SetInteger(String name, int value, boolean useShader) {
		if (useShader) {
			this.Use();
		}
		int uniformLoc = glGetUniformLocation(this.ID, name);
		glUniform1i(uniformLoc, value);	
	}
	
	void SetVector3f(String name, Vector3f vec, boolean useShader) {
		if (useShader) {
			this.Use();
		}
		int uniformLoc = glGetUniformLocation(this.ID, name);
		glUniform3f(uniformLoc, vec.x, vec.y, vec.z);		
	}
		
	void SetMat4f(String name, Matrix4f matrix, boolean useShader) {
		if (useShader) {
			this.Use();
		}
		int uniformLoc = glGetUniformLocation(this.ID, name);
		try (MemoryStack stack = MemoryStack.stackPush()) { 
			glUniformMatrix4fv(uniformLoc, false, matrix.get(stack.mallocFloat(16)));			
		}
	}
	
	private void checkCompileErrors(int object, String type) {
		int success;
		int len;		
		
		if (type != "PROGRAM") { // Dealing with fragment and vertex (and geometry) shaders
			success = glGetShaderi(object, GL_COMPILE_STATUS);
			if (success != 1) {
				len = glGetShaderi(object, GL_INFO_LOG_LENGTH); // Using this instead of just supposing that length is 1024
				System.out.println("ERROR with "+object+":  Shader compilation failed");
	            System.out.println(glGetShaderInfoLog(object, len));
			}
		}
		else { // Program compilation (linkage)
			success = glGetProgrami(object, GL_LINK_STATUS);
			//System.out.println(success);
			if (success != 1) {
				len = glGetProgrami(object, GL_INFO_LOG_LENGTH); 
				System.out.println("ERROR: Shader program compilation failed");
	            System.out.println(glGetProgramInfoLog(object, len));
			}
			//else {System.out.println("Successful linkage");}; // Checking program linked
		}
	}
}
