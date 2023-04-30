package com.usman.csudh.bank.core;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.usman.csudh.util.UniqueCounter;

public class Account implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String accountName;
	private Customer accountHolder;
	private ArrayList<Transaction> transactions;
	private String currency;
	private double balance;
	private String ssn;

	public Account(String currency) {
		this.currency = currency;
	}

	public Account(double balance) {
		this.balance = balance;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	private boolean open=true;
	private int accountNumber;
	private static Map<Integer, Account> accounts = new HashMap<>();
	private static Map<String, Double> exchangeRates = new HashMap<>();
	private static boolean exchangeRatesLoaded = false;

	protected Account(String name, Customer customer) {
		accountName=name;
		accountHolder=customer;
		transactions=new ArrayList<Transaction>();
		accountNumber=UniqueCounter.nextValue();
		ssn=customer.getSSN();
	}
	
	public String getAccountName() {
		return accountName;
	}
	public String getSSN() {
		return ssn;
	}
	public Customer getAccountHolder() {
		return accountHolder;
	}

	public double getBalance() {
		double workingBalance=0;
		
		for(Transaction t: transactions) {
			if(t.getType()==Transaction.CREDIT)workingBalance+=t.getAmount();
			else workingBalance-=t.getAmount();
		}
		
		return workingBalance;
	}

	public void deposit(double amount)  throws AccountClosedException{
		double balance=getBalance();
		if(!isOpen()&&balance>=0) {
			throw new AccountClosedException("\nAccount is closed with positive balance, deposit not allowed!\n\n");
		}
		transactions.add(new Transaction(Transaction.CREDIT,amount));
	}

	public void withdraw(double amount) throws InsufficientBalanceException {
			
		double balance=getBalance();
			
		if(!isOpen()&&balance<=0) {
			throw new InsufficientBalanceException("\nThe account is closed and balance is: "+balance+"\n\n");
		}
		
		transactions.add(new Transaction(Transaction.DEBIT,amount));
	}
	
	public void close() {
		open=false;
	}
	
	public boolean isOpen() {
		return open;
	}
	
	public int getAccountNumber() {
		return accountNumber;
	}

	public String toString() {
		String aName=accountNumber+"("+accountName+")"+" : "+accountHolder.toString()+"" +" : "+getBalance()+" : "+(open?"Account Open":"Account Closed");
		return aName;
	}
	 
	public void printTransactions(OutputStream out) throws IOException {
		
		out.write("\n\n".getBytes());
		out.write("------------------\n".getBytes());
	
		for(Transaction t: transactions) {
			out.write(t.toString().getBytes());
			out.write((byte)10);
		}
		out.write("------------------\n".getBytes());
		out.write(("Balance: "+getBalance()+"\n\n\n").getBytes());
		out.flush();
		
	}
	public String getCurrency() {
		return currency;
	}
}


