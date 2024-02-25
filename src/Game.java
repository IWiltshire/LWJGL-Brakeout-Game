/* TO DO
 * Fix reset
 * Level select
 * Assigning background art to each level
 * Powerups
 * Block health -> implement damage texture for blocks
 * Better levels
 */
import java.util.ArrayList;
import static org.lwjgl.glfw.GLFW.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Math;

enum GameState {
	GAME_ACTIVE,
	GAME_MENU,
	GAME_WIN
}

enum Direction {
	UP,
	RIGHT,
	DOWN,
	LEFT
}

public class Game {
	GameState State;
	boolean[] Keys = new boolean[1024];
	int Width, Height;
	SpriteRenderer Renderer;
	ArrayList<GameLevel> Levels = new ArrayList<GameLevel>(); // Have to initialise this in Java and cannot just declare it
	int Level;
	
	// Player setup
	Vector2f PLAYER_SIZE = new Vector2f(100.0f, 20.0f); // Initial size of paddle
	float PLAYER_VELOCITY = 500.0f; // Initial paddle velocity
	GameObject Player;
	// Ball setup
	final Vector2f INITIAL_BALL_VELOCITY = new Vector2f(100.0f, -350.0f);
	float BALL_RADIUS = 12.5f;
	BallObject Ball;
	
	Game(int width, int height) {
		this.Width = width;
		this.Height = height;
		this.State = GameState.GAME_ACTIVE;
	}
	
	void Init() {
		// Load shaders
		ResourceManager.LoadShader("shaders/spriteVertex.glsl",
				"shaders/spriteFragment.glsl",
				"sprite");
		// Configure shaders
		Matrix4f projection = new Matrix4f();
		projection.ortho(0.0f, (float)this.Width, (float)this.Height, 0.0f, -1.0f, 1.0f);
		ResourceManager.GetShader("sprite").Use();
		ResourceManager.GetShader("sprite").SetInteger("image", 0, false); // Bool is unnecessary here since Use() method is used in line above. Seems like "image" is set to 0 to initialise the texture, but this is uneeded.
		ResourceManager.GetShader("sprite").SetMat4f("projection", projection, false);
		// Set render-specific controls
		Renderer = new SpriteRenderer(ResourceManager.GetShader("sprite"));
		// Load textures
		ResourceManager.LoadTexture("sprites/is_ball.png", true, "face");
		ResourceManager.LoadTexture("sprites/is_background_grey.png", false, "background");
		ResourceManager.LoadTexture("sprites/is_block.png", false, "block");
		ResourceManager.LoadTexture("sprites/is_block_solid.png", false, "block_solid");
		ResourceManager.LoadTexture("sprites/is_paddle.png", true, "paddle");
		// Load levels		
		GameLevel one = new GameLevel();
		one.Load("levels/01.lvl", this.Width, this.Height/2);
		GameLevel two = new GameLevel();
		two.Load("levels/02.lvl", this.Width, this.Height/2);
		GameLevel three = new GameLevel();
		three.Load("levels/03.lvl", this.Width, this.Height/2);
		GameLevel four = new GameLevel();
		four.Load("levels/04.lvl", this.Width, this.Height/2);
		this.Levels.add(one);
		this.Levels.add(two);
		this.Levels.add(three);
		this.Levels.add(four);
		// Choosing level
		this.Level = 2; // We traverse through Levels by 0-index
		// Player initialisation (or rather, assignment/instantiation)
		Vector2f playerPos = new Vector2f(0.5f * (this.Width - PLAYER_SIZE.x), this.Height - 1.5f * PLAYER_SIZE.y); // "- 1.25f" included just to move the paddle from the very bottom of the screen
		Player = new GameObject(playerPos, PLAYER_SIZE, ResourceManager.GetTexture("paddle"), new Vector3f(0.0f,1.0f,0.9f), new Vector2f(0f,0f));	
		// Ball initialisation
		Vector2f ballPos = new Vector2f();
		playerPos.add(new Vector2f(PLAYER_SIZE.x / 2.0f - BALL_RADIUS, -BALL_RADIUS * 2.0f), ballPos); // Assigning value to ballPos
		Ball = new BallObject(ballPos, BALL_RADIUS, INITIAL_BALL_VELOCITY, ResourceManager.GetTexture("face"));
	}
	
	void ProcessInput(float deltatime) {
		if (this.State == GameState.GAME_ACTIVE) {
			float velocity = PLAYER_VELOCITY * Window.deltaTime; // Velocity is multiplied by deltatime (presumably for equivalent movement on all machines)
			// Movement
			if (this.Keys[GLFW_KEY_A] || this.Keys[GLFW_KEY_LEFT]) {
				if (Player.Position.x >= 0.0f) {
					Player.Position.x -= velocity;
					if (Ball.Stuck) {
						Ball.Position.x -= velocity;
					}
				}
			}
			if (this.Keys[GLFW_KEY_D] || this.Keys[GLFW_KEY_RIGHT]) { // Makes no difference if I have "if" or "else if" here.
				if (Player.Position.x <= this.Width - Player.Size.x) { // Have to subtract width of player paddle because sprite is centered in top left
					Player.Position.x += velocity;
					if (Ball.Stuck) {
						Ball.Position.x += velocity;
					}
				}
			}
			// Freeing ball
			if (this.Keys[GLFW_KEY_SPACE]) {
				Ball.Stuck = false;
			}
			// Quick reset
			if (this.Keys[GLFW_KEY_R]) {
				this.ResetLevel();
				this.ResetPlayer();
			}
		}

	}
	
