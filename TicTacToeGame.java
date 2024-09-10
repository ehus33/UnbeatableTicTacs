import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TicTacToeGame extends JFrame {
    private JButton[][] buttons;
    private int boardSize;
    private int winCondition;
    private char currentPlayer;
    private boolean gameEnded;
    private boolean humanVsAI;

    public TicTacToeGame(int boardSize, int winCondition, boolean humanVsAI) {
        this.boardSize = boardSize;
        this.winCondition = winCondition;
        this.humanVsAI = humanVsAI;
        this.currentPlayer = 'X';
        this.gameEnded = false;

        buttons = new JButton[boardSize][boardSize];
        setLayout(new GridLayout(boardSize, boardSize));

        initializeButtons();
        setTitle("Tic Tac Toe");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initializeButtons() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                buttons[row][col] = new JButton("");
                buttons[row][col].setFont(new Font("Arial", Font.PLAIN, 60));
                buttons[row][col].setFocusPainted(false);
                buttons[row][col].addActionListener(new ButtonClickListener(row, col));
                add(buttons[row][col]);
            }
        }
    }

    private class ButtonClickListener implements ActionListener {
        int row, col;

        public ButtonClickListener(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (buttons[row][col].getText().equals("") && !gameEnded) {
                buttons[row][col].setText(String.valueOf(currentPlayer));
                if (checkForWin(row, col)) {
                    gameEnded = true;
                    JOptionPane.showMessageDialog(null, "Player " + currentPlayer + " wins!");
                } else if (isBoardFull()) {
                    gameEnded = true;
                    JOptionPane.showMessageDialog(null, "It's a draw!");
                } else {
                    switchPlayer();
                    if (humanVsAI && currentPlayer == 'O' && !gameEnded) {
                        aiMoveWithTimeout(15); // AI makes a move with a timeout
                    }
                }
            }
        }
    }

    private void switchPlayer() {
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
    }

    private boolean isBoardFull() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (buttons[row][col].getText().equals("")) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkForWin(int row, int col) {
        return (checkDirection(row, col, 1, 0) || // Check row
                checkDirection(row, col, 0, 1) || // Check column
                checkDirection(row, col, 1, 1) || // Check diagonal
                checkDirection(row, col, 1, -1)); // Check anti-diagonal
    }

    private boolean checkDirection(int row, int col, int rowDir, int colDir) {
        String symbol = buttons[row][col].getText();
        int count = 0;

        int r = row, c = col;
        while (r >= 0 && r < boardSize && c >= 0 && c < boardSize && buttons[r][c].getText().equals(symbol)) {
            count++;
            r += rowDir;
            c += colDir;
        }

        r = row - rowDir;
        c = col - colDir;
        while (r >= 0 && r < boardSize && c >= 0 && c < boardSize && buttons[r][c].getText().equals(symbol)) {
            count++;
            r -= rowDir;
            c -= colDir;
        }

        return count >= winCondition;
    }

    // AI move with timeout check
    private void aiMoveWithTimeout(int timeoutSeconds) {
        Thread aiThread = new Thread(() -> {
            int[] bestMove = minimax(0, true);
            buttons[bestMove[1]][bestMove[2]].setText("O");

            if (checkForWin(bestMove[1], bestMove[2])) {
                gameEnded = true;
                JOptionPane.showMessageDialog(null, "AI wins!");
            } else if (isBoardFull()) {
                gameEnded = true;
                JOptionPane.showMessageDialog(null, "It's a draw!");
            } else {
                switchPlayer();
            }
        });

        aiThread.start();

        try {
            // Wait for the AI thread to finish for the given timeout period
            aiThread.join(timeoutSeconds * 1000);

            if (aiThread.isAlive()) {
                System.out.println("AI took too long to make a move. Exiting.");
                aiThread.interrupt(); // Interrupt the thread (optional)
                System.exit(1); // Forcefully exit the application
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private int[] minimax(int depth, boolean isMaximizing) {
        int maxDepth = 4;
        int score = evaluateBoard();
        if (score == 10)
            return new int[] { score - depth };
        if (score == -10)
            return new int[] { score + depth };
        if (isBoardFull() || depth == maxDepth)
            return new int[] { 0 };

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            int bestRow = -1, bestCol = -1;

            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    if (buttons[row][col].getText().equals("")) {
                        buttons[row][col].setText("O");
                        int[] result = minimax(depth + 1, false);
                        buttons[row][col].setText("");
                        if (result[0] > bestScore) {
                            bestScore = result[0];
                            bestRow = row;
                            bestCol = col;
                        }
                    }
                }
            }
            return new int[] { bestScore, bestRow, bestCol };
        } else {
            int bestScore = Integer.MAX_VALUE;
            int bestRow = -1, bestCol = -1;

            for (int row = 0; row < boardSize; row++) {
                for (int col = 0; col < boardSize; col++) {
                    if (buttons[row][col].getText().equals("")) {
                        buttons[row][col].setText("X");
                        int[] result = minimax(depth + 1, true);
                        buttons[row][col].setText("");
                        if (result[0] < bestScore) {
                            bestScore = result[0];
                            bestRow = row;
                            bestCol = col;
                        }
                    }
                }
            }
            return new int[] { bestScore, bestRow, bestCol };
        }
    }

    private int evaluateBoard() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                if (!buttons[row][col].getText().equals("")) {
                    if (checkForWin(row, col)) {
                        return buttons[row][col].getText().equals("O") ? 10 : -10;
                    }
                }
            }
        }
        return 0;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String[] gameModes = { "Human vs Human", "Human vs AI" };
            int gameModeChoice = JOptionPane.showOptionDialog(null, "Choose Game Mode", "Game Mode",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, gameModes, gameModes[0]);

            String boardSizeInput = JOptionPane.showInputDialog("Enter board size:");
            String winConditionInput = JOptionPane.showInputDialog("Enter number of X's or O's in a row to win:");

            try {
                int boardSize = Integer.parseInt(boardSizeInput);
                int winCondition = Integer.parseInt(winConditionInput);

                if (boardSize >= 3 && winCondition <= boardSize) {
                    boolean humanVsAI = (gameModeChoice == 1);
                    new TicTacToeGame(boardSize, winCondition, humanVsAI);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Invalid input. Board size must be >= 3 and win condition <= board size.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Please enter valid numbers.");
            }
        });
    }
}
