import random

#! Game works but bankroll resets after every round, Issue with game loop

# Define card deck --------------------------------------------------------------------------------------------------------------------------------------------

suits = ['Hearts', 'Diamonds', 'Clubs', 'Spades']
values = ['2', '3', '4', '5', '6', '7', '8', '9', '10', 'Jack', 'Queen', 'King', 'Ace']

# Define functions --------------------------------------------------------------------------------------------------------------------------------------------

def shuffle_deck():
    """
    Shuffle the deck of cards.
    """
    deck = [{'Suit': suit, 'Value': value} for suit in suits for value in values]
    random.shuffle(deck)
    return deck

def deal_initial_hands(deck):
    """
    Deal two cards each to the player and the dealer.
    """
    player_hand = [deck.pop(), deck.pop()]
    dealer_hand = [deck.pop(), deck.pop()]
    return player_hand, dealer_hand

def calculate_hand_value(hand):
    """
    Calculate the total value of a hand.
    """
    value = 0
    num_aces = 0
    
    for card in hand:
        card_value = card['Value']
        if card_value.isdigit():
            value += int(card_value)
        elif card_value != 'Ace':
            value += 10
        else:
            num_aces += 1
    
    # Handle Aces as 1 or 11
    for _ in range(num_aces):
        if value + 11 <= 21:
            value += 11
        else:
            value += 1
    
    return value

def player_turn(deck, player_hand):
    """
    Allow the player to hit or stand.
    """
    while True:
        print("Your hand:", player_hand, "Value:", calculate_hand_value(player_hand))
        choice = input("Do you want to hit or stand? ").lower()
        if choice == 'hit':
            player_hand.append(deck.pop())
            if calculate_hand_value(player_hand) > 21:
                print("Busted! Your hand value is over 21.")
                break
        elif choice == 'stand':
            break
        else:
            print("Invalid choice. Please enter 'hit' or 'stand'.")

def dealer_turn(deck, dealer_hand):
    """
    Simulate the dealer's turn.
    """
    while calculate_hand_value(dealer_hand) < 17:
        dealer_hand.append(deck.pop())

def determine_winner(player_hand, dealer_hand):
    """
    Determine the winner of the round.
    """
    player_value = calculate_hand_value(player_hand)
    dealer_value = calculate_hand_value(dealer_hand)
    
    if player_value > 21:
        return "Dealer"
    elif dealer_value > 21:
        return "Player"
    elif player_value > dealer_value:
        return "Player"
    elif dealer_value > player_value:
        return "Dealer"
    else:
        return "Tie"

def end_of_round(player_hand, dealer_hand, bet, bankroll, winner):
    """
    Display the results of the round and update the player's bankroll.
    """
    print("Player's hand:", player_hand, "Value:", calculate_hand_value(player_hand))
    print("Dealer's hand:", dealer_hand, "Value:", calculate_hand_value(dealer_hand))
    print("Winner:", winner)
    
    if winner == "Player":
        bankroll += bet
    elif winner == "Dealer":
        bankroll -= bet
    
    print("Player's bankroll:", bankroll)

def game_over(bankroll):
    """
    End the game.
    """
    if bankroll <= 0:
        print("Game over! You're out of money.")
    else:
        print("Thanks for playing! Your final bankroll:", bankroll)

# Initialize the bankroll -------------------------------------------------------------------------------------------------------------------------------

bankroll = 1000  # Starting bankroll

# Game loop --------------------------------------------------------------------------------------------------------------------------------------------

while bankroll > 0:
    print("\nWelcome to Blackjack! Your bankroll:", bankroll)

    # Shuffle the deck and deal initial hands
    deck = shuffle_deck()
    player_hand, dealer_hand = deal_initial_hands(deck)

    # Place a bet
    while True:
        try:
            bet = int(input("Place your bet: "))
            if bet <= 0 or bet > bankroll:
                print("Invalid bet amount. Please bet between 1 and", bankroll)
            else:
                break
        except ValueError:
            print("Invalid input. Please enter a valid bet.")

    # Player's turn
    player_turn(deck, player_hand)

    # Dealer's turn
    dealer_turn(deck, dealer_hand)

    # Determine the winner
    winner = determine_winner(player_hand, dealer_hand)

    # End of round
    end_of_round(player_hand, dealer_hand, bet, bankroll, winner)

    # Check if the player wants to continue playing
    play_again = input("Do you want to play another round? (yes/no) ").lower()
    if play_again != 'yes':
        break

# Game over --------------------------------------------------------------------------------------------------------------------------------------------

game_over(bankroll)
