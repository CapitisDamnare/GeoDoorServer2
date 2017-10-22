package com.tapsi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

public class Visualization extends Application {
    public static final CountDownLatch latch = new CountDownLatch(1);
    public static Visualization startUpTest = null;
    public Stage globalStage;

    Button btn_test;

    public static Visualization waitForStartUpTest() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return startUpTest;
    }

    public static void setStartUpTest(Visualization startUpTest0) {
        startUpTest = startUpTest0;
        latch.countDown();
    }

    public Visualization() {
        setStartUpTest(this);
    }

    public void printSomething() {
        System.out.println("You called a method on the application");
    }

    @Override
    public void start(Stage stage) throws Exception {
        Platform.setImplicitExit(false);
        globalStage = stage;

        stage.setTitle("GeoDoorServer");
        btn_test = new Button();
        btn_test.setText("My Button");

        StackPane layout = new StackPane();
        layout.getChildren().add(btn_test);

        Scene scene = new Scene(layout, 300, 300);
        stage.setScene(scene);

        stage.show();

    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    public void hideVisualization(boolean val) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (val)
                    globalStage.hide();
                else
                    globalStage.show();
            }
        });
    }
    public void closeVisualization() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Platform.exit();
            }
        });
    }
}
