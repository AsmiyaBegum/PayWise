# PayWise
Banking Application

**Purpose** :

This banking application enables users to log in using their mobile numbers and
validates their identity through a 4-digit PIN. It includes essential features such as a home
page, a transaction page, and a transaction success page.

**Goals :**

    ● **User Authentication**:
      - Validate the mobile number and a 4-digit PIN during the login process.
    ● **Home Screen**:
      - Display user details, including personal information.
      - Display bank account details, such as the account balance and transaction history.
    ● **Currency Information**:
      - Provide a list of available currencies on the home screen.
    ● **Transaction Screen**:
      - Allow users to transfer money to other accounts.
      - Phone number and correct currency amount validation should be done
    ●** Transaction Success Message**:
      - Display a success message or confirmation upon completing a transaction.
**Proposed Flow :**

    The application will consist of 4 screen login , home , send money and transfer success screen.
  
**Login Screen :**

    The login screen of this banking application offers the following functionality:
      ● Mobile Number Input: Users can enter their mobile number for authentication.
      ● Mobile Number Validation: The application verifies that the provided mobile number
    is a correct Indian number.
      ● OTP Generation and SMS: A random PIN is generated for 4 digits and sent to the
    user's mobile number using an SMS gateway service.
      ● OTP Confirmation Toast: A toast message is displayed to confirm the successful
    sending of the OTP.
      ● OTP Verification: Users enter the received OTP PIN for validation.
      ● Resending OTP: Users can request a new OTP after a few minutes, which remains
    valid for a short duration.

      ● Successful Validation: Upon successful OTP validation, users are granted access to
    the home screen, allowing them to explore the banking services and information
    provided by the application.
    
**Home Screen :**

      ● Display the user's name on the home screen, providing a personalised experience.
      ● Display the user's account details on the home screen.
      ● Mask the account balance for security purposes, showing only a masked or
    obscured version of the balance.
      ● Retrieve a list of currencies from an API response.
      ● Display the list of currencies on the home screen, allowing the user to view
    available currencies.
      ● Implement a "Send Money" option or button on the home screen.
      ● When the user clicks on the "Send" option, navigate to the send money screen.
      
**Send Money Screen** :

      ● User inputs recipient's mobile number.
        - Validation: Verify if it is a valid Indian number.
      ● User inputs transaction amount.
        - Validation: Verify if the amount is greater than zero.
      ● Send button to initiate the transaction.
      ● Re-validation of inputs upon clicking the send button.
      ● Transaction confirmation prompt for the user to confirm the money transfer.
      
**Transfer Success Page** :

      ● Transferred Amount: Displays the amount that has been successfully transacted.
      ● Recipient Details: Shows the details of the recipient who received the transfer.
      ● Date and Time: Indicates the exact date and time when the transaction was completed.
      ● Back to Home: Provides an option for the user to navigate back to the home page.


**Tech Stack**

  - Kotlin
  - Coroutines
  - Navigation Component
  - Constraint Layout
  - Twilio
  - Location API
  - Restful API
  - Retrofit
  - Data Binding
  - View Binding
  - MVVM
  - Encryption data using Encrypted Shared Preferences
  - ViewModel And LiveData
