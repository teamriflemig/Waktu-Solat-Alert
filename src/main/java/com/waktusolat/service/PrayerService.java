package com.waktusolat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.waktusolat.model.PrayerTime;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class PrayerService {
    // Alamat URL API JAKIM untuk mendapatkan waktu solat harian
    private static final String API_URL = "https://www.e-solat.gov.my/index.php?r=esolatApi/takwimsolat&period=today&zone=";

    // Alat untuk menukar data JSON kepada objek Java
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Mengambil data waktu solat berdasarkan kod zon
     */
    public PrayerTime fetchPrayerTime(String zoneCode) throws Exception {
        // 1. Bina URL lengkap dengan kod zon yang dipilih
        URL url = URI.create(API_URL + zoneCode).toURL();

        // 2. Buka sambungan HTTP ke pelayan JAKIM
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        // 3. Semak jika sambungan berjaya (Kod 200 bermaksud OK)
        if (connection.getResponseCode() != 200) {
            throw new RuntimeException("Gagal mengambil waktu solat: " + connection.getResponseCode());
        }

        // 4. Baca aliran data (input stream) daripada sambungan tersebut
        try (InputStream inputStream = connection.getInputStream()) {
            // 5. Tukar aliran data kepada struktur pohon JSON (JsonNode)
            JsonNode root = objectMapper.readTree(inputStream);

            // 6. Dapatkan nod 'prayerTime' dan ambil elemen pertama (indeks 0)
            JsonNode prayerTimeNode = root.path("prayerTime").get(0);

            // 7. Pastikan data tidak kosong
            if (prayerTimeNode == null || prayerTimeNode.isMissingNode()) {
                throw new RuntimeException("Tiada data waktu solat dijumpai dalam respons");
            }

            // 8. Tukar data JSON tersebut kepada objek kelas 'PrayerTime'
            return objectMapper.treeToValue(prayerTimeNode, PrayerTime.class);
        } finally {
            // 9. Putuskan sambungan untuk lepaskan sumber sistem
            connection.disconnect();
        }
    }
}
