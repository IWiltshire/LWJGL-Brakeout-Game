// Each object in the game (player, bricks, etc) is represented by a GameObject instance
import org.joml.Vector2f;
import org.joml.Vector3f;

public class GameObject {
	// Object state
	Vector2f Position, Size, Velocity; // This stuff is obviously spat into the sprite renderer
	Vector3f Colour;
	float Rotation;
	boolean IsSolid;
	boolean Destroyed;
	// Render state
	Texture2D Sprite;
	
	GameObject(Vector2f pos, Vector2f size, Texture2D sprite, Vector3f colour, Vector2f velocity) {
		this.Position = pos;
		this.Size = size;
		this.Velocity = velocity;
		this.Colour = colour;
		this.Rotation = 0.0f;
		this.Sprite = sprite;
		this.IsSolid = false;
		this.Destroyed = false;
	}
	
	GameObject() {
		this.Position = new Vector2f(0.0f, 0.0f);
		this.Size = new Vector2f(1.0f, 1.0f);
		this.Velocity = new Vector2f(0.0f);
		this.Colour = new Vector3f(1.0f);
		this.Rotation = 0.0f;
		this.Sprite = new Texture2D(); // Check this later
		this.IsSolid = false;
		this.Destroyed = false;
	}
	
	void Draw(SpriteRenderer renderer) { 
		renderer.DrawSprite(this.Sprite, this.Position, this.Size, this.Rotation, this.Colour);
	}
	
}
