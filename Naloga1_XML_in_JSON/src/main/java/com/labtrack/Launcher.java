package com.labtrack;

//import com.labtrack.controller.MainController;

import com.labtrack.console.ConsoleApp;

import java.util.Collections;

public class Launcher {
    public static void main(String[] args){
        //LabTrackApp.main(args);
        //MainController mainController = new MainController();
        //mainController.loadData();
        ConsoleApp app = new ConsoleApp();
        app.run();

    }
}