package com.usman.csudh.bank.core;
public class SavingAccount extends Account{
	private static final long serialVersionUID = 1L;

	public SavingAccount(Customer customer, String currency) {
		super("Saving", customer);
		this.setCurrency(currency);
	}



	//Withdrawals only allowed against positive balances 
	public void withdraw(double amount) throws InsufficientBalanceException {
		if (getBalance() - amount < 0) {
			throw new InsufficientBalanceException("Not enough funds to cover withdrawal");

		}
		super.withdraw(amount);

	}

}
