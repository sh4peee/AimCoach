module com.example.aimcoach {
    requires javafx.controls;
    requires javafx.fxml;


    opens kalashnikov.v.s.aimcoach to javafx.fxml;
    exports kalashnikov.v.s.aimcoach;
}