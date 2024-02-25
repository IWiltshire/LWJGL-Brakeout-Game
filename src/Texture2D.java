import java.nio.ByteBuffer;
import static org.lwjgl.opengl.GL33.*;

public class Texture2D {
	
	int ID; // ID of texture object
	int Width, Height; // Dimensions of texture image
	
	int Internal_Format; // Format of texture object
	int Image_Format; // Format of texture image
	// Texture configuration
	int Wrap_S; // Recall s is horizontal; t vertical
	int Wrap_T;
	int Filter_Min; // Filter mode if image is smaller than screen pixels
	int Filter_Max; // Converse to above
	
	Texture2D() { // Constructor sets default
		this.Width = 0;
		this.Height = 0;
		this.Internal_Format = GL_RGB;
		this.Image_Format = GL_RGB;
		this.Wrap_S = GL_REPEAT; 
		this.Wrap_T = GL_REPEAT;
		this.Filter_Min = GL_LINEAR; 
		this.Filter_Max = GL_NEAREST; // Using nearest neighbour instead of bilinear keeps low-res (16x16) pixel art sharp.
		
		this.ID = glGenTextures();
	}
	
	Texture2D(int width, int height, int internal_format, int image_format, int wrap_s, int wrap_t, int filter_min, int filter_max) {
		this.Width = width;
		this.Height = height;
		this.Internal_Format = internal_format;
		this.Image_Format = image_format;
		this.Wrap_S = wrap_s; 
		this.Wrap_T = wrap_t;
		this.Filter_Min = filter_min;
		this.Filter_Max = filter_max;	
		
		this.ID = glGenTextures();	
	}
	
	void Generate(int width, int height, ByteBuffer data) { // Generate image from texture
		this.Width = width;
		this.Height = height;
		// Creating texture
		glBindTexture(GL_TEXTURE_2D, this.ID);
		glTexImage2D(GL_TEXTURE_2D, 0, this.Internal_Format, width, height, 0, this.Image_Format, GL_UNSIGNED_BYTE, data);
		// Set texture wrapping and filtering
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, this.Wrap_S);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, this.Wrap_T);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, this.Filter_Min);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, this.Filter_Max);
		//glGenerateMipmap(GL_TEXTURE_2D); // This line may not be helpful for this specific project
		// Unbind texture
		glBindTexture(GL_TEXTURE_2D, 0);
	}
	
	void Bind() { // Binds texture to object
		glBindTexture(GL_TEXTURE_2D, this.ID);		
	}

}
