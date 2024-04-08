package kalashnikov.v.s.aimcoach;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;


import java.util.*;

public class AimCoachApp extends Application {

    public static final int TARGET_RADIUS = 25;
    public static final int TARGET_COUNT = 3;
    private int score = 0;
    private int record = 0;
    private List<Target> targets = new ArrayList<>();
    private Timer timer;
    private int secondsLeft = 60;

    private Label scoreLabel;
    private Label recordLabel;
    private Label timeLabel;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("AimCoach App");

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 1280, 960); // Установка размеров окна

        // Создание меню
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem newGameMenuItem = new MenuItem("New Game");
        MenuItem settingsMenuItem = new MenuItem("Settings");
        MenuItem exitMenuItem = new MenuItem("Exit");
        fileMenu.getItems().addAll(newGameMenuItem, settingsMenuItem, new SeparatorMenuItem(), exitMenuItem);
        menuBar.getMenus().add(fileMenu);

        // Обработка событий меню
        newGameMenuItem.setOnAction(event -> startNewGame());
        exitMenuItem.setOnAction(event -> primaryStage.close());

        root.setTop(menuBar);

        Canvas canvas = new Canvas(1280, 960); // Создание Canvas
        root.setCenter(canvas); // Добавление Canvas на главное окно

        primaryStage.setScene(scene);
        primaryStage.show();

        VBox infoBox = new VBox();
        scoreLabel = new Label("Score: " + score);
        recordLabel = new Label("Record: " + record);
        timeLabel = new Label("Time left: " + secondsLeft + "s");
        infoBox.getChildren().addAll(scoreLabel, recordLabel, timeLabel);
        root.setTop(infoBox);

        // Создание мишеней
        for (int i = 0; i < TARGET_COUNT; i++) {
            createTarget();
        }

        // Запуск таймера
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsLeft--;
                if (secondsLeft <= 0) {
                    timer.cancel();
                }
                updateInfoLabels();
            }
        }, 1000, 1000);

        // Рисование мишеней
        drawTargets(canvas.getGraphicsContext2D());

        // Обработка кликов мыши
        canvas.setOnMouseClicked(event -> {
            int mouseX = (int) event.getX();
            int mouseY = (int) event.getY();
            for (int i = 0; i < targets.size(); i++) {
                Target target = targets.get(i);
                if (target.isHit(mouseX, mouseY)) {
                    score++;
                    scoreLabel.setText("Score: " + score); // Обновление счета
                    targets.remove(target); // Удаление попавшей мишени
                    createTarget(); // Создание новой мишени
                    drawTargets(canvas.getGraphicsContext2D()); // Перерисовка мишеней
                    break;
                }
            }
        });

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void drawTargets(GraphicsContext gc) {
        gc.clearRect(0, 0, 1280, 960); // Очистка холста перед перерисовкой
        gc.setFill(Color.RED); // Установка цвета мишени
        for (Target target : targets) {
            gc.fillOval(target.getX(), target.getY(), TARGET_RADIUS * 2, TARGET_RADIUS * 2);
        }
    }

    private void createTarget() {
        Random random = new Random();
        int x = random.nextInt(1280 - TARGET_RADIUS * 2);
        int y = random.nextInt(960 - TARGET_RADIUS * 2);
        targets.add(new Target(x, y));
    }

    public void startNewGame() {
        score = 0;
        secondsLeft = 60;
        timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                secondsLeft--;
                if (secondsLeft <= 0) {
                    timer.cancel();
                }
                timeLabel.setText("Time left: " + secondsLeft + "s"); // Обновление времени
            }
        }, 1000, 1000);
        targets.clear(); // Очистка мишеней
        for (int i = 0; i < TARGET_COUNT; i++) {
            createTarget();
        }
        scoreLabel.setText("Score: " + score); // Обновление счета
    }

    private void updateInfoLabels() {
        Platform.runLater(() -> {
            timeLabel.setText("Time left: " + secondsLeft + "s");
        }); // Обновление времени
    }

    public static void main(String[] args) {
        launch(args);
    }
}

class Target {
    private int x;
    private int y;

    public Target(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean isHit(int mouseX, int mouseY) {
        return Math.pow(mouseX - x - AimCoachApp.TARGET_RADIUS, 2) + Math.pow(mouseY - y - AimCoachApp.TARGET_RADIUS, 2) < Math.pow(AimCoachApp.TARGET_RADIUS, 2);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

