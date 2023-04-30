package com.usman.csudh.bank.core;


import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class Bank {
	private static final String DATA_FILE = "bank_data.ser";
	private static Map<Integer, Account> accounts = new TreeMap<Integer, Account>();

	private static boolean exchangeRateLoaded;
	private Config config;


	public static void readFromDisk() throws IOException, ClassNotFoundException {
		FileInputStream fileInputStream=new FileInputStream(new File("bank_data.ser"));
		ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream);
		Map<Integer, Account> a1=(Map<Integer, Account>)objectInputStream.readObject();
		accounts=a1;
	}

	public static Account getAccount(int accountNumber) {
		return accounts.get(accountNumber);
	}


	public static void loadFinished() {
		exchangeRateLoaded = true;
	}

	private static Map<String, Double> loadCSVFile() throws IOException, InterruptedException {
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

		scanner.close();
		exchangeRateLoaded = true;
		return exchangeRates;
	}


	public static Account openCheckingAccount(String firstName, String lastName, String ssn, double overdraftLimit,String currency) {
		Customer c = new Customer(firstName, lastName, ssn);
		Account a = new CheckingAccount(c, overdraftLimit,currency);
		accounts.put(a.getAccountNumber(), a);
		return a;

	}

	public static Account openSavingAccount(String firstName, String lastName, String ssn,String currency) {
		Customer c = new Customer(firstName, lastName, ssn);
		Account a = new SavingAccount(c,currency);
		accounts.put(a.getAccountNumber(), a);
		return a;

	}

	public static Account lookup(int accountNumber) throws NoSuchAccountException {
		if (!accounts.containsKey(accountNumber)) {
			throw new NoSuchAccountException("\nAccount number: " + accountNumber + " not found!\n\n");
		}

		return accounts.get(accountNumber);
	}

	public static void makeDeposit(int accountNumber, double amount) throws AccountClosedException, NoSuchAccountException {

		lookup(accountNumber).deposit(amount);

	}

	public static void makeWithdrawal(int accountNumber, double amount) throws InsufficientBalanceException, NoSuchAccountException {
		lookup(accountNumber).withdraw(amount);
	}

	public static void closeAccount(int accountNumber) throws NoSuchAccountException {
		lookup(accountNumber).close();
	}

	public static double getBalance(int accountNumber) throws NoSuchAccountException {
		return lookup(accountNumber).getBalance();
	}


	public static double getCurrencyExchangeRate(Map<String, Double> exchangeRates, String sourceCurrencyCode, String targetCurrencyCode) {
		if (exchangeRates.containsKey(sourceCurrencyCode) && exchangeRates.containsKey(targetCurrencyCode)) {
			double sourceRate = exchangeRates.get(sourceCurrencyCode);
			double targetRate = exchangeRates.get(targetCurrencyCode);
			return targetRate / sourceRate;
		} else {
			System.out.println("Invalid currency code(s).");
			return -1;
		}
	}

	public static double getAccountBalanceInUSD(Account account, Map<String, Double> exchangeRates, String accountCurrency) {
		double balance = account.getBalance();

		if (!accountCurrency.equals("USD")) {
			double exchangeRate = getCurrencyExchangeRate(exchangeRates, accountCurrency, "USD");
			System.out.println(exchangeRate);
			balance /= exchangeRate;
		}
		return balance;
	}

	public static void printAccountTransactions(int accountNumber, OutputStream out) throws IOException, NoSuchAccountException {

		lookup(accountNumber).printTransactions(out);
	}


	//case 8
	public static double currencyConverter(double amount, String sourceCurrencyCode, String targetCurrencyCode) throws IOException, InterruptedException {

		// Check if exchange rate file has been loaded
		if (!exchangeRateLoaded) {
			System.out.println("Exchange rate file not loaded.");
		} else {

			// Read the currency exchange file and store its contents
			Map<String, Double> exchangeRates = loadCSVFile();


			// Convert the amount from source currency to USD
			double usdAmount;
			if (sourceCurrencyCode.equals("USD")) {
				usdAmount = amount;
			} else {
				double exchangeRate = exchangeRates.get(sourceCurrencyCode);
				usdAmount = amount * exchangeRate;
			}

			// Convert the USD amount to target currency
			double convertedAmount;
			if (targetCurrencyCode.equals("USD")) {
				convertedAmount = usdAmount;
			} else {
				double exchangeRate = exchangeRates.get(targetCurrencyCode);
				convertedAmount = usdAmount / exchangeRate;
			}

			return convertedAmount;
		}

		return amount;
	}

	//save all data to a file
	public static void saveDataToFile() {
		try {
			FileOutputStream fileOut = new FileOutputStream(DATA_FILE);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);

			// write the entire Bank object to the file
			out.writeObject(accounts);
			out.close();
			fileOut.close();
			System.out.println("Bank data saved to file.");
		} catch (IOException e) {
			System.err.println("Error saving bank data to file: " + e.getMessage());
		}

	}


	// Method to load data from a file
	public static Bank loadDataFromFile() {
		Bank bank = null;
		try {
			FileInputStream fileIn = new FileInputStream(DATA_FILE);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			bank = (Bank) in.readObject();
			in.close();
			fileIn.close();
			System.out.println("Bank data loaded from file.");
		} catch (IOException e) {
			System.err.println("Error loading bank data from file: " + e.getMessage());
		} catch (ClassNotFoundException e) {
			System.err.println("Error loading bank data from file: " + e.getMessage());
		}
		return bank;
	}

}