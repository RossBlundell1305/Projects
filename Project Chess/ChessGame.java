import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ChessGame extends JFrame {

    private static final int BOARD_SIZE = 8;
    private static final int SQUARE_SIZE = 80;

    private JPanel[][] squarePanels;
    private String[][] positions;

    private int selectedRow = -1;
    private int selectedCol = -1;

    private boolean whiteToMove = true; // Keeps track of which player's turn it is

    public ChessGame() {
        setTitle("Chess Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(SQUARE_SIZE * BOARD_SIZE, SQUARE_SIZE * BOARD_SIZE);
        setLocationRelativeTo(null);

        squarePanels = new JPanel[BOARD_SIZE][BOARD_SIZE];
        positions = getInitialPositions();

        JPanel chessboardPanel = createChessboardPanel();
        add(chessboardPanel);

        setVisible(true);
    }

    private JPanel createChessboardPanel() {
        JPanel chessboardPanel = new JPanel(new GridLayout(BOARD_SIZE, BOARD_SIZE));

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                JPanel squarePanel = new JPanel(new BorderLayout());
                squarePanel.setBackground(getSquareColor(row, col));

                JLabel pieceLabel = new JLabel(positions[row][col]);
                pieceLabel.setHorizontalAlignment(SwingConstants.CENTER);
                pieceLabel.setFont(new Font("SansSerif", Font.BOLD, 48));

                squarePanel.add(pieceLabel, BorderLayout.CENTER);
                chessboardPanel.add(squarePanel);

                final int finalRow = row;
                final int finalCol = col;
                squarePanel.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleSquareClick(finalRow, finalCol);
                    }
                });

                squarePanels[row][col] = squarePanel;
            }
        }

        return chessboardPanel;
    }

    private Color getSquareColor(int row, int col) {
        boolean isWhite = (row + col) % 2 == 0;
        return isWhite ? Color.WHITE : Color.LIGHT_GRAY;
    }

    //* Piece initial setup
     private String[][] getInitialPositions() {
        String[][] positions = new String[BOARD_SIZE][BOARD_SIZE];
        // Initialize with empty squares
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                positions[row][col] = "";
            }
        }

        // Set up white pieces
        positions[0][0] = "♜";
        positions[0][1] = "♞";
        positions[0][2] = "♝";
        positions[0][3] = "♛";
        positions[0][4] = "♚";
        positions[0][5] = "♝";
        positions[0][6] = "♞";
        positions[0][7] = "♜";
        for (int col = 0; col < BOARD_SIZE; col++) {
            positions[1][col] = "♟";
        }

        // Set up black pieces
        positions[7][0] = "♖";
        positions[7][1] = "♘";
        positions[7][2] = "♗";
        positions[7][3] = "♕";
        positions[7][4] = "♔";
        positions[7][5] = "♗";
        positions[7][6] = "♘";
        positions[7][7] = "♖";
        for (int col = 0; col < BOARD_SIZE; col++) {
            positions[6][col] = "♙";
        }

        return positions;
    }

    private void handleSquareClick(int row, int col) {
    if (selectedRow == -1 && selectedCol == -1) {
        // No piece selected yet
        if (!positions[row][col].isEmpty()) {
            // A piece is present in the clicked square
            selectedRow = row;
            selectedCol = col;
            squarePanels[row][col].setBackground(Color.YELLOW);
        }
    } else {
        // A piece is already selected, attempt to move it
        String selectedPiece = positions[selectedRow][selectedCol];
        if (isValidMove(selectedPiece, selectedRow, selectedCol, row, col)) {
            movePiece(selectedRow, selectedCol, row, col);
            whiteToMove = !whiteToMove; // Switch turns after successful move
        }

        // Reset selection
        squarePanels[selectedRow][selectedCol].setBackground(getSquareColor(selectedRow, selectedCol));
        selectedRow = -1;
        selectedCol = -1;
    }
}


    private boolean isCorrectTurn(int row, int col) {
        // Check if it's the current player's turn based on the color of the piece
        String piece = positions[row][col];
        if (whiteToMove && piece.charAt(0) == '♙' || piece.charAt(0) == '♖' || piece.charAt(0) == '♘'
                || piece.charAt(0) == '♗' || piece.charAt(0) == '♕' || piece.charAt(0) == '♔') {
            return true;
        } else return !whiteToMove && piece.charAt(0) == '♟' || piece.charAt(0) == '♜' || piece.charAt(0) == '♞'
                || piece.charAt(0) == '♝' || piece.charAt(0) == '♛' || piece.charAt(0) == '♚';
    }


    private boolean isValidMove(String piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (piece.isEmpty() || piece.length() < 2) {
            return false; // Invalid or empty squares can't make moves
        }

    char pieceType = piece.charAt(1);
    int rowDiff = Math.abs(toRow - fromRow);
    int colDiff = Math.abs(toCol - fromCol);

    switch (pieceType) {
        case '♙':
            // White pawn
            if (whiteToMove) {
                // Pawn can move one square forward
                if (fromCol == toCol && fromRow - toRow == 1 && positions[toRow][toCol].isEmpty()) {
                    return true;
                }
                // Pawn can move two squares forward from the starting position
                if (fromCol == toCol && fromRow == 6 && fromRow - toRow == 2 && positions[toRow][toCol].isEmpty()
                        && positions[toRow + 1][toCol].isEmpty()) {
                    return true;
                }
                // Pawn can capture diagonally
                if (Math.abs(fromCol - toCol) == 1 && fromRow - toRow == 1 && !positions[toRow][toCol].isEmpty()
                        && !isWhitePiece(toRow, toCol)) {
                    return true;
                }
            }
            break;
        case '♟':
            // Black pawn
            if (!whiteToMove) {
                // Pawn can move one square forward
                if (fromCol == toCol && toRow - fromRow == 1 && positions[toRow][toCol].isEmpty()) {
                    return true;
                }
                // Pawn can move two squares forward from the starting position
                if (fromCol == toCol && fromRow == 1 && toRow - fromRow == 2 && positions[toRow][toCol].isEmpty()
                        && positions[toRow - 1][toCol].isEmpty()) {
                    return true;
                }
                // Pawn can capture diagonally
                if (Math.abs(fromCol - toCol) == 1 && toRow - fromRow == 1 && !positions[toRow][toCol].isEmpty()
                        && isWhitePiece(toRow, toCol)) {
                    return true;
                }
            }
            break;
        case '♖':
        case '♜':
            // Rook
            return (fromRow == toRow && colDiff > 0 && isPathClear(fromRow, fromCol, toRow, toCol))
                    || (fromCol == toCol && rowDiff > 0 && isPathClear(fromRow, fromCol, toRow, toCol));
        case '♘':
        case '♞':
            // Knight (no need for path checking)
            return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
        case '♗':
        case '♝':
            // Bishop
            return rowDiff == colDiff && isPathClear(fromRow, fromCol, toRow, toCol);
        case '♕':
        case '♛':
            // Queen (combining bishop and rook moves)
            return (fromRow == toRow && colDiff > 0 && isPathClear(fromRow, fromCol, toRow, toCol))
                    || (fromCol == toCol && rowDiff > 0 && isPathClear(fromRow, fromCol, toRow, toCol))
                    || (rowDiff == colDiff && isPathClear(fromRow, fromCol, toRow, toCol));
        case '♔':
        case '♚':
            // King
            return rowDiff <= 1 && colDiff <= 1;
        default:
            return false;
    }
    return false;
}



    private boolean isWhitePiece(int row, int col) {
        String piece = positions[row][col];
        return !piece.isEmpty() && piece.charAt(0) == '♙' || piece.charAt(0) == '♖' || piece.charAt(0) == '♘'
                || piece.charAt(0) == '♗' || piece.charAt(0) == '♕' || piece.charAt(0) == '♔';
    }

    private boolean isPathClear(int fromRow, int fromCol, int toRow, int toCol) {
    int rowDiff = toRow - fromRow;
    int colDiff = toCol - fromCol;
    int rowStep = rowDiff == 0 ? 0 : rowDiff / Math.abs(rowDiff);
    int colStep = colDiff == 0 ? 0 : colDiff / Math.abs(colDiff);

    int row = fromRow + rowStep;
    int col = fromCol + colStep;

    while (row != toRow || col != toCol) {
        if (!positions[row][col].isEmpty()) {
            return false;
        }
        row += rowStep;
        col += colStep;
    }
    return true;
}


    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        // Update the chessboard state by moving the piece
        positions[toRow][toCol] = positions[fromRow][fromCol];
        positions[fromRow][fromCol] = "";

        // Update the visual representation of the chessboard
        squarePanels[toRow][toCol].removeAll();
        JLabel pieceLabel = new JLabel(positions[toRow][toCol]);
        pieceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        pieceLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        squarePanels[toRow][toCol].add(pieceLabel, BorderLayout.CENTER);

        squarePanels[fromRow][fromCol].removeAll();
        squarePanels[fromRow][fromCol].revalidate();
        squarePanels[fromRow][fromCol].repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGame::new);
    }
}
