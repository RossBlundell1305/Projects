import random
import time 

#range of dice values
min = 1
max = 20

#loop to roll dice
roll_again = "yes"

#loop
while roll_again == "yes" or roll_again == "y":
    dice_status = input("Is this an advantage or disadvantage roll? (a/d) : ")
    print("Rolling the dice...") 
    time.sleep(2)
    print("The values are....")
    time.sleep(2)
    random1 = random.randint(min, max)
    random2 = random.randint(min, max)
    print (random1)
    time.sleep(2)
    print (random2)
    time.sleep(2)
    
    if dice_status == "a":
        print("Advantage roll")
        if random1 > random2:
            print(random1)
        else:
            print(random2)

    elif dice_status == "d":
        print("Disadvantage roll")
        if random1 < random2:
            print(random1)
        else:
            print(random2)

    roll_again = input("Roll the dice again? (y/n) :")
    time.sleep(2)
    if roll_again == "n":
        print("Thanks for playing!")
        break