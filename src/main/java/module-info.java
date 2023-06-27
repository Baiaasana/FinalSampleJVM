module com.example.finalsample {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.finalsample to javafx.fxml;
    exports com.example.finalsample;
}