	void Update(float deltatime) {
		Ball.Move(deltatime, this.Width);
		this.DoCollisions(); // Performing collisions
		if (Ball.Position.y > this.Height) {
			this.ResetLevel();
			this.ResetPlayer();
		}
	}
	
	void Render() {		
		//ResourceManager.listShaders(); // It seems that shaders and textures are being stored properly
		//System.out.println(Renderer.shader); Refers to same shader as above -> good!
		
		// Draw background
		// Alternative to the current method, I could make unique backgrounds for each level
		if (this.State == GameState.GAME_ACTIVE) {
			Renderer.DrawSprite(ResourceManager.GetTexture("background"),
					new Vector2f(0.0f, 0.0f),
					new Vector2f(this.Width, this.Height),
					0.0f,
					new Vector3f(0.797f, 0.797f, 0.99f));
		}
		// Draw current level
		this.Levels.get(this.Level).Draw(Renderer);
		// Draw player
		this.Player.Draw(Renderer);
		// Draw ball
		this.Ball.Draw(Renderer);
	}
	
	boolean CheckCollision(GameObject one, GameObject two) { // Collisions between AABBs
		// x-axis
		boolean collisionX = one.Position.x + one.Size.x >= two.Position.x // right side of one geq than left side of two
				&& two.Position.x + two.Size.x >= one.Position.x; // right side of two geq left side of one
		// y-axis
		boolean collisionY = one.Position.y + one.Size.y >= two.Position.y // bottom side of one geq (lower) than top side of two
				&& two.Position.y + two.Size.y >= one.Position.y; // bottom side of two lower than top side of one
		
		return collisionX && collisionY;
	}
	
	Object[] CheckCollision(BallObject one, GameObject two) { // Ball and AABB collision
		// Centre of circle
		Vector2f centre = new Vector2f();
		one.Position.add(one.Radius, one.Radius, centre);
		// Calculate AABB information (centre, half extents)
		Vector2f aabb_half_extents = new Vector2f(0.5f*two.Size.x, 0.5f*two.Size.y);
		Vector2f aabb_centre = new Vector2f(
				two.Position.x + aabb_half_extents.x,
				two.Position.y + aabb_half_extents.y
				);
		// Difference between centres
		Vector2f difference = new Vector2f();
		centre.sub(aabb_centre, difference);
		
		Vector2f temp = new Vector2f();
		Vector2f clamped = clamp(difference, aabb_half_extents.mul(-1, temp), aabb_half_extents); // Have to use my own function here since JOML lacks clamp for vectors (why?!; annoying)
		// Add clamped value to AABB_centre and get value of box closest to circle (maths behind this is confusing -> check later)
		Vector2f closest = new Vector2f();
		aabb_centre.add(clamped, closest);
		// Get new distance (difference) vector and check length against radius
		closest.sub(centre, difference);
		
		Object[] res = new Object[3]; // Opted to use an array object rather than import tuples library
		if (difference.length() < one.Radius) {
			res[0] = true;
			res[1] = VectorDirection(difference);
			res[2] = difference;
		}
		else {
			res[0] = false;
			res[1] = Direction.UP;
			res[2] = new Vector2f(0.0f, 0.0f);
		}
		return res;
	}
	Vector2f clamp(Vector2f target, Vector2f min, Vector2f max) {
		Vector2f res = new Vector2f();
		res.x = Math.max(min.x, Math.min(max.x, target.x));
		res.y = Math.max(min.y, Math.min(max.y, target.y));
		return res; // Unlike the JOML functions, my custom clamp returns a new vector rather than altering the inputted one
	}
	
