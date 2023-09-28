import tkinter as tk
from tkinter import messagebox
import random

#! When selecting different game mode after already playing, gamemode will change but player turn will not update to new game mode. 

# Global variables
player_turn = "X"  # Initialize the player turn
board = [" " for _ in range(9)]  # Create an empty game board
game_mode = ""  # Store the selected game mode ("2P" for two-player, "AI" for AI)
player1_name = ""  # Player 1's name
player2_name = ""  # Player 2's name

# Function to set the game mode when a button is clicked
def set_game_mode(mode):
    global game_mode, player1_name, player2_name
    game_mode = mode
    if mode == "2P":
        # Create a dialog window for player names
        dialog = tk.Toplevel(root)
        dialog.title("Player Names")
        tk.Label(dialog, text="Enter Player 1's name:").pack()
        entry1 = tk.Entry(dialog)
        entry1.pack()
        tk.Label(dialog, text="Enter Player 2's name:").pack()
        entry2 = tk.Entry(dialog)
        entry2.pack()
        confirm_button = tk.Button(dialog, text="Confirm",
                                   command=lambda: get_player_names(entry1.get(), entry2.get(), dialog))
        confirm_button.pack()

# Function to get player names from the dialog
def get_player_names(name1, name2, dialog):
    global player1_name, player2_name
    player1_name = name1
    player2_name = name2
    dialog.destroy()  # Close the dialog window
    messagebox.showinfo("Tic-Tac-Toe", "Two-player mode selected.")
    update_player_turn_label()  # Start the game with the first player's turn
    new_game()

# Function to update the label showing the current player's turn
def update_player_turn_label():
    if player_turn == "X":
        player_turn_label.config(text=f"Turn: {player1_name} (X)")
    else:
        player_turn_label.config(text=f"Turn: {player2_name} (O)")

# Create the main GUI window
root = tk.Tk()
root.title("Tic-Tac-Toe")

# Create game mode buttons
two_player_button = tk.Button(root, text="2 Player Mode",
                              command=lambda: set_game_mode("2P"))
ai_button = tk.Button(root, text="AI Mode", command=lambda: set_game_mode("AI"))

two_player_button.grid(row=0, column=0)
ai_button.grid(row=0, column=1)

# Create a label to show the current player's turn
player_turn_label = tk.Label(root, text="")
player_turn_label.grid(row=5, columnspan=3)

# Function to check for a win or a tie
def check_win():
    # Check rows, columns, and diagonals for a win
    for i in range(0, 9, 3):
        if board[i] == board[i + 1] == board[i + 2] != " ":
            return True
    for i in range(3):
        if board[i] == board[i + 3] == board[i + 6] != " ":
            return True
    if board[0] == board[4] == board[8] != " ":
        return True
    if board[2] == board[4] == board[6] != " ":
        return True
    # Check for a tie
    if " " not in board:
        return "Tie"
    return False

# Create a function to handle player clicks
def player_click(index):
    global player_turn
    if board[index] == " ":
        board[index] = player_turn
        buttons[index].config(text=player_turn)
        result = check_win()
        if result:
            if result == "Tie":
                messagebox.showinfo("Tic-Tac-Toe", "It's a tie!")
            else:
                winner_name = player1_name if player_turn == "X" else player2_name
                messagebox.showinfo("Tic-Tac-Toe", f"{winner_name} wins!")
            new_game()  # Start a new game without closing the application
        else:
            player_turn = "O" if player_turn == "X" else "X"
            update_player_turn_label()  # Update the player turn label
            # If the game mode is AI and it's the AI's turn, let the AI make a move
            if game_mode == "AI" and player_turn == "O":
                ai_move()

# Create the game board buttons
buttons = []
for i in range(9):
    row = i // 3
    col = i % 3
    button = tk.Button(root, text=" ", width=10, height=2,
                       command=lambda i=i: player_click(i))
    button.grid(row=row + 1, column=col)  # Adjust the row to leave space for the mode buttons
    buttons.append(button)

# Create a function for the AI's move
def ai_move():
    available_moves = [i for i in range(9) if board[i] == " "]
    if available_moves:
        ai_choice = random.choice(available_moves)
        player_click(ai_choice)

# Create a function to start a new game
def new_game():
    global player_turn, board
    player_turn = "X"
    board = [" " for _ in range(9)]
    for button in buttons:
        button.config(text=" ")
    # If the game mode is AI and it's the AI's turn, let the AI make a move
    if game_mode == "AI" and player_turn == "O":
        ai_move()

# Create a "New Game" button
new_game_button = tk.Button(root, text="New Game", command=new_game)
new_game_button.grid(row=6, columnspan=3)  # Adjust the row to leave space for the mode buttons

# Main loop
root.mainloop()
