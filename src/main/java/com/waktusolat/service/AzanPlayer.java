package com.waktusolat.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class AzanPlayer {
    // Pemain audio JavaFX
    private MediaPlayer mediaPlayer;

    // Lokasi fizikal fail Azan di dalam komputer
    // Lokasi fizikal fail Azan di dalam komputer (Nama dipendekkan untuk elak ralat
    // karakter khas)
    private static final String AZAN_FILE_PATH = "C:/Users/Muhamad Iskandar/.gemini/antigravity/scratch/WaktuSolatMY/Audio/Azan.mp3";

    /**
     * Memulakan permainan audio Azan
     */
    public void play() {
        // Amaran: Hentikan dan buang (dispose) pemain lama jika ada untuk elak
        // kebocoran memori
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            // 1. Kenalpasti fail di lokasi yang ditetapkan
            java.io.File file = new java.io.File(AZAN_FILE_PATH);

            // 2. Semak jika fail tersebut benar-benar wujud
            if (!file.exists()) {
                System.err.println("Fail Azan tidak dijumpai di: " + AZAN_FILE_PATH);
                return;
            }

            // 3. Tukar laluan fail kepada format URI (Uniform Resource Identifier) yang
            // difahami JavaFX
            String uriString = file.toURI().toString();
            System.out.println("Cubaan memainkan Azan tempatan: " + uriString);

            // 4. Sediakan media dan pemain media
            Media pick = new Media(uriString);
            mediaPlayer = new MediaPlayer(pick);

            // 5. Tetapkan kekuatan suara kepada 75%
            mediaPlayer.setVolume(0.75);

            // 6. Pantau ralat semasa proses memuatkan atau memainkan audio
            mediaPlayer.setOnError(() -> {
                String error = mediaPlayer.getError() != null ? mediaPlayer.getError().getMessage()
                        : "Ralat tidak diketahui";
                System.err.println("Ralat AzanPlayer: " + error);
            });

            // 7. Apabila media sudah sedia, mulakan permainan secara automatik
            mediaPlayer.setOnReady(() -> {
                System.out.println("AzanPlayer sedia. Durasi: " + mediaPlayer.getTotalDuration());
                mediaPlayer.play();
            });

            // Log status semasa bermain
            mediaPlayer.setOnPlaying(() -> System.out.println("Azan sedang dimainkan..."));

            // Log status apabila audio selesai sepenuhnya
            mediaPlayer.setOnEndOfMedia(() -> System.out.println("Tamat permainan Azan."));

            // 8. Sandaran (Fallback): Jika status sudah 'READY' sebelum 'setOnReady' sempat
            // didaftarkan
            if (mediaPlayer.getStatus() == MediaPlayer.Status.READY) {
                mediaPlayer.play();
            }

        } catch (Exception e) {
            // Tangkap sebarang ralat kritikal (seperti masalah codec atau fail rosak)
            System.err.println("Ralat kritikal semasa memulakan AzanPlayer: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Memberhentikan audio yang sedang dimainkan
     */
    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            System.out.println("Azan dihentikan oleh pengguna.");
        }
    }

    /**
     * Menyemak sama ada audio masih sedang dimainkan atau tidak
     */
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }
}
