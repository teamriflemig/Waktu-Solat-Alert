package com.waktusolat;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;

/**
 * Kelas Utama Aplikasi JavaFX
 */
public class App extends Application {

    // Simpan rujukan kepada 'scene' (pemandangan) supaya boleh ditukar kemudian
    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Muat fail rekabentuk (FXML) dan tetapkan saiz tetingkap (300x220)
        scene = new Scene(loadFXML("primary"), 300, 220);

        // 2. Tetapkan ikon tetingkap menggunakan gambar Logo.png dari folder resources
        stage.getIcons().add(new javafx.scene.image.Image(App.class.getResourceAsStream("Logo.png")));

        // 3. Masukkan 'scene' ke dalam 'stage' (pentas/tetingkap utama)
        stage.setScene(scene);

        // 4. Set tajuk tetingkap yang akan dipaparkan di bahagian atas
        stage.setTitle("Waktu Solat MY");

        // 5. Halang pengguna daripada mengubah saiz tetingkap secara manual
        stage.setResizable(false);

        // 6. Paparkan tetingkap kepada pengguna
        stage.show();

        // 7. Laraskan kedudukan tetingkap ke penjuru kanan bawah skrin (dengan margin
        // 10px)
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double margin = 10;
        stage.setX(visualBounds.getMaxX() - scene.getWidth() - margin);
        stage.setY(visualBounds.getMaxY() - scene.getHeight() - margin);
    }

    // Fungsi untuk menukar kandungan FXML utama jika perlu
    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    // Fungsi bantuan untuk mencari dan memuatkan fail .fxml dari folder resources
    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    // Titik mula program apabila dijalankan
    public static void main(String[] args) {
        launch(); // Melancarkan aplikasi JavaFX
    }

}
