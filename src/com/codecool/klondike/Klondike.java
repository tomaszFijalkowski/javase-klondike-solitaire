package com.codecool.klondike;


import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;


public class Klondike extends Application {

    private static final double WINDOW_WIDTH = 1400;
    private static final double WINDOW_HEIGHT = 900;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Card.loadCardImages();
        Game game = new Game();
        game.setTableBackground(new Image("/table/green.png"));

        Image switchIcon = new Image("file:resources/card_images/switch.png");
        ImageView switchIconWiew = new ImageView(switchIcon);
        Button switchButton = (new Button( "", switchIconWiew ));
        game.getChildren().add(switchButton );
        switchButton.setOnAction((ActionEvent e) -> {
            Card.changeCardSuit();
            System.out.println("CHANGE");
            start(primaryStage);

        });


        primaryStage.setTitle("Klondike Solitaire");
        primaryStage.setScene(new Scene(game, WINDOW_WIDTH, WINDOW_HEIGHT));
        primaryStage.show();

    }

}
