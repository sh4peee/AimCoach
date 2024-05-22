package kalashnikov.v.s.aimcoach;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javafx.scene.paint.Color;

public class AimCoachApp extends Application {
    public static final int LENGTH = 776;
    public static final int WIDTH = 1380;
    public static final int TARGET_RADIUS_EASY = 35;
    public static final int TARGET_RADIUS_MEDIUM = 30;
    public static final int TARGET_RADIUS_HARD = 25;
    private int targetRadius = TARGET_RADIUS_EASY;
    public static final int TARGET_COUNT = 3;
    private int score = 0;
    private int record = 0;
    private final List<Target> targets = new ArrayList<>();
    private AnimationTimer timer;
    private long lastTime;
    private long targetTime = 20 * 1_000_000_000L; // 20 секунд в наносекундах
    private Label scoreLabel;
    private Label recordLabel;
    private Label timeLabel;
    private Label accuracyLabel;
    private Stage primaryStage;
    private Scene menuScene;
    private Scene gameScene;
    private ComboBox<String> soundComboBox;
    private ComboBox<String> backgroundComboBox;
    private int successfulHits = 0;
    private int missedHits = 0;
    private static final Map<String, String> BACKGROUND_MAP = Map.of(
            "Dust", "file:src/images/bg_1.jpg",
            "Inferno", "file:src/images/bg_2.jpg"
    );

    private long[] reactionTimes;
    private int attemptIndex;
    private Label[] attemptLabels;
    private Label resultLabel;
    private Scene reactionTestScene;
    private final Random random = new Random();
    private StackPane targetPane;
    private AnimationTimer reactionTimer;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("AimCoach App");

        initializeMenuScene();
        initializeGameScene();
        initializeReactionTestScene();

