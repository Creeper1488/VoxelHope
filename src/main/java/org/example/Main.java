package org.example;

import org.joml.Matrix4f;
import org.joml.sampling.BestCandidateSampling;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

import java.nio.*;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // The window handle
    private long window;

    private List<Cube> cubes = new ArrayList<>();
    private Cube currentCube;
    private boolean rotating = false;
    private int type_bg = 1;
    private boolean F3_change = false;
    private boolean T_change = false;


    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwTerminate();

// Безопасное освобождение callback - используем другое имя переменной
        GLFWErrorCallback errorCallback = glfwSetErrorCallback(null);
        if (errorCallback != null) {
            errorCallback.free();
        }
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit() )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(1920, 1080, "VoxelLand: lagva edition", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        currentCube = new Cube(0, 0, -5, 0, false, 2);

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch(key) {
                    case GLFW_KEY_UP -> currentCube.y += 0.05f;
                    case GLFW_KEY_DOWN -> currentCube.y -= 0.05f;
                    case GLFW_KEY_LEFT -> currentCube.x -= 0.05f;
                    case GLFW_KEY_RIGHT -> currentCube.x += 0.05f;
                    case GLFW_KEY_W -> currentCube.z += 0.05f;
                    case GLFW_KEY_S -> currentCube.z -= 0.05f;
                    case GLFW_KEY_R -> rotating = !rotating;
                    case GLFW_KEY_1 -> currentCube.type = 1;
                    case GLFW_KEY_2 -> currentCube.type = 2;
                    case GLFW_KEY_3 -> currentCube.type = 3;
                    case GLFW_KEY_4 -> currentCube.type = 4;
                    case GLFW_KEY_5 -> currentCube.type = 5;
                    case GLFW_KEY_ESCAPE -> System.out.print("ESC нажата!");
                    case GLFW_KEY_F3 -> {
                        if (!F3_change) {
                            System.out.print("F3 нажата!");
                            F3_change = !F3_change;
                        }
                        else
                            System.out.print("F3 отпущена!");
                    }
                    case GLFW_KEY_T -> {
                        if(T_change) {
                            T_change = !T_change;
                            type_bg = 1;
                        }
                        else {
                            T_change = !T_change;
                            type_bg = 2;
                        }
                    }
                    case GLFW_KEY_ENTER -> {
                        currentCube.is_placed = true;
                        cubes.add(currentCube);
                        switch(currentCube.type) {
                            case 1 -> currentCube = new Cube(0, 0, -5, 0, false, 1);
                            case 2 -> currentCube = new Cube(0, 0, -5, 0, false, 2);
                            case 3 -> currentCube = new Cube(0,0, -5, 0, false, 3);
                            case 4 -> currentCube = new Cube(0, 0, -5, 0, false, 4);
                            case 5 -> currentCube = new Cube(0,0, -5, 0, false, 5);
                        }
                    }
                }
            }
        });
        switch(currentCube.type) {
            case 1 -> currentCube = new Cube(0, 0, -5, 0, false, 1);
            case 2 -> currentCube = new Cube(0, 0, -5, 0, false, 2);
            case 3 -> currentCube = new Cube(0, 0, -5, 0, false, 3);
            case 4 -> currentCube = new Cube(0, 0, -5, 0, false, 4);
        }
        for(int row = 0; row < 40; row++) {
            float z = -4.1f - row * 0.3f;
            for(int i = 0; i < 80; i++){
                float x = -10f + i * 0.3f;
                cubes.add(new Cube(x, -1.7f, z, 0, true, 2));
            }
        }
        float distantZ = -4.1f - 39 * 0.3f;
        for(int i = 0; i < 80; i++) {
            float x = -10f + i * 0.3f;
            cubes.add(new Cube(x, -1.4f, distantZ, 0, true, 2));
        }
        // Get the thread stack and push a new frame
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }
    private void gradientBg(int type) {
        if(type == 1) {
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0, 1920, 0, 1080, -1, 1);


            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_DST_ALPHA);

            glDepthMask(false);


            glBegin(GL_QUADS);


            glColor3f(135 / 255f, 206 / 255f, 235 / 255f);
            glVertex2f(0, 0);
            glVertex2f(1920, 0);

            glColor3f(0 / 255f, 191 / 255f, 255 / 255f);
            glVertex2f(1920, 1080);
            glVertex2f(0, 1080);

            glEnd();

            glDepthMask(true);
            glDisable(GL_BLEND);

            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
        }
        else {
            glMatrixMode(GL_PROJECTION);
            glPushMatrix();
            glLoadIdentity();
            glOrtho(0, 1920, 0, 1080, -1, 1);


            glMatrixMode(GL_MODELVIEW);
            glPushMatrix();
            glLoadIdentity();

            glEnable(GL_BLEND);
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_DST_ALPHA);

            glDepthMask(false);


            glBegin(GL_QUADS);


            glColor3f(42 / 255f, 42 / 255f, 53 / 255f);
            glVertex2f(0, 0);
            glVertex2f(1920, 0);

            glColor3f(19 / 255f,24 / 255f,98 / 255f);
            glVertex2f(1920, 1080);
            glVertex2f(0, 1080);

            glEnd();

            glDepthMask(true);
            glDisable(GL_BLEND);

            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
        }
    }

    private void loop() {
        GL.createCapabilities();

        glEnable(GL_DEPTH_TEST);

        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0),
                1920.0f / 1080.0f, 0.1f, 100f);
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        projection.get(fb);

        // Set the clear color
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(fb);
            if(type_bg == 1)
                gradientBg(1);
            else
                gradientBg(2);
            draw_sun();

            for (Cube cube : cubes) {
                switch(cube.type) {
                    case 2 -> drawCubedirt(cube);
                    case 1 -> drawODNOTON(cube, 120, 20, 0);
                    case 3 -> drawODNOTON(cube, 194, 178, 128);
                    case 4 -> draw_dub(cube);
                    case 5 -> drawODNOTON(cube, 92, 169, 4);
                }
            }
            switch(currentCube.type) {
                case 1 -> drawODNOTON(currentCube, 120, 20, 0);
                case 2 -> drawCubedirt(currentCube);
                case 3 -> drawODNOTON(currentCube, 194, 178, 128);
                case 4 -> draw_dub(currentCube);
                case 5 -> drawODNOTON(currentCube, 92, 169, 4);
            }

            if(rotating)
                currentCube.angle += 0.5f;



            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }
    private void drawCubedirt(Cube cube) {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(cube.x, cube.y, cube.z);
        glRotatef(cube.angle, 0f, 1f, 0f);
        glScalef(0.33f, 0.33f, 0.33f);

        glBegin(GL_QUADS);
        //front
        glColor3f(120 / 255f, 20 / 255f, 0 / 255f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        //back
        glColor3f(120 / 255f, 20 / 255f, 0 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        //top face
        glColor3f(97 / 255f, 255 / 255f, 0 / 255f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        //bottom face
        glColor3f(120 / 255f, 20 / 255f, 0 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        //right face
        glColor3f(120 / 255f, 20 / 255f, 0 / 255f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);

        //left face
        glColor3f(120 / 255f, 20 / 255f, 0 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glEnd();
    }
    private void drawODNOTON(Cube cube, float R, float G, float B) {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(cube.x, cube.y, cube.z);
        glRotatef(cube.angle, 0f, 1f, 0f);
        glScalef(0.33f, 0.33f, 0.33f);

        glBegin(GL_QUADS);
        //front
        glColor3f(R / 255f, G / 255f, B / 255f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        //back
        glColor3f(R / 255f, G / 255f, B / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        //top face
        glColor3f(R / 255f, G / 255f, B / 255f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        //bottom face
        glColor3f(R / 255f, G / 255f, B / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        //right face
        glColor3f(R / 255f, G / 255f, B / 255f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);

        //left face
        glColor3f(R / 255f, G / 255f, B / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glEnd();
    }
    private void draw_dub(Cube cube) {
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glTranslatef(cube.x, cube.y, cube.z);
        glRotatef(cube.angle, 0f, 1f, 0f);
        glScalef(0.33f, 0.33f, 0.33f);

        glBegin(GL_QUADS);
        //front
        glColor3f(150 / 255f, 111 / 255f, 51 / 255f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        //back
        glColor3f(150 / 255f, 111 / 255f, 51 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        //top face
        glColor3f(194 / 255f, 178 /255f, 128 / 255f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        //bottom face
        glColor3f(194 / 255f, 178 /255f, 128 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        //right face
        glColor3f(150 / 255f, 111 / 255f, 51 / 255f);
        glVertex3f(0.5f, -0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, -0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        //left face
        glColor3f(150 / 255f, 111 / 255f, 51 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glEnd();
    }
    private void draw_sun() {
        IntBuffer widthBuffer = BufferUtils.createIntBuffer(1);
        IntBuffer heightBuffer = BufferUtils.createIntBuffer(1);
        glfwGetWindowSize(window, widthBuffer, heightBuffer);
        int windowWidth = widthBuffer.get(0);
        int windowHeight = heightBuffer.get(0);

        // Вычисляем viewport с фиксированным соотношением 4:3
        int viewportWidth, viewportHeight;
        int viewportX, viewportY;

        float targetAspect = 4.0f / 3.0f;
        float aspect = (float)windowWidth / windowHeight;

        if (aspect > targetAspect) {
            // Окно шире целевого соотношения
            viewportHeight = windowHeight;
            viewportWidth = (int)(windowHeight * targetAspect);
            viewportX = (windowWidth - viewportWidth) / 2;
            viewportY = 0;
        } else {
            // Окно уже целевого соотношения
            viewportWidth = windowWidth;
            viewportHeight = (int)(windowWidth / targetAspect);
            viewportX = 0;
            viewportY = (windowHeight - viewportHeight) / 2;
        }

        glViewport(viewportX, viewportY, viewportWidth, viewportHeight);

        // Теперь используем стандартную ортографическую проекцию
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 800, 0, 600, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false);

        // Квадратное солнце
        float size = 80f;
        float centerX = 400f;
        float centerY = 450f;

        glColor3f(1.0f, 1.0f, 0.0f);
        glBegin(GL_QUADS);
        glVertex2f(centerX - size/2, centerY - size/2);
        glVertex2f(centerX + size/2, centerY - size/2);
        glVertex2f(centerX + size/2, centerY + size/2);
        glVertex2f(centerX - size/2, centerY + size/2);
        glEnd();

        glDepthMask(true);
        glDisable(GL_BLEND);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        // Восстанавливаем полный viewport для 3D-рендеринга
        glViewport(0, 0, windowWidth, windowHeight);
    }

    public static void main(String[] args) {
        new Main().run();
    }
}