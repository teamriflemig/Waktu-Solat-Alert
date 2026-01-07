package com.waktusolat;

import com.waktusolat.model.PrayerTime;
import com.waktusolat.service.AzanPlayer;
import com.waktusolat.service.GeolocationService;
import com.waktusolat.service.PrayerService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Timer;
import java.util.TimerTask;

public class PrimaryController {

    // Elemen UI yang disambungkan dari fail FXML
    @FXML
    private ComboBox<String> stateComboBox;
    @FXML
    private ComboBox<com.waktusolat.model.Zone> zoneComboBox;
    @FXML
    private Label timeLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label currentPrayerName;
    @FXML
    private Label currentPrayerTime;
    @FXML
    private Label currentPrayerStatus;
    @FXML
    private Label nextPrayerName;
    @FXML
    private Label nextPrayerTime;
    @FXML
    private Label nextPrayerCountdown;
    @FXML
    private Button stopAzanBtn;

    // Perkhidmatan dan utiliti utama
    private final PrayerService prayerService = new PrayerService();
    private final GeolocationService geoService = new GeolocationService();
    private final AzanPlayer azanPlayer = new AzanPlayer();
    private PrayerTime currentPrayerData;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM yyyy");

    // Pemetaan data zon untuk setiap negeri
    private final java.util.Map<String, java.util.List<com.waktusolat.model.Zone>> stateZones = new java.util.HashMap<>();

    // Pemacu penjejakan Azan untuk mengelakkan pemicu berganda
    private String lastAzanPlayedFor = "";

