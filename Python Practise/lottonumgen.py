import random

choice = input("Euromillions = 1. Irish Lotto = 2. Please enter option 1 or 2: ")

choice = int(choice)

if choice == 1:
    print("EuroMillions numbers: ")

    num1 = random.randint(1, 50)
    num2 = random.randint(1, 50)
    num3 = random.randint(1, 50)
    num4 = random.randint(1, 50)
    num5 = random.randint(1, 50)
    bonus1 = random.randint(1, 12)
    bonus2 = random.randint(1, 12)

    nums = [num1, num2, num3, num4, num5]
    bonus = [bonus1, bonus2]
    nums.sort()
    bonus.sort()

    print(nums, bonus)

elif choice == 2:
    print("Irish Lotto numbers: ")

    num1 = random.randint(1, 47)
    num2 = random.randint(1, 47)
    num3 = random.randint(1, 47)
    num4 = random.randint(1, 47)
    num5 = random.randint(1, 47)
    num6 = random.randint(1, 47)

    nums = [num1, num2, num3, num4, num5, num6]
    nums.sort()

    print(nums)
    
else:
    print("Invalid option. Please enter 1 or 2.")
