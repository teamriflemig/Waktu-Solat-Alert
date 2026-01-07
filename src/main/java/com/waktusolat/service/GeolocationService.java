package com.waktusolat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Perkhidmatan untuk mengesan lokasi pengguna berdasarkan alamat IP
 */
public class GeolocationService {
    // API percuma untuk mendapatkan maklumat lokasi dari IP
    private static final String GEO_API_URL = "http://ip-api.com/json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Mengambil data lokasi semasa pengguna
     * 
     * @return Map yang mengandungi 'city' dan 'regionName'
     */
    public Map<String, String> fetchLocation() {
        Map<String, String> locationData = new HashMap<>();
        try {
            URL url = URI.create(GEO_API_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            if (connection.getResponseCode() == 200) {
                try (InputStream inputStream = connection.getInputStream()) {
                    JsonNode root = objectMapper.readTree(inputStream);

                    // Semak jika status adalah 'success'
                    if ("success".equals(root.path("status").asText())) {
                        locationData.put("city", root.path("city").asText());
                        locationData.put("regionName", root.path("regionName").asText());
                        locationData.put("region", root.path("region").asText()); // Kod negeri (cth: SGR)
                    } else {
                        System.err.println("API Geolocation gagal: " + root.path("message").asText());
                    }
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            System.err.println("Gagal mengesan lokasi: " + e.getMessage());
        }
        return locationData;
    }
}
