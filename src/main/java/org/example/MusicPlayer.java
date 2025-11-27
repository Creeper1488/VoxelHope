package org.example;

import javax.sound.sampled.*;
import java.io.*;

public class MusicPlayer {
    private Clip musicClip;
    private boolean isPlaying = false;

    public void playMusic() {
        try {
            // Сначала пробуем загрузить из ресурсов JAR
            if (loadFromResources()) {
                return;
            }

            // Если не получилось, пробуем загрузить из внешнего файла
            if (loadFromFileSystem()) {
                return;
            }

            System.out.println("Файл музыки не найден ни в ресурсах JAR, ни рядом с программой");

        } catch (Exception e) {
            System.out.println("Ошибка загрузки музыки: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean loadFromResources() {
        try {
            InputStream audioStream = getClass().getResourceAsStream("/Peaceful_b__27__2025__2205.wav");

            if (audioStream != null) {
                System.out.println("Загружаем музыку из ресурсов JAR...");

                AudioInputStream audioIn = AudioSystem.getAudioInputStream(audioStream);
                musicClip = AudioSystem.getClip();
                musicClip.open(audioIn);
                musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                musicClip.start();

                isPlaying = true;
                System.out.println("Фоновая музыка запущена из ресурсов JAR!");
                return true;
            }
        } catch (Exception e) {
            System.out.println("Ошибка загрузки из ресурсов: " + e.getMessage());
        }
        return false;
    }

    private boolean loadFromFileSystem() {
        try {
            String[] possibleNames = {
                    "Peaceful_b__27__2025__2205.wav",
                    "peaceful_b__27__2025__2205.wav",
                    "game_music.wav",
                    "background_music.wav"
            };

            for (String name : possibleNames) {
                File musicFile = new File(name);
                if (musicFile.exists()) {
                    System.out.println("Найден файл музыки: " + musicFile.getAbsolutePath());

                    AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicFile);
                    musicClip = AudioSystem.getClip();
                    musicClip.open(audioIn);
                    musicClip.loop(Clip.LOOP_CONTINUOUSLY);
                    musicClip.start();

                    isPlaying = true;
                    System.out.println("Фоновая музыка запущена из внешнего файла!");
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("Ошибка загрузки из файловой системы: " + e.getMessage());
        }
        return false;
    }

    public void stop() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            isPlaying = false;
            System.out.println("Музыка остановлена");
        }
    }

    public void toggle() {
        if (isPlaying) {
            stop();
        } else {
            playMusic();
        }
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    // Метод для тестирования аудиосистемы
    public static void testAudioSystem() {
        new Thread(() -> {
            try {
                System.out.println("Тестируем аудиосистему...");
                for (int i = 0; i < 3; i++) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    Thread.sleep(500);
                }
                System.out.println("Аудиосистема работает нормально");
            } catch (Exception e) {
                System.out.println("Проблема с аудиосистемой: " + e.getMessage());
            }
        }).start();
    }
}