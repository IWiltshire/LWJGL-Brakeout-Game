// Tutorial recommends that classes for textures and shaders do not handle loading files. Rather, a different singleton class handles file loading.
// In Java, a singleton can be made from a class or enum. Class based singletons are not safe for multithreading. We make one by giving it a private constructor, a static field for its instance, and a static method for getting the instance.
import java.util.HashMap;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.BufferUtils.createIntBuffer;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_image_free;

public class ResourceManager {

	private static ResourceManager instance = null;
	
	private ResourceManager() { }
	
	public static ResourceManager getInstance() {
		if (ResourceManager.instance == null) {
			ResourceManager.instance = new ResourceManager();
		}
		return ResourceManager.instance;
	}
	
	// Resource storage
	static HashMap<String, Shader> Shaders = new HashMap<String, Shader>(); // Think this is correct
	static HashMap<String, Texture2D> Textures = new HashMap<String, Texture2D>();
	
	static Shader LoadShader(String vShaderFile, String fShaderFile, String name) { 
		Shaders.put(name, loadShaderFromFile(vShaderFile, fShaderFile)); // Bit annoying to do it like this instead of the Python or C way, i.e. Shaders[name] = loadShaderFromFile(vShaderFile, fShaderFile);		
		return Shaders.get(name);
	}
	
	static Shader GetShader(String name) { // Retrieves stored texture
		return Shaders.get(name);
	}
	
	static Texture2D LoadTexture(String file, boolean alpha, String name) {
		Textures.put(name, loadTextureFromFile(file, alpha));
		return Textures.get(name);
	}
	
	static Texture2D GetTexture(String name) {
		return Textures.get(name);
	}
	
	static void Clear() { 
		Shaders.forEach((name, shader) -> { // Deleting all shaders
			glDeleteProgram(shader.ID);
		});
		Shaders.clear(); // Clearing shader hashmap
		
		Textures.forEach((name, texture) -> {
			glDeleteTextures(texture.ID);
		});
		Textures.clear();
	}
	
	private static Shader loadShaderFromFile(String vShaderFile, String fShaderFile) { // Loads and generates shader from files
		String vShaderCode = ""; // Remember that these must start as empty strings to format code properly with concatenations
		String fShaderCode = "";
		
		try (FileReader vShaderMem = new FileReader(vShaderFile);
				FileReader fShaderMem = new FileReader(fShaderFile)){
			
			int vData = vShaderMem.read();
			while (vData != -1) {
				vShaderCode += (char) vData;
				vData = vShaderMem.read();
			}
			int fData = fShaderMem.read(); 
			while (fData != -1) {
				fShaderCode += (char) fData;
				fData = fShaderMem.read();
			}	
		} catch (FileNotFoundException e) { // Unlike previous try-with-resources blocks, you still need to catch exceptions here. I guess this block is only slightly more useful that the alternative?
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Shader shader = new Shader(); // Creating new shader
		shader.Compile(vShaderCode, fShaderCode); // Checking for compilation errors is built in to this method
		return shader;
	}
	
	private static Texture2D loadTextureFromFile(String file, boolean alpha) { // Generates a single texture from a file
		Texture2D texture = new Texture2D();
		
		if (alpha) {
			texture.Internal_Format = GL_RGBA;
			texture.Image_Format = GL_RGBA;
		}
		IntBuffer width = createIntBuffer(1);
		IntBuffer height = createIntBuffer(1);
		IntBuffer channels = createIntBuffer(1);
		//stbi_set_flip_vertically_on_load(true); // Don't need to flip image vertically with how I'm defining texture coordinates
		ByteBuffer data = stbi_load(file, width, height, channels, 0);
		if (data != null) { // Checking texture was read in properly
			texture.Generate(width.get(0), height.get(0), data); // Have to access IntBuffers to get ints for our generation function to work		
			stbi_image_free(data);
        }
		else {
			System.out.println("Failed to read image: " + stbi_failure_reason());
		}
		return texture;
	}
	
	// Maybe helpful functions
	static void listShaders() {
		Shaders.forEach((key, value) -> {
			System.out.println("Shader: "+key+" ; "+value);
		});
	}
	
	static void listTextures() {
		Textures.forEach((key, value) -> {
			System.out.println("Texture: "+key+" ; "+value);
		});
	}
}
