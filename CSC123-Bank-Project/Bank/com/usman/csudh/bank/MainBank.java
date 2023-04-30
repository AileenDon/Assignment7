package com.usman.csudh.bank;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.usman.csudh.bank.core.*;
import com.usman.csudh.util.UIManager;


public class MainBank {

	//All messages are declared as constants to make it easier to change. Also, to ensure future proofing in case the application need to be made available
	//in more than one language

	//case 1
	private static final String MSG_ACCOUNT_CURRENCY = "Please enter the account currency: ";
	private static boolean exchangeRateLoaded=true;
	private static Map<String, Double> currencyName = new HashMap<>();
	
	public static final String MSG_ACCOUNT_OPENED = "%nAccount opened, account number is: %s%n%n";
	public static final String MSG_ACCOUNT_CLOSED = "%nAccount number %s has been closed, balance is %s%n%n";
	public static final String MSG_ACCOUNT_NOT_FOUND = "%nAccount number %s not found! %n%n";
	public static final String MSG_FIRST_NAME = "Enter first name:  ";
	public static final String MSG_LAST_NAME = "Enter last name:  ";
	public static final String MSG_SSN = "Enter Social Security Number:  ";
	public static final String MSG_ACCOUNT_NAME = "Enter account name:  ";
	public static final String MSG_ACCOUNT_OD_LIMIT = "Enter overdraft limit:  ";
	public static final String MSG_ACCOUNT_CREDIT_LIMIT = "Enter credit limit:  ";
	public static final String MSG_AMOUNT = "Enter amount: ";
	public static final String MSG_ACCOUNT_NUMBER = "Enter account number: ";
	public static final String MSG_ACCOUNT_ACTION = "%n%s was %s, account balance is: %s%n%n";
	public static final String MSG_ENTER_FROM_CURRENCY="The currency you are selling :";
	public static final String MSG_ENTER_TO_CURRENCY="The currency you are buying :";
	public static final String MSG_ENTER_AMOUNT="The amount you are selling :";


	//Declare main menu and prompt to accept user input
	public static final String[] menuOptions = { "Open Checking Account%n","Open Saving Account%n", "List Accounts%n","View Statement%n",
			"Show Account Information%n","Deposit Funds%n", "Withdraw Funds%n", "Close an Account%n","currency conversion%n","Exit%n" };
	public static final String MSG_PROMPT = "%nEnter choice: ";

	
	//Declare streams to accept user input / provide output
	InputStream in;
	OutputStream out;


	//Constructor
	public MainBank(InputStream in, OutputStream out) {
		this.in=in;
		this.out=out;
	}

	//Main method.