        // Устанавливаем сцену меню в качестве сцены по умолчанию
        this.primaryStage.setScene(menuScene);
        this.primaryStage.show();
        setupEscapeKeyHandler();
    }
    private void initializeMenuScene() {
        menuScene = createMenuScene();
    }

    private void initializeGameScene() {
        gameScene = createGameScene();
    }

    private void initializeReactionTestScene() {
        reactionTestScene = createReactionTestScene();
    }

    private void setupEscapeKeyHandler() {
        this.primaryStage.addEventHandler(javafx.scene.input.KeyEvent.KEY_RELEASED, key -> {
            if (key.getCode() == KeyCode.ESCAPE) {
                if (this.primaryStage.getScene() == gameScene || this.primaryStage.getScene() == reactionTestScene) {
                    this.primaryStage.setScene(menuScene);
                    if (timer != null) timer.stop();
                }
            }
        });
    }
    private Scene createMenuScene() {
        // Выпадающий список для выбора звука попадания
        soundComboBox = new ComboBox<>();
        soundComboBox.getItems().addAll("ak47.wav", "awp.wav", "deagle.wav");
        soundComboBox.setValue("ak47.wav"); // Устанавливаем значение по умолчанию

        backgroundComboBox = new ComboBox<>();
        backgroundComboBox.getItems().addAll(
                "Dust", "Inferno"
        );
        backgroundComboBox.setValue("Dust"); // Установка значения по умолчанию

        // Обработчик выбора фона
        backgroundComboBox.setOnAction(event -> {
            String selectedBackground = backgroundComboBox.getValue();
            updateBackground(selectedBackground);
        });

        Label backgroundLabel = new Label("Карта:");
        Label soundLabel = new Label("Звук выстрела:");

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

        // Кнопка для входа в игру
        Button startGameButton = new Button("Start Game");
        startGameButton.setOnAction(event -> {
            // Замена сцены на сцену игры
            primaryStage.setScene(gameScene);
            resetScore();
            generateTargets();
            lastTime = System.nanoTime();
            startTimer();

        });

        Button reactionTestButton = new Button("Reaction Test");
        reactionTestButton.setOnAction(event -> primaryStage.setScene(reactionTestScene));

        Button restartButton = new Button("Restart");
        restartButton.setOnAction(event -> {
            resetGame(); // Переиспользование метода resetGame для перезапуска игры
        });

        // Кнопку для выхода из приложения
        Button exitButton = new Button("Exit");
        exitButton.setOnAction(event -> {
            primaryStage.close();
        });

        Label difficultyLabel = new Label("Difficulty: Easy");

        Button changeDifficultyButton = new Button("Change Difficulty");
        changeDifficultyButton.setOnAction(event -> {
            // Изменение сложности игры
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
        // Текущий счет
        scoreLabel = new Label();
        scoreLabel.setText("Score: " + score);

        // Отображение наилучшего результата
        recordLabel = new Label("Record: " + record);

        // Вертикальная панель для элементов меню
        VBox menuLayout = new VBox(20);
        menuLayout.setAlignment(Pos.CENTER);
        menuLayout.getChildren().addAll(timeComboBox);
        menuLayout.getChildren().addAll(backgroundLabel, backgroundComboBox);
        menuLayout.getChildren().addAll(startGameButton,restartButton, reactionTestButton, changeDifficultyButton, exitButton, difficultyLabel, scoreLabel, recordLabel);
        menuLayout.getChildren().addAll(soundLabel, soundComboBox);

        // Установка фона
        Image backgroundImage = new Image("file:src/images/menu_bg.jpg");
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);

        // Главная панель
        BorderPane menuPane = new BorderPane();
        menuPane.setBackground(new Background(background));
        menuPane.setCenter(menuLayout);
        menuPane.setTop(timeComboBox);

        // Сцена меню
        Scene menuScene = new Scene(menuPane, WIDTH, LENGTH);
        menuScene.getStylesheets().add("file:src/style.css");

        return menuScene;
    }

    private void setGameBackground(BorderPane pane, String imagePath) {
        Image backgroundImage = new Image(imagePath);
        BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, BackgroundSize.DEFAULT);
        pane.setBackground(new Background(background));
    }

    private void resetGame() {
        score = 0;
        successfulHits = 0;
        missedHits = 0;
        scoreLabel.setText("Score: " + score);
        accuracyLabel.setText("Accuracy: --%");
        generateTargets();
        lastTime = System.nanoTime();
        targetTime = 20 * 1_000_000_000L;
        startTimer();
        primaryStage.setScene(gameScene);
    }

    private void updateAccuracyLabel() {
        if (successfulHits + missedHits > 0) {
            double accuracy = 100.0 * successfulHits / (successfulHits + missedHits);
            accuracyLabel.setText(String.format("Accuracy: %.2f%%", accuracy));
        }
    }

    private void updateBackground(String backgroundName) {
        String imagePath = BACKGROUND_MAP.getOrDefault(backgroundName, "");
        setGameBackground((BorderPane) gameScene.getRoot(), imagePath);
    }

    private Scene createGameScene() {
        accuracyLabel = new Label("Accuracy: 0.00%");
        accuracyLabel.setTextFill(Color.GOLD);

        Canvas gameCanvas = new Canvas(WIDTH, LENGTH);
        // Получаем графический контекст
        GraphicsContext gc = gameCanvas.getGraphicsContext2D();
        // Оставшееся времени
        timeLabel = new Label("Time Left: " + (targetTime / 1_000_000_000));
        timeLabel.setTextFill(Color.GOLD);
        // Панель для элементов игры
        VBox gameLayout = new VBox(20);
        gameLayout.setAlignment(Pos.TOP_CENTER);
        gameLayout.getChildren().addAll(timeLabel, gameCanvas);
        BorderPane gamePane = new BorderPane();
        gamePane.setCenter(gameLayout);
        HBox topPanel = new HBox();
        topPanel.setAlignment(Pos.CENTER_RIGHT);
        topPanel.setPadding(new Insets(10, 20, 10, 20));
        topPanel.setSpacing(10);
        topPanel.getChildren().addAll(timeLabel, accuracyLabel);  // Добавление меток в панель
        gamePane.setTop(topPanel);
        gamePane.setCenter(gameCanvas);
        // Сцена игры
        Scene gameScene = new Scene(gamePane, WIDTH, LENGTH);
        gamePane.setBackground(new Background(new BackgroundImage(new Image("file:src/images/bg_1.jpg"),
                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));
        // Обработка нажатия на полотно игры
        gameCanvas.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            boolean hit = false;
            for (Target target : targets) {
                if (target.contains(event.getX(), event.getY()) && !target.isPopped()) {
                    successfulHits++;
                    hit = true;
                    score++;
                    scoreLabel.setText("Score: " + score);
                    target.pop();
                    String selectedSound = soundComboBox.getValue();
                    String soundPath = "src/Sounds/" + selectedSound;
                    // запускает звук с заданной громкостью( от 0 до 1)
                    SoundPlayer.playSound(soundPath).setVolume((float) 0.5);
                    double x = new Random().nextDouble() * (WIDTH - targetRadius * 2) + targetRadius;
                    double y = new Random().nextDouble() * (LENGTH - targetRadius * 2) + targetRadius;
                    targets.add(new Target(x, y, targetRadius));
                    // Перерисовываем цели на полотне
                    drawTargets(gc);
                    // Прерываем цикл, чтобы обработать клик только на одном шарике за раз
                    break;
                }
            }
            if (!hit) {
                missedHits++;
            }
            updateAccuracyLabel();
        });
        targets.clear();
        generateTargets();

        drawTargets(gc);
        return gameScene;
    }

    private void drawTargets(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, LENGTH);
        for (Target target : targets) {
            target.draw(gc);
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
                    updateAccuracyLabel();
                    if (score > record) {
                        record = score;
                        recordLabel.setText("Record: " + record);
                    }
                    primaryStage.setScene(menuScene);
                }
                long secondsLeft = targetTime / 1_000_000_000;
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
            double x = random.nextDouble() * (WIDTH - targetRadius * 2) + targetRadius;
            double y = random.nextDouble() * (LENGTH - targetRadius * 2) + targetRadius;
            targets.add(new Target(x, y, targetRadius));
        }
    }
    private Scene createReactionTestScene() {
        VBox reactionLayout = new VBox(20);
        reactionLayout.setAlignment(Pos.CENTER);
        reactionLayout.setPadding(new Insets(20));

        Label reactionTestLabel = new Label("Reaction Test");
        reactionTestLabel.setStyle("-fx-font-size: 24px;");

        Button startReactionTestButton = new Button("Start Reaction Test");

        resultLabel = new Label();

        attemptLabels = new Label[5];
        for (int i = 0; i < attemptLabels.length; i++) {
            attemptLabels[i] = new Label("Attempt " + (i + 1) + ": -- ms");
        }

        targetPane = new StackPane();
        targetPane.setPrefSize(WIDTH, LENGTH);
        targetPane.setStyle("-fx-background-color: green;");
        targetPane.setOnMousePressed(this::handleReactionClick);

        startReactionTestButton.setOnAction(event -> startReactionTest());

        reactionLayout.getChildren().addAll(reactionTestLabel, startReactionTestButton, resultLabel);
        reactionLayout.getChildren().addAll(attemptLabels);
        reactionLayout.getChildren().add(targetPane);

        BackgroundFill backgroundFill = new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        reactionLayout.setBackground(background);

        Scene scene = new Scene(reactionLayout, WIDTH, LENGTH);
        scene.setOnKeyPressed(event -> {
            if (event.getCode().toString().equals("ESCAPE")) {
                event.consume();
            }
        });

        scene.getStylesheets().add("file:src/style.css");
        return scene;
    }

    private void startReactionTest() {
        reactionTimes = new long[5];
        attemptIndex = 0;
        resultLabel.setText("");
        for (int i = 0; i < attemptLabels.length; i++) {
            attemptLabels[i].setText("Attempt " + (i + 1) + ": -- ms");
        }
        displayNextTarget();
    }

    private void displayNextTarget() {
        if (attemptIndex >= 5) {
            long totalReactionTime = 0;
            for (long reactionTime : reactionTimes) {
                totalReactionTime += reactionTime;
            }
            long averageReactionTime = totalReactionTime / 5;
            resultLabel.setText("Average Reaction Time: " + averageReactionTime + " ms");
            return;
        }

        long delay = random.nextInt(3000) + 1000;
        reactionTimer = new AnimationTimer() {
            private long startTime = -1;
            private boolean targetVisible = false;

            @Override
            public void handle(long now) {
                if (startTime == -1) {
                    startTime = now;
                } else if (now - startTime >= delay * 1_000_000 && !targetVisible) {
                    showTarget();
                    targetVisible = true;
                    reactionTimer.stop();
                }
            }
        };
        reactionTimer.start();
    }

    private void showTarget() {
        Label targetLabel = new Label("Click!");
        targetLabel.setAlignment(Pos.CENTER);
        targetLabel.setPrefSize(1380, 355);
        targetLabel.setStyle("-fx-background-color: red; -fx-font-size: 36px; -fx-padding: 20px;");
        targetPane.getChildren().add(targetLabel);
        targetPane.setUserData(System.nanoTime());
    }

    private void handleReactionClick(MouseEvent event) {
        if (targetPane.getChildren().isEmpty()) {
            return;
        }

        long startTime = (long) targetPane.getUserData();
        long reactionTime = (System.nanoTime() - startTime) / 1_000_000;
        reactionTimes[attemptIndex] = reactionTime;
        attemptLabels[attemptIndex].setText("Attempt " + (attemptIndex + 1) + ": " + reactionTime + " ms");
        attemptIndex++;
        targetPane.getChildren().clear();

        displayNextTarget();
    }
    public static void main(String[] args) {
        launch(args);
    }
}