	void DoCollisions() {
		for (GameObject box: this.Levels.get(this.Level).Bricks) {
			if (!box.Destroyed) {
				Object[] collision = CheckCollision(Ball, box);
				if ((boolean) collision[0]) {
					if (!box.IsSolid) { // destroy block if not solid
						box.Destroyed = true;
					}
					// Collision resolution, i.e physical results from colliding
					Direction dir = (Direction) collision[1]; // Would using the tuple library have saved me from the need to cast things again and again?
					Vector2f diff_vector = (Vector2f) collision[2];
					if (dir == Direction.LEFT || dir == Direction.RIGHT) { // horizontal collision
						Ball.Velocity.x = -Ball.Velocity.x; // reverse horizontal movement
						// Relocate
						float penetration = Ball.Radius - Math.abs(diff_vector.x); // overlap of ball into block
						if (dir == Direction.LEFT) {
							Ball.Position.x += penetration; // If ball was moving to the left (i.e. hit right side of block), move the ball back out to the right - eliminating the overlap					
						}
						else {
							Ball.Position.x -= penetration; // Converse of above: move ball to the left
						}
					}
					else { // vertical collision
						Ball.Velocity.y = -Ball.Velocity.y; // Inverse vertical velocity
						// Relocate
						float penetration = Ball.Radius - Math.abs(diff_vector.y);
						if (dir == Direction.UP) {
							Ball.Position.y -= penetration; // Move ball back up?! Testing shows that this is right, but I'm not sure why
						}
						else {
							Ball.Position.y += penetration; // Move ball back down
						}
					}
				}
			}
		}
		// Checking collisions for ball and paddle if ball is not stuck
		Object[] result = CheckCollision(Ball, Player);
		if (!Ball.Stuck && (boolean) result[0]) {
			// Check where ball hit paddle (board). Impacts further from centre of paddle gain more horizontal velocity
			float centreBoard = Player.Position.x + (Player.Size.x * 0.5f);
			float distance = (Ball.Position.x + Ball.Radius) - centreBoard;
			float percentage = Math.abs(distance) / (Player.Size.x * 0.5f); // Put distance in Math.abs() to prevent unwanted reversal of x-velocity when hitting left side of paddle
			percentage = org.joml.Math.clamp(0.2f, 0.45f, percentage); // Clamping percentage so that ball does not go vertical (0) or too horizontal (1).
			float strength = 3.0f; // This impacts to what extent percentage changes velocity
			// Move
			Vector2f oldVelocity = new Vector2f(Ball.Velocity); // Passing old velocity into new Vector2f(). There would otherwise be a problem, perhaps because they would otherwise be referring to the same address?
			Ball.Velocity.x = oldVelocity.x * percentage * strength; // Using oldVelocity instead of INITIAL_BALL_VELOCITY allows for a negative initial x-component without causing a bug in this calculation.
			Ball.Velocity.normalize().mul(oldVelocity.length()); // Velocity is normalised and then scaled to length (magnitude) of original velocity vector. In this way, speed stays constant.
			// Fix "sticky paddle"
			Ball.Velocity.y = -1.0f * Math.abs(Ball.Velocity.y); // Movement of ball once it hits paddle is always upwards. This avoid bugs where the ball might end up inside the paddle and get stuck constantly flipping vertical direction
			// Moving ball to top of paddle
			Ball.Position.y = Player.Position.y - 2*Ball.Radius; // Remember top left is (0,0). This leads to odd behaviour if the side of the paddle hits the ball, but I think it's fine.
		}
	}
	
	Direction VectorDirection(Vector2f target) {
		// If ball collides with right or left side of block, horizontal velocity is reversed
		// If ball collides with bottom or top of AABB, vertical velocity is reversed
		// Use dot product to find collision direction. With unit vectors, dot product has upper bound of 1 for when angle between direction vector and ordinal vector is 0
		// => look for the ordinal where dot product is maximised
		
		Vector2f[] compass = {
				new Vector2f(0.0f, 1.0f), // up
				new Vector2f(1.0f, 0.0f), // right
				new Vector2f(0.0f, -1.0f), // down
				new Vector2f(-1.0f, 0.0f) // left
		};
		float max = 0.0f;
		int best_match = 3; // Using 3 instead of -1 because Java
		for (int i=0; i<4; i++) {
			float dot_product;
			Vector2f temp = new Vector2f();
			dot_product = target.normalize(temp).dot(compass[i]);
			if (dot_product > max) {
				max = dot_product;
				best_match = i;
			}
		}
		return Direction.values()[best_match];
	}
	
	void ResetLevel() {
		if (this.Level == 0) {
			this.Levels.get(0).Load("levels/01.lvl", this.Width, this.Height/2);
		}
		else if (this.Level == 1) {
			this.Levels.get(1).Load("levels/02.lvl", this.Width, this.Height/2);
		}
		else if (this.Level == 2) {
			this.Levels.get(2).Load("levels/03.lvl", this.Width, this.Height/2);
		}
		else if (this.Level == 3) {
			this.Levels.get(3).Load("levels/04.lvl", this.Width, this.Height/2);
		}	
	}
	
	void ResetPlayer() {
		Player.Size = PLAYER_SIZE;
		Player.Position = new Vector2f(0.5f * (this.Width - PLAYER_SIZE.x), this.Height - 1.5f * PLAYER_SIZE.y);
		Vector2f temp = new Vector2f();
		Player.Position.add(new Vector2f(0.5f * PLAYER_SIZE.x - BALL_RADIUS, -BALL_RADIUS * 2.0f), temp);
		
		Ball.Reset(temp, new Vector2f(100.0f, -350.0f)); // Issue seems to be fixed by passing (100,-350) instead of INITIAL_BALL_VELOCITY, but I need to test more later.
	}
}
