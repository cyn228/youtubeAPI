import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {

    static int rating_num  = 0;
    /*
     * Example main method
     * You can use this main as a playground to test the model with your API key
     * Again, the API key can be obtained by following this guide:
     * https://developers.google.com/youtube/registering_an_application#create_project
     */
    public static void main(String[] args) {
        Model model = new Model("AIzaSyD1Fwr1e_47xBt99XVO1qY3vmG6KEm5ARY");
        View v = new View(model);


    }

}