    @FXML
    public void initialize() {
        // 1. Sediakan data zon awal
        setupZoneData();

        // 2. Masukkan senarai negeri ke dalam pilihan (secar disusun A-Z)
        stateComboBox.getItems().addAll(stateZones.keySet().stream().sorted().toArray(String[]::new));

        // 3. Tambah 'listener' untuk kemas kini zon apabila negeri berubah
        stateComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                zoneComboBox.getItems().setAll(stateZones.get(newVal));
                zoneComboBox.getSelectionModel().selectFirst();
            }
        });

        zoneComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fetchData();
            }
        });

        // Tetapan pilihan lalai: Cuba kesan lokasi secara automatik
        autoDetectLocation();

        startClock();

        // Segarkan data setiap satu jam
        Timer refreshTimer = new Timer(true);
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                fetchData();
            }
        }, 3600000, 3600000);
    }

    private void setupZoneData() {
        stateZones.put("Johor", java.util.List.of(
                new com.waktusolat.model.Zone("JHR01", "Pulau Aur & Pemanggil", "Johor"),
                new com.waktusolat.model.Zone("JHR02", "JB, Kota Tinggi, Mersing", "Johor"),
                new com.waktusolat.model.Zone("JHR03", "Kluang, Pontian", "Johor"),
                new com.waktusolat.model.Zone("JHR04", "Batu Pahat, Muar, Segamat", "Johor")));
        stateZones.put("Kedah", java.util.List.of(
                new com.waktusolat.model.Zone("KDH01", "Kota Setar, Kubang Pasu", "Kedah"),
                new com.waktusolat.model.Zone("KDH02", "Kuala Muda, Yan, Pendang", "Kedah"),
                new com.waktusolat.model.Zone("KDH03", "Padang Terap, Sik", "Kedah"),
                new com.waktusolat.model.Zone("KDH04", "Baling", "Kedah"),
                new com.waktusolat.model.Zone("KDH05", "Bandar Baharu, Kulim", "Kedah"),
                new com.waktusolat.model.Zone("KDH06", "Langkawi", "Kedah")));
        stateZones.put("Kelantan", java.util.List.of(
                new com.waktusolat.model.Zone("KTN01", "Kota Bharu, Pasir Mas", "Kelantan"),
                new com.waktusolat.model.Zone("KTN03", "Gua Musang, Jeli", "Kelantan")));
        stateZones.put("Melaka", java.util.List.of(new com.waktusolat.model.Zone("MLK01", "Seluruh Melaka", "Melaka")));
        stateZones.put("Negeri Sembilan", java.util.List.of(
                new com.waktusolat.model.Zone("NGS01", "Tampin, Jempol", "Negeri Sembilan"),
                new com.waktusolat.model.Zone("NGS02", "Seremban, Port Dickson", "Negeri Sembilan")));
        stateZones.put("Pahang", java.util.List.of(
                new com.waktusolat.model.Zone("PHG01", "Pulau Tioman", "Pahang"),
                new com.waktusolat.model.Zone("PHG02", "Kuantan, Pekan, Rompin", "Pahang"),
                new com.waktusolat.model.Zone("PHG03", "Jerantut, Temerloh", "Pahang"),
                new com.waktusolat.model.Zone("PHG04", "Bentong, Lipis, Raub", "Pahang"),
                new com.waktusolat.model.Zone("PHG05", "Genting Sempah", "Pahang"),
                new com.waktusolat.model.Zone("PHG06", "Cameron Highlands", "Pahang")));
        stateZones.put("Perlis", java.util.List.of(new com.waktusolat.model.Zone("PLS01", "Seluruh Perlis", "Perlis")));
        stateZones.put("Pulau Pinang",
                java.util.List.of(new com.waktusolat.model.Zone("PNG01", "Seluruh P.Pinang", "Pulau Pinang")));
        stateZones.put("Perak", java.util.List.of(
                new com.waktusolat.model.Zone("PRK01", "Tapah, Tanjung Malim", "Perak"),
                new com.waktusolat.model.Zone("PRK02", "Ipoh, Batu Gajah", "Perak"),
                new com.waktusolat.model.Zone("PRK03", "Grik, Lenggong", "Perak"),
                new com.waktusolat.model.Zone("PRK05", "Teluk Intan, Lumut", "Perak"),
                new com.waktusolat.model.Zone("PRK06", "Taiping, Parit Buntar", "Perak")));
        stateZones.put("Sabah", java.util.List.of(
                new com.waktusolat.model.Zone("SBH07", "Kota Kinabalu, Papar", "Sabah"),
                new com.waktusolat.model.Zone("SBH04", "Tawau, Lahad Datu", "Sabah"),
                new com.waktusolat.model.Zone("SBH01", "Sandakan", "Sabah")));
        stateZones.put("Sarawak", java.util.List.of(
                new com.waktusolat.model.Zone("SWK08", "Kuching, Bau, Lundu", "Sarawak"),
                new com.waktusolat.model.Zone("SWK04", "Sibu, Kapit", "Sarawak"),
                new com.waktusolat.model.Zone("SWK02", "Miri, Niah", "Sarawak")));
        stateZones.put("Selangor", java.util.List.of(
                new com.waktusolat.model.Zone("SGR01", "Gombak, Petaling, Shah Alam", "Selangor"),
                new com.waktusolat.model.Zone("SGR02", "Kuala Selangor, Sabak Bernam", "Selangor"),
                new com.waktusolat.model.Zone("SGR03", "Klang, Kuala Langat", "Selangor")));
        stateZones.put("Terengganu", java.util.List.of(
                new com.waktusolat.model.Zone("TRG01", "Kuala Terengganu, Marang", "Terengganu"),
                new com.waktusolat.model.Zone("TRG02", "Besut, Setiu", "Terengganu"),
                new com.waktusolat.model.Zone("TRG03", "Hulu Terengganu", "Terengganu"),
                new com.waktusolat.model.Zone("TRG04", "Dungun, Kemaman", "Terengganu")));
        stateZones.put("Wilayah Persekutuan", java.util.List.of(
                new com.waktusolat.model.Zone("WLY01", "KL, Putrajaya", "Wilayah Persekutuan"),
                new com.waktusolat.model.Zone("WLY02", "Labuan", "Wilayah Persekutuan")));
    }

    /**
     * Mengesan tetingkap lokasi pengguna dan memilih zon yang sesuai
     */
    private void autoDetectLocation() {
        new Thread(() -> {
            java.util.Map<String, String> location = geoService.fetchLocation();
            if (location.isEmpty()) {
                Platform.runLater(() -> {
                    stateComboBox.getSelectionModel().select("Wilayah Persekutuan");
                    zoneComboBox.getSelectionModel().select(0);
                });
                return;
            }

            String regionCode = location.get("region");
            String regionName = location.get("regionName");
            String city = location.get("city");

            Platform.runLater(() -> {
                // Pemetaan Kod Region (ISO) ke Nama Negeri yang digunakan dalam aplikasi
                java.util.Map<String, String> regionCodeToState = new java.util.HashMap<>();
                regionCodeToState.put("01", "Johor");
                regionCodeToState.put("02", "Kedah");
                regionCodeToState.put("03", "Kelantan");
                regionCodeToState.put("04", "Melaka");
                regionCodeToState.put("05", "Negeri Sembilan");
                regionCodeToState.put("06", "Pahang");
                regionCodeToState.put("07", "Pulau Pinang");
                regionCodeToState.put("08", "Perak");
                regionCodeToState.put("09", "Perlis");
                regionCodeToState.put("10", "Selangor");
                regionCodeToState.put("11", "Terengganu");
                regionCodeToState.put("12", "Sabah");
                regionCodeToState.put("13", "Sarawak");
                regionCodeToState.put("14", "Wilayah Persekutuan"); // KL
                regionCodeToState.put("15", "Wilayah Persekutuan"); // Labuan
                regionCodeToState.put("16", "Wilayah Persekutuan"); // Putrajaya

                String detectedState = regionCodeToState.get(regionCode);

                if (detectedState != null && stateZones.containsKey(detectedState)) {
                    stateComboBox.getSelectionModel().select(detectedState);
                } else {
                    // Cuba padankan via nama (fallback)
                    boolean foundState = false;
                    for (String state : stateZones.keySet()) {
                        if (regionName.toLowerCase().contains(state.toLowerCase()) ||
                                state.toLowerCase().contains(regionName.toLowerCase())) {
                            stateComboBox.getSelectionModel().select(state);
                            foundState = true;
                            break;
                        }
                    }

                    if (!foundState) {
                        stateComboBox.getSelectionModel().select("Wilayah Persekutuan");
                    }
                }

                // Cuba padankan bandar dalam zon negeri tersebut
                String selectedState = stateComboBox.getSelectionModel().getSelectedItem();
                java.util.List<com.waktusolat.model.Zone> zones = stateZones.get(selectedState);

                com.waktusolat.model.Zone matchedZone = zones.get(0); // Default to first
                for (com.waktusolat.model.Zone zone : zones) {
                    if (zone.getName().toLowerCase().contains(city.toLowerCase()) ||
                            city.toLowerCase().contains(zone.getName().split(",")[0].trim().toLowerCase())) {
                        matchedZone = zone;
                        break;
                    }
                }
                zoneComboBox.getSelectionModel().select(matchedZone);
            });
        }).start();
    }

    /**
     * Memulakan jam digital sistem yang dikemaskini setiap 1 saat
     */
    private void startClock() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Gunakan Platform.runLater untuk memastikan perubahan UI berlaku pada Thread
                // UI JavaFX
                Platform.runLater(() -> {
                    LocalDateTime now = LocalDateTime.now();
                    timeLabel.setText(now.format(timeFormatter));
                    dateLabel.setText(now.format(dateFormatter));

                    // Semak status waktu solat setiap saat
                    updatePrayerInfo(now.toLocalTime());
                });
            }
        }, 0, 1000);
    }

    /**
     * Mengambil data dari internet (API JAKIM) menggunakan thread berasingan
     */
    private void fetchData() {
        com.waktusolat.model.Zone selectedZone = zoneComboBox.getSelectionModel().getSelectedItem();
        if (selectedZone == null)
            return;

        // Jangan sekat UI thread semasa menunggu data dari internet
        new Thread(() -> {
            try {
                currentPrayerData = prayerService.fetchPrayerTime(selectedZone.getCode());
                // Kemaskini paparan waktu solat sebaik sahaja data diterima
                Platform.runLater(() -> updatePrayerInfo(LocalTime.now()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Logik utama untuk menentukan solat semasa, solat seterusnya dan pemicu Azan
     */
    private void updatePrayerInfo(LocalTime now) {
        if (currentPrayerData == null)
            return;

        // Senarai nama solat dalam Bahasa Malaysia
        String[] names = { "Subuh", "Syuruk", "Zohor", "Asar", "Maghrib", "Isyak" };

        // Tukar rentetan masa dari API kepada format objek masa (LocalTime)
        LocalTime[] times = {
                parseTime(currentPrayerData.getFajr()),
                parseTime(currentPrayerData.getSyuruk()),
                parseTime(currentPrayerData.getDhuhr()),
                parseTime(currentPrayerData.getAsr()),
                parseTime(currentPrayerData.getMaghrib()),
                parseTime(currentPrayerData.getIsha())
        };

        // Kenalpasti indeks solat yang sedang berlangsung berdasarkan masa sistem
        int currentIndex = -1;
        for (int i = 0; i < times.length; i++) {
            if (now.isAfter(times[i]) || now.equals(times[i])) {
                currentIndex = i;
            }
        }

        if (currentIndex != -1) {
            // Paparkan nama dan waktu solat semasa di UI
            currentPrayerName.setText(names[currentIndex]);
            currentPrayerTime.setText(times[currentIndex].format(DateTimeFormatter.ofPattern("hh:mm a")));

            // Semakan latar belakang untuk permainan Azan automatik
            if (currentIndex != 1) { // 1 = Syuruk (Azan biasanya tidak dilaungkan untuk Syuruk)
                String pName = names[currentIndex];
                LocalTime pTime = times[currentIndex];

                // Pemicu Azan yang Mantap:
                // Mainkan Azan jika solat ini belum dilaungkan dan berada dalam tingkap 1 minit
                if (!lastAzanPlayedFor.equals(pName) &&
                        (now.equals(pTime) || (now.isAfter(pTime) && now.isBefore(pTime.plusMinutes(1))))) {
                    lastAzanPlayedFor = pName; // Tandakan sebagai sudah dilaungkan
                    azanPlayer.play();
                    stopAzanBtn.setVisible(true);
                    stopAzanBtn.setManaged(true);
                }
            }
        }

        int nextIndex = (currentIndex + 1) % times.length;
        nextPrayerName.setText(names[nextIndex]);
        nextPrayerTime.setText(times[nextIndex].format(DateTimeFormatter.ofPattern("hh:mm a")));

        LocalTime nextTime = times[nextIndex];
        long diffSeconds;
        if (nextTime.isAfter(now)) {
            diffSeconds = java.time.Duration.between(now, nextTime).getSeconds();
        } else {
            diffSeconds = java.time.Duration.between(now, nextTime.plusHours(24)).getSeconds();
        }

        long hours = diffSeconds / 3600;
        long minutes = (diffSeconds % 3600) / 60;
        long seconds = diffSeconds % 60;
        nextPrayerCountdown.setText(String.format(" %02d:%02d:%02d", hours, minutes, seconds));

        // Kemas kini keterlihatan butang henti
        if (!azanPlayer.isPlaying()) {
            stopAzanBtn.setVisible(false);
            stopAzanBtn.setManaged(false);
        }
    }

    /**
     * Membantu menukar String "HH:mm" dari API kepada format LocalTime Java
     */
    private LocalTime parseTime(String timeStr) {
        if (timeStr == null)
            return LocalTime.now();
        String[] parts = timeStr.split(":");
        return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
    }

    /**
     * Tindakan manual apabila butang "Henti Azan" ditekan
     */
    @FXML
    private void stopAzan() {
        azanPlayer.stop();
        stopAzanBtn.setVisible(false);
        stopAzanBtn.setManaged(false);
    }

    /**
     * Tindakan manual apabila ikon pembesar suara (🔊) ditekan untuk menguji suara
     */
    @FXML
    private void testAzan() {
        // Jika sedang bermain, hentikan. Jika sedang diam, mainkan.
        if (azanPlayer.isPlaying()) {
            stopAzan();
        } else {
            azanPlayer.play();
            stopAzanBtn.setVisible(true);
            stopAzanBtn.setManaged(true);
        }
    }
}
