import java.sql.*;
import java.util.Scanner;

public class nm {

    private static final int GRID_SIZE = 9;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Database connection details
        String jdbcURL = "jdbc:mysql://localhost:3306/puzzle";
        String dbUser = "root";
        String dbPassword = "Nagumeena_22"; // Replace with your actual password

        try (Connection connection = DriverManager.getConnection(jdbcURL, dbUser, dbPassword)) {
            System.out.println("Connected to the database!");

            System.out.println("Enter the Sudoku puzzle row by row (use 0 for empty cells):");
            int[][] board = new int[GRID_SIZE][GRID_SIZE];

            // Read the Sudoku board
            for (int i = 0; i < GRID_SIZE; i++) {
                System.out.println("Enter row " + (i + 1) + ": ");
                for (int j = 0; j < GRID_SIZE; j++) {
                    board[i][j] = scanner.nextInt();
                }
            }

            // Convert board to string for database storage
            String puzzleString = boardToString(board);

            // Solve the puzzle
            System.out.println("Solving the Sudoku puzzle...");
            if (solveSudoku(board)) {
                System.out.println("Solved Sudoku:");
                printBoard(board);

                // Convert solved board to string
                String solutionString = boardToString(board);

                // Save puzzle and solution to the database
                savePuzzleToDatabase(connection, puzzleString, solutionString);
            } else {
                System.out.println("This Sudoku puzzle cannot be solved.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        scanner.close();
    }

    private static boolean solveSudoku(int[][] board) {
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (board[row][col] == 0) { // Find an empty cell
                    for (int num = 1; num <= 9; num++) {
                        if (isValidPlacement(board, num, row, col)) {
                            board[row][col] = num;

                            // Recursively attempt to solve the rest
                            if (solveSudoku(board)) {
                                return true;
                            }

                            // Backtrack
                            board[row][col] = 0;
                        }
                    }

                    return false; // No valid number found, so backtrack
                }
            }
        }
        return true; // All cells are filled
    }

    private static boolean isValidPlacement(int[][] board, int number, int row, int col) {
        for (int i = 0; i < GRID_SIZE; i++) {
            if (board[row][i] == number || board[i][col] == number) {
                return false;
            }
        }

        int subgridRowStart = row - row % 3;
        int subgridColStart = col - col % 3;
        for (int i = subgridRowStart; i < subgridRowStart + 3; i++) {
            for (int j = subgridColStart; j < subgridColStart + 3; j++) {
                if (board[i][j] == number) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void printBoard(int[][] board) {
        for (int row = 0; row < GRID_SIZE; row++) {
            if (row % 3 == 0 && row != 0) {
                System.out.println("-----------");
            }
            for (int col = 0; col < GRID_SIZE; col++) {
                if (col % 3 == 0 && col != 0) {
                    System.out.print("|");
                }
                System.out.print(board[row][col]);
            }
            System.out.println();
        }
    }

    private static String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                sb.append(board[row][col]);
            }
        }
        return sb.toString();
    }

    private static void savePuzzleToDatabase(Connection connection, String puzzle, String solution) throws SQLException {
        String sql = "INSERT INTO Puzzles (puzzle, solution) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, puzzle);
            statement.setString(2, solution);
            statement.executeUpdate();
            System.out.println("Puzzle saved to the database.");
        }
    }
}
