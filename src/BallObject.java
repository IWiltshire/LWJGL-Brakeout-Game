import org.joml.Vector2f;
import org.joml.Vector3f;

public class BallObject extends GameObject{
	// Ball state
	float Radius;
	boolean Stuck;	
	
	BallObject(Vector2f pos, float radius, Vector2f velocity, Texture2D sprite) {
		super(pos, new Vector2f(2*radius, 2*radius), sprite, new Vector3f(1.0f,1.0f,1.0f), velocity); // This has to be placed first
		// Using 2*radius in size to get diameter
		this.Radius = radius;
		this.Stuck = true;
	}
	
	BallObject() {
		super();
		this.Radius = 12.5f;
		this.Stuck = true;
	}
	
	Vector2f Move(float dt,int window_width) {
		if (!this.Stuck) { // If not stuck to paddle (obviously)
			// Moving ball
			Vector2f temp = new Vector2f();
			this.Position.add(this.Velocity.mul(dt, temp)); // Cannot use "+=" with JOML and have to use odd notation like in previous project
			// Checking window bound. If touching edge of screen, reverse velocity and restore at correct position
			if (this.Position.x <= 0.0f) {
				this.Velocity.x = -this.Velocity.x;
				this.Position.x = 0.0f;
			}
			else if (this.Position.x + this.Size.x >= window_width) { // Have to keep in mind size of ball and that origin is top left
				this.Velocity.x = -this.Velocity.x;
				this.Position.x = window_width - this.Size.x;
			}
			if (this.Position.y <= 0.0f) {
				this.Velocity.y = -this.Velocity.y;
				this.Position.y = 0.0f;
			}
		}
		return this.Position;
	}
	
	void Reset(Vector2f position, Vector2f velocity) {
		this.Position = position;
		this.Velocity = velocity;
		this.Stuck = true;		
	}
}
