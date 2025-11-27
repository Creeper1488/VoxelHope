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
import java.util.Random;

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
    private List<Entity> entities = new ArrayList<>();
    private float camX = 0.0f, camY = 0.0f, camZ = 5.0f;
    private float camPitch = 0.0f, camYaw = 0.0f;
    private boolean firstMouse = true;
    private double lastMouseX, lastMouseY;
    private MusicPlayer musicPlayer;



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

        // Create the window
        window = glfwCreateWindow(1920, 1080, "VoxelHope", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        currentCube = new Cube(0, 0, -5, 0, false, 2);
        musicPlayer = new MusicPlayer();
        musicPlayer.playMusic();

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if(action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch(key) {
                    case GLFW_KEY_W -> moveCamera(-0.1f, 0, 0);    // Вперед
                    case GLFW_KEY_S -> moveCamera(0.1f, 0, 0);   // Назад
                    case GLFW_KEY_A -> moveCamera(0, 0, -0.1f);    // Влево
                    case GLFW_KEY_D -> moveCamera(0, 0, 0.1f);   // Вправо
                    case GLFW_KEY_SPACE -> moveCamera(0, 0.1f, 0);    // Вверх
                    case GLFW_KEY_LEFT_SHIFT -> moveCamera(0, -0.1f, 0); // Вниз
                    case GLFW_KEY_UP -> currentCube.y += 0.05f;
                    case GLFW_KEY_DOWN -> currentCube.y -= 0.05f;
                    case GLFW_KEY_LEFT -> currentCube.x -= 0.05f;
                    case GLFW_KEY_RIGHT -> currentCube.x += 0.05f;
                    case GLFW_KEY_O -> currentCube.z += 0.05f;
                    case GLFW_KEY_L -> currentCube.z -= 0.05f;
                    case GLFW_KEY_R -> rotating = !rotating;
                    case GLFW_KEY_1 -> currentCube.type = 1;
                    case GLFW_KEY_2 -> currentCube.type = 2;
                    case GLFW_KEY_3 -> currentCube.type = 3;
                    case GLFW_KEY_4 -> currentCube.type = 4;
                    case GLFW_KEY_5 -> currentCube.type = 5;
                    case GLFW_KEY_6 -> currentCube.type = 6;
                    case GLFW_KEY_7 -> currentCube.type = 7;
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
                            case 6 -> currentCube = new Cube(0,0, -5, 0, false, 6);
                            case 7 -> currentCube = new Cube(0,0, -5, 0, false, 7);
                        }
                    }
                }
            }
        });
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }

            float xoffset = (float)(xpos - lastMouseX);
            float yoffset = (float)(lastMouseY - ypos);
            lastMouseX = xpos;
            lastMouseY = ypos;

            float sensitivity = 0.1f;
            xoffset *= sensitivity;
            yoffset *= sensitivity;

            camYaw += xoffset;
            camPitch += yoffset;

            if (camPitch > 89.0f) camPitch = 89.0f;
            if (camPitch < -89.0f) camPitch = -89.0f;
        });
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            if (yoffset > 0) {
                currentCube.type = Math.min(7, currentCube.type + 1);
            } else {
                currentCube.type = Math.max(1, currentCube.type - 1);
            }
        });

        // Захватываем курсор
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        switch(currentCube.type) {
            case 1 -> currentCube = new Cube(0, 0, -5, 0, false, 1);
            case 2 -> currentCube = new Cube(0, 0, -5, 0, false, 2);
            case 3 -> currentCube = new Cube(0, 0, -5, 0, false, 3);
            case 4 -> currentCube = new Cube(0, 0, -5, 0, false, 4);
            case 5 -> currentCube = new Cube(0, 0, -5, 0, false, 5);
            case 6 -> currentCube = new Cube(0, 0, -5, 0, false, 6);
            case 7 -> currentCube = new Cube(0, 0, -5, 0, false, 7);
        }
       Random rand = new Random();
        int seed = rand.nextInt();

        for(int x = 0; x < 80; x++) {
            for(int z = 0; z < 40; z++) {
                // Получаем высоту из шума
                float heightNoise = (float) (
                        Math.sin(x * 0.1 + seed) * Math.cos(z * 0.08 + seed) * 1.5 +
                                Math.sin(x * 0.05 + z * 0.03 + seed * 2) * 0.8
                );

                int blockHeight = 2 + (int)(heightNoise * 1.5); // высота от 1 до 4 блоков
                if(blockHeight < 1) blockHeight = 1;
                if(blockHeight > 4) blockHeight = 4;

                // Генерация блоков для этой колонки
                for(int h = 0; h < blockHeight; h++) {
                    float worldX = -10f + x * 0.3f;
                    float worldZ = -4.1f - z * 0.3f;
                    float worldY = -1.7f + h * 0.3f;

                    cubes.add(new Cube(worldX, worldY, worldZ, 0, true,
                            h == blockHeight - 1 ? 2 : 1)); // трава сверху, земля снизу
                }

                // РЕДКИЕ деревья - только 2% шанс
                if(blockHeight >= 2 && rand.nextInt(50) == 0) { // всего 2% шанс
                    addTree(-10f + x * 0.3f, -1.7f + (blockHeight - 1) * 0.3f, -4.1f - z * 0.3f);
                }
            }
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
    private void moveCamera(float forward, float up, float right) {
        // Учитываем направление взгляда камеры
        float yawRad = (float)Math.toRadians(camYaw);

        // Движение вперед/назад и влево/вправо с учетом направления
        camX += forward * (float)Math.sin(yawRad) + right * (float)Math.cos(yawRad);
        camZ += forward * (float)Math.cos(yawRad) - right * (float)Math.sin(yawRad);
        camY += up;
    }
    private void gradientBg(int type) {
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

        if(type == 1) {
            glColor3f(135 / 255f, 206 / 255f, 235 / 255f);
            glVertex2f(0, 0);
            glVertex2f(1920, 0);

            glColor3f(0 / 255f, 191 / 255f, 255 / 255f);
            glVertex2f(1920, 1080);
            glVertex2f(0, 1080);
        }
        else {
            glColor3f(42 / 255f, 42 / 255f, 53 / 255f);
            glVertex2f(0, 0);
            glVertex2f(1920, 0);

            glColor3f(19 / 255f,24 / 255f,98 / 255f);
            glVertex2f(1920, 1080);
            glVertex2f(0, 1080);
        }

            glEnd();

            glDepthMask(true);
            glDisable(GL_BLEND);

            glPopMatrix();
            glMatrixMode(GL_PROJECTION);
            glPopMatrix();
            glMatrixMode(GL_MODELVIEW);
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

        double lastTime = glfwGetTime();
        int frames = 0;
        double fps = 0;

        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            double currentTime = glfwGetTime();
            frames++;
            if (currentTime - lastTime >= 1.0) {
                fps = frames;
                frames = 0;
                lastTime = currentTime;
            }

            glMatrixMode(GL_PROJECTION);
            glLoadMatrixf(fb);
            if(type_bg == 1)
                gradientBg(1);
            else
                gradientBg(2);
            glMatrixMode(GL_MODELVIEW);
            glLoadIdentity();

            // Применяем преобразования камеры
            glRotatef(camPitch, 1.0f, 0.0f, 0.0f);
            glRotatef(camYaw, 0.0f, 1.0f, 0.0f);
            glTranslatef(-camX, -camY, -camZ);
            if (F3_change) {
                renderDebugHUD(fps);
            }
            draw_sun();


            for (Cube cube : cubes) {
                switch(cube.type) {
                    case 2 -> drawCubedirt(cube);
                    case 1 -> drawODNOTON(cube, 120, 20, 0);
                    case 3 -> drawODNOTON(cube, 194, 178, 128);
                    case 4 -> draw_dub(cube);
                    case 5 -> drawODNOTON(cube, 92, 169, 4);
                    case 6 -> drawODNOTON(cube, 255, 165, 79);
                    case 7 -> drawODNOTON(cube, 173, 165, 135);
                }
            }
            switch(currentCube.type) {
                case 1 -> drawODNOTON(currentCube, 120, 20, 0);
                case 2 -> drawCubedirt(currentCube);
                case 3 -> drawODNOTON(currentCube, 194, 178, 128);
                case 4 -> draw_dub(currentCube);
                case 5 -> drawODNOTON(currentCube, 92, 169, 4);
                case 6 -> drawODNOTON(currentCube, 255, 165, 79);
                case 7 -> drawODNOTON(currentCube, 173, 165, 135);
            }

            if(rotating)
                currentCube.angle += 0.5f;


            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }
    private void renderDebugHUD(double fps) {
        // Сохраняем текущие матрицы
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, 1920, 0, 1080, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        // Отключаем тест глубины для HUD
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Рисуем полупрозрачный фон для текста
        glColor4f(0.0f, 0.0f, 0.0f, 0.7f); // Черный с прозрачностью
        glBegin(GL_QUADS);
        glVertex2f(5, 1075);
        glVertex2f(400, 1075);
        glVertex2f(400, 950);
        glVertex2f(5, 950);
        glEnd();

        // Рисуем белые прямоугольники как "текст"
        glColor3f(1.0f, 1.0f, 1.0f); // Белый цвет

        float y = 1060f;

        // VoxelHope Alpha 0.0.4
        drawDebugTextLine(10, y, 250, 15);
        y -= 20f;

        // Camera coordinates
        drawDebugTextLine(10, y, 200, 15);
        y -= 20f;

        // Rotation
        drawDebugTextLine(10, y, 180, 15);
        y -= 20f;

        // Blocks count
        drawDebugTextLine(10, y, 120, 15);
        y -= 20f;

        // FPS
        drawDebugTextLine(10, y, 80, 15);

        // Восстанавливаем настройки
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
    }

    private void drawDebugTextLine(float x, float y, float width, float height) {
        glBegin(GL_QUADS);
        glVertex2f(x, y);
        glVertex2f(x + width, y);
        glVertex2f(x + width, y - height);
        glVertex2f(x, y - height);
        glEnd();
    }
    private void addTree(float x, float y, float z) {
        // Ствол дерева (3 блока высотой)
        cubes.add(new Cube(x, y + 0.3f, z, 0, true, 4)); // дуб
        cubes.add(new Cube(x, y + 0.6f, z, 0, true, 4)); // дуб
        cubes.add(new Cube(x, y + 0.9f, z, 0, true, 4)); // дуб

        // Листва (простой крест)
        cubes.add(new Cube(x, y + 1.2f, z, 0, true, 5)); // центр
        cubes.add(new Cube(x + 0.3f, y + 1.2f, z, 0, true, 5)); // право
        cubes.add(new Cube(x - 0.3f, y + 1.2f, z, 0, true, 5)); // лево
        cubes.add(new Cube(x, y + 1.2f, z + 0.3f, 0, true, 5)); // вперед
        cubes.add(new Cube(x, y + 1.2f, z - 0.3f, 0, true, 5)); // назад
    }
    private void drawCubedirt(Cube cube) {
        glPushMatrix();
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
        glVertex3f(0.5f, -0.5f, 0.5f);

        //left face
        glColor3f(120 / 255f, 20 / 255f, 0 / 255f);
        glVertex3f(-0.5f, -0.5f, -0.5f);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, -0.5f);
        glEnd();
        glPopMatrix();
    }
    private void drawODNOTON(Cube cube, float R, float G, float B) {
        glPushMatrix();
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
        glPopMatrix();
    }
    private void draw_dub(Cube cube) {
        glPushMatrix();
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
        glPopMatrix();
    }
    private void draw_sun() {
        glPushMatrix();

        // Используем только вращение камеры, но не позицию
        glLoadIdentity();
        glRotatef(camPitch, 1.0f, 0.0f, 0.0f);
        glRotatef(camYaw, 0.0f, 1.0f, 0.0f);

        // Солнце на фиксированном расстоянии (очень далеко)
        glTranslatef(0, 10.0f, -50.0f);

        float sunSize = 5.0f;
        glScalef(sunSize, sunSize, sunSize);

        glColor3f(1.0f, 1.0f, 0.8f);
        glBegin(GL_QUADS);
        glVertex3f(-0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, -0.5f, 0.5f);
        glVertex3f(0.5f, 0.5f, 0.5f);
        glVertex3f(-0.5f, 0.5f, 0.5f);
        glEnd();

        glPopMatrix();
    }

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (Exception e) {
            e.printStackTrace();
            // Ждем ввод, чтобы увидеть ошибку
            System.out.println("Нажмите Enter для выхода...");
            try {
                System.in.read();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}