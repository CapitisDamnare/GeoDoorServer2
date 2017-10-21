package com.tapsi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.concurrent.CountDownLatch;

public class Visualization extends Application {
    public static final CountDownLatch latch = new CountDownLatch(1);
    public static Visualization startUpTest = null;
    public Stage globalStage;

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
        BorderPane pane = new BorderPane();
        Scene scene = new Scene(pane, 500, 500);
        stage.setScene(scene);

        Label label = new Label("Hello");
        pane.setCenter(label);

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
