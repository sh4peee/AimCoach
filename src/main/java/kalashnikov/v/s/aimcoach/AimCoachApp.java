package kalashnikov.v.s.aimcoach;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class AimCoachApp extends Application {
    public static final int LENGTH = 600;
    public static final int WIDTH = 800;
    public static final int TARGET_RADIUS_EASY = 25;
    public static final int TARGET_RADIUS_MEDIUM = 20;
    public static final int TARGET_RADIUS_HARD = 15;
    private int targetRadius = TARGET_RADIUS_EASY;
    public static final int TARGET_COUNT = 3;
    private int score = 0;
    private int record = 0;
    private List<Target> targets = new ArrayList<>();
    private AnimationTimer timer;
    private long lastTime;
    private long targetTime = 20 * 1_000_000_000L; // 20 секунд в наносекундах
    private Label scoreLabel;
    private Label recordLabel;
    private Label timeLabel;
    private Stage primaryStage;
    private Scene menuScene;
    private Scene gameScene;
    private ComboBox<String> soundComboBox;
    private boolean isGamePaused = false;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AimCoach App");

        // Создаем сцену меню
        menuScene = createMenuScene();

        // Устанавливаем сцену меню в качестве сцены по умолчанию
        this.primaryStage.setScene(menuScene);
        this.primaryStage.show();

        // Обработка нажатия на клавишу ESC для вызова и закрытия меню
        this.primaryStage.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, (key) -> {
            if (key.getCode() == KeyCode.ESCAPE) {
                if (this.primaryStage.getScene() == gameScene) {
                    this.primaryStage.setScene(menuScene);
                    isGamePaused = true;
                    if (timer != null) timer.stop(); // Пауза таймера
                }
            }
        });

        // Создаем сцену игры и присваиваем ее gameScene
        gameScene = createGameScene();
    }
    private Scene createMenuScene() {
        // Выпадающий список для выбора звука попадания
        soundComboBox = new ComboBox<>();
        soundComboBox.getItems().addAll("ak47.wav", "awp.wav", "deagle.wav");
        soundComboBox.setValue("ak47.wav"); // Устанавливаем значение по умолчанию

        // Создаем выпадающий список для выбора времени игры
        ComboBox<String> timeComboBox = new ComboBox<>();
        timeComboBox.getItems().addAll("20 sec", "40 sec", "60 sec");
        timeComboBox.setValue("20 sec"); // Устанавливаем значение по умолчанию

        // Обработка выбора времени игры
        timeComboBox.setOnAction(event -> {
            String selectedTime = timeComboBox.getValue();
            switch (selectedTime) {
                case "20 sec":
                    targetTime = 20 * 1_000_000_000L;
                    break;
                case "40 sec":
                    targetTime = 40 * 1_000_000_000L;
                    break;
                case "60 sec":
                    targetTime = 60 * 1_000_000_000L;
                    break;
            }
        });

        // Создаем кнопку для входа в игру
        Button startGameButton = new Button("Start Game");
        startGameButton.setOnAction(event -> {
            // Заменяем сцену на сцену игры
            primaryStage.setScene(gameScene);
            isGamePaused = false;
            resetScore();
            generateTargets();
            lastTime = System.nanoTime();
            startTimer();

        });

        // Создаем кнопку для выхода из приложения
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(event -> {
            // Закрываем приложение
            primaryStage.close();
        });

        // Создаем метку для отображения текущей сложности
        Label difficultyLabel = new Label("Difficulty: Easy");

        // Создаем кнопку для изменения сложности игры
        Button changeDifficultyButton = new Button("Change Difficulty");
        changeDifficultyButton.setOnAction(event -> {
            // Изменяем сложность игры и обновляем метку
            if (targetRadius== TARGET_RADIUS_EASY) {
                targetRadius = TARGET_RADIUS_MEDIUM;
                difficultyLabel.setText("Difficulty: Medium");
            } else if (targetRadius == TARGET_RADIUS_MEDIUM) {
                targetRadius = TARGET_RADIUS_HARD;
                difficultyLabel.setText("Difficulty: Hard");
            } else if (targetRadius == TARGET_RADIUS_HARD) {
                targetRadius = TARGET_RADIUS_EASY;
                difficultyLabel.setText("Difficulty: Easy");
            }
            generateTargets();
        });

        // Создаем метку для отображения текущего счета
        scoreLabel = new Label();
        scoreLabel.setText("Score: " + score);

        // Создаем метку для отображения наилучшего результата
        recordLabel = new Label("Record: " + record);

        // Создаем вертикальную панель для расположения элементов меню
        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.getChildren().addAll(timeComboBox);
        menuLayout.getChildren().addAll(startGameButton, changeDifficultyButton, exitButton, difficultyLabel, scoreLabel, recordLabel);
        menuLayout.getChildren().add(soundComboBox);

        // Создаем главную панель
        BorderPane menuPane = new BorderPane();
        menuPane.setCenter(menuLayout);
        menuPane.setTop(timeComboBox);

        // Создаем сцену меню
        Scene menuScene = new Scene(menuPane, WIDTH, LENGTH);

        return menuScene;
    }
    private Scene createGameScene() {

        // Создаем полотно для рисования
        Canvas gameCanvas = new Canvas(WIDTH, LENGTH);

        // Получаем графический контекст
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();

        // Создаем метку для отображения оставшегося времени
        timeLabel = new Label("Time Left: " + (targetTime / 1_000_000_000)); // Переводим наносекунды всекунды

        // Создаем вертикальную панель для расположения элементов игры
        VBox gameLayout = new VBox(20);
        gameLayout.setAlignment(Pos.TOP_CENTER);

        gameLayout.getChildren().addAll(timeLabel, gameCanvas);

        // Создаем главную панель
        BorderPane gamePane = new BorderPane();
        gamePane.setCenter(gameLayout);

        // Создаем сцену игры
        Scene gameScene = new Scene(gamePane, WIDTH, LENGTH);

        // Обработка нажатия на полотно игры
        gameCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            Iterator<Target> iterator = targets.iterator();
            while (iterator.hasNext()) {
                Target target = iterator.next();
                if (target.contains(event.getX(), event.getY()) && !target.isPopped()) {
                    score++; // Увеличиваем счет
                    scoreLabel.setText("Score: " + score); // Обновляем метку счета
                    target.pop(); // Помечаем текущий шарик как "взорванный"

                    // Получаем выбранный звук из ComboBox
                    String selectedSound = soundComboBox.getValue();
                    // Создаем путь к файлу звука попадания
                    String soundPath = "src/Sounds/" + selectedSound;
                    // запускает звук с заданной громкостью( от 0 до 1)_
                    SoundPlayer.playSound(soundPath).setVolume((float) 0.4);
                    // запускает звук и остановит все остальные потоки
                    //SoundPlayer.playSound("src/Sounds/ak47.wav").join();
                    // запускает звук не останавливая потоки и смешивая звуки
                    //SoundPlayer.playSound("sounds/hello.wav");

                    // Создаем новый шарик и добавляем его в список целей
                    double x = new Random().nextDouble() * (WIDTH - targetRadius * 2) + targetRadius;
                    double y = new Random().nextDouble() * (LENGTH - targetRadius * 2) + targetRadius;
                    targets.add(new Target(x, y, targetRadius));

                    // Перерисовываем цели на полотне
                    drawTargets(gc);

                    // Прерываем цикл, чтобы обработать клик только на одном шарике за раз
                    break;
                }
            }
        });
        targets.clear();
        generateTargets();
        // Рисуем шарики на полотне
        drawTargets(gc);
        return gameScene;
    }

    private void drawTargets(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, LENGTH); // Очищаем полотно
        for (Target target : targets) {
            target.draw(gc); // Рисуем цели
        }
    }

    private void startTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long currentTime = System.nanoTime();
                long elapsedTime = currentTime - lastTime;
                lastTime = currentTime;

                targetTime -= elapsedTime;

                if (targetTime <= 0) {
                    timer.stop();
                    if (score > record) {
                        record = score;
                        recordLabel.setText("Record: " + record);
                    }
                    isGamePaused = true;
                    primaryStage.setScene(menuScene);
                }

                long secondsLeft = targetTime / 1_000_000_000; // Переводим наносекунды в секунды
                timeLabel.setText("Time Left: " + secondsLeft);
            }
        };
        timer.start();
    }
    private void resetScore() {
        score = 0;
        scoreLabel.setText("Score: " + score);
    }
    private void generateTargets() {
        Random random = new Random();
        int remainingTargets = TARGET_COUNT - targets.size();
        for (int i = 0; i < remainingTargets; i++) {
            double x = random.nextDouble() * (WIDTH - targetRadius * 2) + targetRadius - targets.size();
            double y = random.nextDouble() * (LENGTH - targetRadius * 2) + targetRadius - targets.size();
            targets.add(new Target(x, y, targetRadius));
        }
    }
    public static void main(String[] args) {
        launch(args);
    }
}