	private boolean open=true;
	public boolean isOpen() {
		return open;
	}
	
	
	//The core of the program responsible for providing user experience.
	public void run() {

		Account acc;
		int option = 0;

		UIManager ui = new UIManager(this.in, this.out, menuOptions, MSG_PROMPT);
		try {

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("http://www.usman.cloud/banking/exchange-rate.csv"))
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
			String responseBody = response.body();

			StringReader stringReader = new StringReader(responseBody);
			Scanner scanner = new Scanner(stringReader);

			Map<String, Double> exchangeRates = new HashMap<>();

			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] parts = line.split(",");
				if (parts.length == 3) {
					String currencyCode = parts[0].trim();
					String currencyName = parts[1].trim();
					double exchangeRate = Double.parseDouble(parts[2].trim());
					exchangeRates.put(currencyCode, exchangeRate);
				}
			}

			Bank.readFromDisk();
			Bank.loadFinished();
			scanner.close();


			do {
				option = ui.getMainOption(); //Render main menu

				switch (option) {
					case 1:

						// Ask for customer information and account overdraft limit
						String firstName = ui.readToken(MSG_FIRST_NAME);
						String lastName = ui.readToken(MSG_LAST_NAME);
						String ssn = ui.readToken(MSG_SSN);
						double overdraftLimit = ui.readDouble(MSG_ACCOUNT_OD_LIMIT);

						// Ask for account currency if exchange rate file has been loaded
						String accountCurrency = "USD";

						if (exchangeRateLoaded) {
							do {
								accountCurrency = ui.readToken(MSG_ACCOUNT_CURRENCY);
								if (!exchangeRates.containsKey(accountCurrency)) {
									System.out.println("Currency not available, please enter a different currency.");
								}
							} while (!exchangeRates.containsKey(accountCurrency));
						}

						// Open the account and print the result including the account number
						Account a = Bank.openCheckingAccount(firstName, lastName, ssn, overdraftLimit,accountCurrency);
						ui.print(MSG_ACCOUNT_OPENED, new Object[]{a.getAccountNumber()});
						break;

						case 2:

						//Compact statement to accept user input, open account
						// print the result including the account number
						// Ask for customer information and account overdraft limit

							String FirstName = ui.readToken(MSG_FIRST_NAME);
							String LastName = ui.readToken(MSG_LAST_NAME);
							String Ssn = ui.readToken(MSG_SSN);

							// Ask for account currency if exchange rate file has been loaded
							String AccountCurrency = "USD";

							if (exchangeRateLoaded) {
								do {
									AccountCurrency = ui.readToken(MSG_ACCOUNT_CURRENCY);
									if (!exchangeRates.containsKey(AccountCurrency)) {
										System.out.println("Currency not available, please enter a different currency.");
									}
								} while (!exchangeRates.containsKey(AccountCurrency));
							}

							// Open the account and print the result including the account number
							Account A = Bank.openSavingAccount(FirstName, LastName, Ssn,AccountCurrency);
							ui.print(MSG_ACCOUNT_OPENED, new Object[]{A.getAccountNumber()});
							break;

					case 3:

						//Get bank to print list of accounts to the output stream provided as method argument
						int Number = ui.readInt(MSG_ACCOUNT_NUMBER);
						Account Acc = Bank.getAccount(Number);

						if (Acc != null) {
							String Currency = Acc.getCurrency();
							double balance = Acc.getBalance();
							double usdBalance = Bank.getAccountBalanceInUSD(Acc, exchangeRates, Currency);

							System.out.println(Acc.getAccountNumber()+" ("+Acc.getAccountName()+") : "
									+ Acc.getAccountHolder() +" : "+Currency + " : "+ String.format("%.2f", balance)+" : "
									+ String.format("%.2f", usdBalance)+" : " +(open ? "Account Open" : "Account Closed"));

						} else {
							System.out.println("Invalid account number.");
						}
						break;

					case 4:
						//find account and get the account to print transactions to the output stream provided in method arguments
						try {
							Bank.printAccountTransactions(ui.readInt(MSG_ACCOUNT_NUMBER),this.out);
						} catch (NoSuchAccountException e1) {
							this.handleException(ui, e1);
						}
						break;

					case 5:
						//ask for the account number, display details of the account
						int accNumber = ui.readInt(MSG_ACCOUNT_NUMBER);
						Account AccN = Bank.getAccount(accNumber);

						if (AccN != null) {
							String currency = AccN.getCurrency();
							double balance = AccN.getBalance();
							double usdBalance = Bank.getAccountBalanceInUSD(AccN, exchangeRates, currency);

							System.out.println("Account Number: " + AccN.getAccountNumber());
							System.out.println("Name: " + AccN.getAccountHolder().getFirstName()+" "+AccN.getAccountHolder().getLastName());
							System.out.println("SSN: " + AccN.getAccountHolder().getSSN());
							System.out.println("Currency: " + currency);
							System.out.println("Currency Balance: " + currency + " " + balance);
							System.out.println("USD Balance: USD " + String.format("%.2f", usdBalance));
						} else {
							System.out.println("Invalid account number.");
						}
						break;

					case 6:
						//find account, deposit money and print result

						try {
							int accountNumber = ui.readInt(MSG_ACCOUNT_NUMBER);
							Bank.makeDeposit(accountNumber, ui.readDouble(MSG_AMOUNT));
							ui.print(MSG_ACCOUNT_ACTION, new Object[]{"Deposit", "successful", Bank.getBalance(accountNumber)});
						} catch (NoSuchAccountException | AccountClosedException e) {
							this.handleException(ui, e);

						}
						break;

					case 7:
						//find account, withdraw money and print result
						try {
							int accountNumber = ui.readInt(MSG_ACCOUNT_NUMBER);
							Bank.makeWithdrawal(accountNumber, ui.readDouble(MSG_AMOUNT));
							ui.print(MSG_ACCOUNT_ACTION, new Object[]{"Withdrawal", "successful", Bank.getBalance(accountNumber)});

						} catch (NoSuchAccountException | InsufficientBalanceException e) {
							this.handleException(ui, e);

						}
						break;

					case 8:
						//find account and close it

						try {
							int accountNumber = ui.readInt(MSG_ACCOUNT_NUMBER);
							Bank.closeAccount(accountNumber);
							ui.print(MSG_ACCOUNT_CLOSED,
									new Object[]{accountNumber, Bank.getBalance(accountNumber)});

						} catch (NoSuchAccountException e) {
							this.handleException(ui, e);

						}
						break;


/*					case 9:

						double amount = ui.readDouble(MSG_CURRENCY_AMOUNT);
						String sourceCurrencyCode = ui.readToken(MSG_SOURCE_CURRENCY_CODE);
						String targetCurrencyCode = ui.readToken(MSG_TARGET_CURRENCY_CODE);

						double convertedAmount = Bank.currencyConverter(amount, sourceCurrencyCode, targetCurrencyCode);

						if (convertedAmount != -1) {
							System.out.printf("%.2f %s = %.2f %s%n", amount, sourceCurrencyCode, convertedAmount, targetCurrencyCode);
						}

						break;*/

					case 9:
						// Convert currency

					try {
						String sellCurrency = ui.readToken(MSG_ENTER_FROM_CURRENCY);

						double sellAmount = ui.readDouble(MSG_ENTER_AMOUNT);
						String buyCurrency = ui.readToken(MSG_ENTER_TO_CURRENCY);

						// Check that one of the currencies is USD
						if (!sellCurrency.equals("USD") && !buyCurrency.equals("USD")) {
							System.out.println("Error: one of the currencies must be USD");
							return;
						}
						double exchangeRate = Bank.currencyConverter(sellAmount,sellCurrency, buyCurrency)/sellAmount;

						if (exchangeRate != -1) {
							double buyAmount = exchangeRate * sellAmount;
							System.out.printf("The exchange rate is %.2f and you will get %s %.2f.%n", exchangeRate, buyCurrency, buyAmount);
						} else {
							System.out.printf("Could not find exchange rate for %s to %s.%n", sellCurrency, buyCurrency);
						}

					} catch (IOException e) {
						System.out.println("Currency file could not be loaded, currency conversion service and foreign currency account are not available.");
					}


					case 10:
						// If option 11 is selected, the program will exit

						System.out.println("Exiting");
						Bank.saveDataToFile();
						break;
					default:
						System.out.println("Invalid choice. Please try again.");
						break;}



			} while (option != menuOptions.length);

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private  void handleException(UIManager ui, Exception e) throws IOException{
		ui.print(e.getMessage(), new Object[] { });
	}


}