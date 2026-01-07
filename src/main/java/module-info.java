module com.waktusolat {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.fasterxml.jackson.databind;

    opens com.waktusolat to javafx.fxml;
    opens com.waktusolat.model to com.fasterxml.jackson.databind;

    exports com.waktusolat;
}
