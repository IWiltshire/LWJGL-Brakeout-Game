import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class GameLevel {
	// level state
	ArrayList<GameObject> Bricks = new ArrayList<GameObject>();
	
	GameLevel() {}
	
	void Load(String file, int levelWidth, int levelHeight) {
		Bricks.clear(); // Starting from a fresh slate
		
		// load from file
		int tileCode;
		GameLevel level;
		String line;
		ArrayList<ArrayList<Integer>> tileData = new ArrayList<>(); // Rows will be rows of bricks, i.e. width; columns tell height of level
		
		try(Scanner scanner = new Scanner(new File(file))) {
			while (scanner.hasNextLine()) {
				ArrayList<Integer> row = new ArrayList<Integer>();
				// ---- TO DO
				line = scanner.nextLine(); // ".nextLine()" immediately gives the first line rather than starting with 0 or some other placeholder.
				String[] lineS = line.split(" ");
				for (String i: lineS) row.add(Integer.parseInt(i));
				// ----
				tileData.add(row);
			}
			if (tileData.size() > 0) {
				this.init(tileData, levelWidth, levelHeight);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//System.out.println(tileData.get(0)); // Yay! Everything works!
	}
	
	void Draw(SpriteRenderer renderer) { // Draws all bricks in level
		for (GameObject tile: this.Bricks) { // Terminology has suddenly changed from bricks, to blocks, to tiles
			if (!tile.Destroyed) {
				tile.Draw(renderer);
			}
		}
	}
	
	boolean IsCompleted() { // Checks if all bricks in level destroyed
		for (GameObject tile: this.Bricks) {
			if (!tile.Destroyed && !tile.IsSolid) { // Remember && is logical AND
				return false;
			}
		}
		return true;
	}
	
	private void init(ArrayList<ArrayList<Integer>> tileData, int lvlWidth, int lvlHeight) {
		// calculate dimensions
		int height = tileData.size();
		int width = tileData.get(0).size();
		float unit_width = (float) lvlWidth / width;
		float unit_height = (float) lvlHeight / height;
		
		// initialise level tiles based on tileData
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				// Checking block type from level data (for colour atm; change to helath later)
				if (tileData.get(y).get(x) == 1) { // solid brick
					Vector2f pos = new Vector2f(unit_width * x, unit_height * y);
					Vector2f size = new Vector2f(unit_width, unit_height);
					GameObject obj = new GameObject(pos,
							size,
							ResourceManager.GetTexture("block_solid"),
							new Vector3f(0.8f, 0.8f, 0.7f),
							new Vector2f(0f,0f));
					obj.IsSolid = true;
					this.Bricks.add(obj);
				}
				else if (tileData.get(y).get(x) > 1) {
					Vector3f colour = new Vector3f(1.0f);
					if (tileData.get(y).get(x) == 2) {
						colour = new Vector3f(0.2f, 0.6f, 1.0f);
					}
					else if (tileData.get(y).get(x) == 3) {
						colour = new Vector3f(0.0f, 0.7f, 0.0f);
					}
					else if (tileData.get(y).get(x) == 4) {
						colour = new Vector3f(0.8f, 0.8f, 0.4f);
					}
					else if (tileData.get(y).get(x) == 5) {
						colour = new Vector3f(1.0f, 0.5f, 0.0f);
					}
					
					Vector2f pos = new Vector2f(unit_width * x, unit_height * y); // Makes sense to just define these outside of the loops
					Vector2f size = new Vector2f(unit_width, unit_height);
					
					GameObject obj = new GameObject(pos,
							size,
							ResourceManager.GetTexture("block"),
							colour,
							new Vector2f(0f,0f));
					
					this.Bricks.add(obj);
				}
			}
		}
	}
}
