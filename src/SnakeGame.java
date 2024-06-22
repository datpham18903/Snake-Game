import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.LinkedList;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener, MouseListener, MouseMotionListener {
    // Game constants
    private static final int SLUG_FRAME_RATE = 10;
    private static final int WORM_FRAME_RATE = 20;
    private static final int PYTHON_FRAME_RATE = 30;
    private static final int SPEED_INCREMENT = 1;
    private static final int CELL_SIZE = 25;

    // Color scheme
    private static final Color BACKGROUND_COLOR = new Color(155, 186, 90);
    private static final Color TEXT_COLOR = new Color(43, 51, 26);
    private static final Color SNAKE_COLOR = new Color(43, 51, 26);
    private static final Color FOOD_COLOR = new Color(220, 20, 60);

    // Fonts
    private static final Font TITLE_FONT = new Font("Showcard Gothic", Font.BOLD, 90);
    private static final Font BUTTON_FONT = new Font("Showcard Gothic", Font.BOLD, 30);
    private static final Font SCORE_FONT = new Font("Showcard Gothic", Font.PLAIN, 20);

    // Game state variables
    private boolean gameStarted = false;
    private boolean gameOver = false;
    private boolean welcomeScreen = true;
    private boolean newHighScore = false;

    // Game dimensions
    private final int width;
    private final int height;

    // Game settings
    private int frameRate = WORM_FRAME_RATE;
    private int scoreMultiplier = 2;
    private int score = 0;
    private int highScore = 0;

    // Game objects
    private GamePoint food;
    private Direction direction = Direction.RIGHT;
    private Direction newDirection = Direction.RIGHT;
    private final LinkedList<GamePoint> snake = new LinkedList<>();
    private Timer timer;
    private final Random random = new Random();

    // UI elements
    private Rectangle slugButton;
    private Rectangle wormButton;
    private Rectangle pythonButton;
    private String hoveredButton = null;

    // Constructor
    public SnakeGame(final int width, final int height) {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
        setBackground(BACKGROUND_COLOR);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                handleKeyEvent(e.getKeyCode());
            }
        });
        addMouseListener(this);
        addMouseMotionListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);
    }

    // Initializes and starts the game
    public void startGame() {
        if (timer != null) {
            timer.stop();
        }
        resetGameData();
        timer = new Timer(1000 / frameRate, this);
        timer.start();
    }

    // Handles key events based on the current game state
    private void handleKeyEvent(final int keyCode) {
        if (welcomeScreen) {
            handleWelcomeScreen(keyCode);
        } else if (!gameStarted && keyCode == KeyEvent.VK_SPACE) {
            gameStarted = true;
        } else if (!gameOver) {
            handleGameInProgress(keyCode);
        } else if (keyCode == KeyEvent.VK_SPACE) {
            returnToWelcomeScreen();
        }
    }

    // Handles key events on the welcome screen
    private void handleWelcomeScreen(final int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_S -> startGameWithFrameRate(SLUG_FRAME_RATE);
            case KeyEvent.VK_W -> startGameWithFrameRate(WORM_FRAME_RATE);
            case KeyEvent.VK_P -> startGameWithFrameRate(PYTHON_FRAME_RATE);
        }
    }

    // Starts the game with the specified frame rate
    private void startGameWithFrameRate(int frameRate) {
        this.frameRate = frameRate;
        scoreMultiplier = switch (frameRate) {
            case SLUG_FRAME_RATE -> 1;
            case WORM_FRAME_RATE -> 2;
            case PYTHON_FRAME_RATE -> 3;
            default -> 2;
        };
        welcomeScreen = false;
        gameStarted = true;
        startGame();
    }

    // Handles key events during active gameplay
    private void handleGameInProgress(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP -> updateDirection(Direction.UP, Direction.DOWN);
            case KeyEvent.VK_DOWN -> updateDirection(Direction.DOWN, Direction.UP);
            case KeyEvent.VK_RIGHT -> updateDirection(Direction.RIGHT, Direction.LEFT);
            case KeyEvent.VK_LEFT -> updateDirection(Direction.LEFT, Direction.RIGHT);
        }
    }

    // Updates the snake's direction if the new direction is not opposite to the current direction
    private void updateDirection(Direction newDir, Direction oppositeDir) {
        if (direction != oppositeDir) {
            newDirection = newDir;
        }
    }

    // Resets all game data to initial state
    private void resetGameData() {
        snake.clear();
        snake.add(new GamePoint(width / 2, height / 2));
        direction = Direction.RIGHT;
        newDirection = Direction.RIGHT;
        generateFood();
        newHighScore = false;
        score = 0;
    }

    // Generates a new food item at a random location not occupied by the snake
    private void generateFood() {
        do {
            int foodX = random.nextInt(width / CELL_SIZE) * CELL_SIZE;
            int foodY = random.nextInt(height / CELL_SIZE) * CELL_SIZE;
            if (foodY >= height - CELL_SIZE) {
                foodY -= CELL_SIZE;
            }
            food = new GamePoint(foodX, foodY);
        } while (snake.contains(food));
    }

    // Main rendering method
    @Override
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (welcomeScreen) {
            drawWelcomeScreen(g2d);
        } else if (gameOver) {
            drawGameOverScreen(g2d);
        } else {
            drawGameInProgress(g2d);
        }

        drawScores(g2d);
    }

    // Draws the welcome screen
    private void drawWelcomeScreen(Graphics2D g2d) {
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(TITLE_FONT);
        drawCenteredString(g2d, "snake", height / 2 - 60);
        g2d.setFont(SCORE_FONT);
        drawCenteredString(g2d, "CHOOSE LEVEL:", height / 2);

        setupButtons();
        drawButton(g2d, slugButton, "SLUG");
        drawButton(g2d, wormButton, "WORM");
        drawButton(g2d, pythonButton, "PYTHON");
    }

    // Sets up the difficulty selection buttons
    private void setupButtons() {
        FontMetrics metrics = getFontMetrics(BUTTON_FONT);
        int slugWidth = metrics.stringWidth("SLUG");
        int wormWidth = metrics.stringWidth("WORM");
        int pythonWidth = metrics.stringWidth("PYTHON");

        int baseY = height / 2 + 40;
        int buttonHeight = 40;
        int buttonSpacing = 60;

        slugButton = new Rectangle((width - (slugWidth + wormWidth + pythonWidth + buttonSpacing * 2)) / 2, baseY, slugWidth, buttonHeight);
        wormButton = new Rectangle(slugButton.x + slugWidth + buttonSpacing, baseY, wormWidth, buttonHeight);
        pythonButton = new Rectangle(wormButton.x + wormWidth + buttonSpacing, baseY, pythonWidth, buttonHeight);
    }

    // Draws a button with hover effect
    private void drawButton(Graphics2D g2d, Rectangle button, String text) {
        boolean isHovered = text.equals(hoveredButton);
        g2d.setColor(isHovered ? TEXT_COLOR : BACKGROUND_COLOR);
        g2d.fillRect(button.x - 5, button.y - 5, button.width + 30, button.height + 5);

        g2d.setColor(isHovered ? BACKGROUND_COLOR : TEXT_COLOR);
        g2d.setFont(BUTTON_FONT);
        g2d.drawString(text, button.x + 10, button.y + button.height - 10);
    }

    // Draws the snake and food during active gameplay
    private void drawGameInProgress(Graphics2D g2d) {
        g2d.setColor(FOOD_COLOR);
        g2d.fillRect(food.x, food.y, CELL_SIZE, CELL_SIZE);

        g2d.setColor(SNAKE_COLOR);
        for (GamePoint point : snake) {
            g2d.fillRect(point.x, point.y, CELL_SIZE, CELL_SIZE);
        }
    }

    // Draws the current score and high score
    private void drawScores(Graphics2D g2d) {
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(SCORE_FONT);

        g2d.drawString("Score: " + score, 10, height - 10);

        String highScoreText = "High Score: " + highScore;
        int highScoreTextWidth = g2d.getFontMetrics().stringWidth(highScoreText);
        g2d.drawString(highScoreText, width - highScoreTextWidth - 10, height - 10);
    }

    // Draws the game over screen
    private void drawGameOverScreen(Graphics2D g2d) {
        g2d.setColor(TEXT_COLOR);
        g2d.setFont(TITLE_FONT);
        drawCenteredString(g2d, newHighScore ? "best score!" : "game over!", height / 2);
        g2d.setFont(SCORE_FONT);
        drawCenteredString(g2d, "Press Space to Restart", height / 2 + 60);
    }

    // Utility method to draw centered text
    private void drawCenteredString(Graphics2D g2d, String text, int y) {
        FontMetrics metrics = g2d.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }

    // Moves the snake and handles collisions
    private void move() {
        direction = newDirection;
        GamePoint head = snake.getFirst();

        // Determine new head position based on direction
        GamePoint newHead = switch (direction) {
            case UP -> new GamePoint(head.x, head.y - CELL_SIZE);
            case DOWN -> new GamePoint(head.x, head.y + CELL_SIZE);
            case RIGHT -> new GamePoint(head.x + CELL_SIZE, head.y);
            case LEFT -> new GamePoint(head.x - CELL_SIZE, head.y);
        };

        // Check for collision
        if (isCollision(newHead)) {
            gameOver = true;
            if (score > highScore) {
                highScore = score;
                newHighScore = true;
            }
        } else {
            // Move snake
            snake.addFirst(newHead);
            if (newHead.equals(food)) {
                // Snake ate food
                generateFood();
                score += scoreMultiplier;
                // Increase speed if not at maximum
                if (frameRate < PYTHON_FRAME_RATE) {
                    frameRate += SPEED_INCREMENT;
                    timer.setDelay(1000 / frameRate);
                }
            } else {
                // Remove tail if food wasn't eaten
                snake.removeLast();
            }
        }
    }

    // Checks if the given point collides with the snake or game boundaries
    private boolean isCollision(GamePoint head) {
        return head.x < 0 || head.x >= width || head.y < 0 || head.y >= height - CELL_SIZE
                || snake.subList(1, snake.size()).contains(head);
    }

    // Game loop
    @Override
    public void actionPerformed(final ActionEvent e) {
        if (gameStarted && !gameOver) {
            move();
        }
        repaint();
    }

    // Resets the game state to return to the welcome screen
    private void returnToWelcomeScreen() {
        gameStarted = false;
        gameOver = false;
        welcomeScreen = true;
        resetGameData();
    }

    // Represents a point in the game grid
    private record GamePoint(int x, int y) {}

    // Represents the direction of snake movement
    private enum Direction { UP, DOWN, RIGHT, LEFT }

    // Mouse click handler
    @Override
    public void mouseClicked(MouseEvent e) {
        if (welcomeScreen) {
            Point clickPoint = e.getPoint();
            if (slugButton.contains(clickPoint)) {
                startGameWithFrameRate(SLUG_FRAME_RATE);
            } else if (wormButton.contains(clickPoint)) {
                startGameWithFrameRate(WORM_FRAME_RATE);
            } else if (pythonButton.contains(clickPoint)) {
                startGameWithFrameRate(PYTHON_FRAME_RATE);
            }
        }
    }

    // Mouse movement handler for button hover effects
    @Override
    public void mouseMoved(MouseEvent e) {
        if (welcomeScreen) {
            Point hoverPoint = e.getPoint();
            hoveredButton = null;
            if (slugButton.contains(hoverPoint)) {
                hoveredButton = "SLUG";
            } else if (wormButton.contains(hoverPoint)) {
                hoveredButton = "WORM";
            } else if (pythonButton.contains(hoverPoint)) {
                hoveredButton = "PYTHON";
            }
            setCursor(hoveredButton != null ? Cursor.getPredefinedCursor(Cursor.HAND_CURSOR) : Cursor.getDefaultCursor());
            repaint();
        }
    }

    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
}