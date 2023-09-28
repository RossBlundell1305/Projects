from tkinter import Label, Tk 
import time

# Create a GUI window
app_window = Tk() 
app_window.title("Digital Clock") 
app_window.geometry("420x150") 
app_window.resizable(1,1)

# Styling the label widget so that clock will look more attractive
text_font= ("Boulder", 68, 'bold')
background = "#7FFFD4"
foreground= "#000000"
border_width = 25

# Add Label widget for displaying the clock
label = Label(app_window, font=text_font, bg=background, fg=foreground, bd=border_width) 
label.grid(row=0, column=1)

# Function for updating the label
def digital_clock(): 
   time_live = time.strftime("%H:%M:%S")
   label.config(text=time_live) 
   label.after(200, digital_clock)

# Call the digital_clock() function
digital_clock()
app_window.mainloop()