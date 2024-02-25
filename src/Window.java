import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.nio.*;
import org.lwjgl.BufferUtils;
import java.lang.Math;
import org.joml.*; // Use for vectors and matrices

import static org.lwjgl.BufferUtils.*; // Direct buffers only!
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*; // This corresponds to OpenGL ver 3.3
import static org.lwjgl.system.MemoryUtil.NULL; // Needed for GLFW functions

class Window {
    static long window;
    static int SCR_WIDTH = 800, SCR_HEIGHT = 600;
    String title = "Isaac's Breakout Game";
    
    // Camera
    static float lastX = SCR_WIDTH/2.0f; // Width and height need to have been instantiated prior for this to make sense.
    static float lastY = SCR_HEIGHT/2.0f;
    static boolean firstMouse = true;

    // Timing
    static float deltaTime = 0.0f, lastTime = 0.0f;
    
    // Game
    static Game Breakout = new Game(SCR_WIDTH, SCR_HEIGHT); 
    

    void init() {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE); // Forward compatibility
    
        window = glfwCreateWindow(Window.SCR_WIDTH, Window.SCR_HEIGHT, title, NULL, NULL); 
        if (window == NULL) {
        	throw new IllegalStateException("Failed to create GLFW window");
        }
        glfwMakeContextCurrent(window);

        GL.createCapabilities(); 
        
        // Framebuffer size callback
        Resize framebuffer_size_callback = new Resize();
        glfwSetFramebufferSizeCallback(window, framebuffer_size_callback);
        
        // Key callback
        Keyboard keyCallback = new Keyboard();
        glfwSetKeyCallback(window, keyCallback);
        
        // Might not need mouse for this project
        //Mouse mousePosCallback = new Mouse();
        //glfwSetCursorPosCallback(window, mousePosCallback);
        //glfwSetCursorPos(window, Window.lastX, Window.lastY); // Defaulting mouse position to centre of screen 
        
        // OpenGL config
        glViewport(0,0,SCR_WIDTH,SCR_HEIGHT); // Top left of window is (0,0)
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glfwSwapInterval(1); // V-sync
        // Z-buffer not needed as game is 2D
        
        // Getting singleton here as a precaution since it's involved in making game
        ResourceManager.getInstance(); // Is this a nice place to have this, or should I put it in Game.Init()?
        
        Breakout.Init();

    }

    void loop() {
        while (!glfwWindowShouldClose(window)) {
            float currentFrame = (float) glfwGetTime();
            deltaTime = currentFrame - lastTime;
            lastTime = currentFrame; // Should this be here and not the bottom of the loop?
            glfwPollEvents();
    
            // User input
            Breakout.ProcessInput(deltaTime);
            
            // Update game state
            Breakout.Update(deltaTime);            
                       
            // Render
            glClearColor(0.8f, 0.7f, 0.7f, 1.0f);
            glClear(GL_COLOR_BUFFER_BIT);
            Breakout.Render(); 
            
            glfwSwapBuffers(window);
           }          
    }
    
    void run() {
        init(); 
        loop();
        ResourceManager.Clear(); 
        glfwTerminate();
    }

}

// Old code from previous project. Re-implement later when menu is set up?
//class Mouse extends GLFWCursorPosCallback {
//    public void invoke(long window, double xPosIn, double yPosIn) {
//        float xPos = (float) xPosIn;
//        float yPos = (float) yPosIn;
//        
//        if (Window.firstMouse) {
//            Window.lastX = xPos;
//            Window.lastY = yPos;
//            Window.firstMouse = false;
//        }
//
//        float xOffset = xPos - Window.lastX;
//        float yOffset = Window.lastY - yPos; // Reverse order here because screen coordinate starts with top left as (0,0) and bottom left as (0,1)
//
//        Window.lastX = xPos;
//        Window.lastY = yPos;
//
//   }
//}

class Resize extends GLFWFramebufferSizeCallback { // Callback method has to be defined in its own class so that it can be invoked properly
    public void invoke(long window, int width, int height) {
        glViewport(0,0,width,height);
    }
}

class Keyboard extends GLFWKeyCallback {
	@Override
	public void invoke(long window, int key, int scancode, int action, int mode) {
		if (key==GLFW_KEY_ESCAPE && action == GLFW_PRESS) { // Exit on escape
			glfwSetWindowShouldClose(window, true);
		}
		
		if (key>=0 && key<1024) {
			if (action == GLFW_PRESS) {
				Window.Breakout.Keys[key] = true;
			}
			else if (action == GLFW_RELEASE) {
				Window.Breakout.Keys[key] = false;
			}
		}
		
	}
}