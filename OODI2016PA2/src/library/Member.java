package library;
/**
 * 
 * @author Brahma Dathan and Sarnath Ramnath
 * @Copyright (c) 2010

 * Redistribution and use with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - the use is for academic purpose only
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Neither the name of Brahma Dathan or Sarnath Ramnath
 *     may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS"AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  
 */
import java.io.Serializable;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import items.Book;
import items.LoanableItem;
import patterns.NewBookDecorator;
public class Member implements Serializable {
	private static final long serialVersionUID = 1L;
	private String name;
	private String address;
	private String phone;
	private String id;
	private static final String MEMBER_STRING = "M";
	private List<LoanableItem> itemsBorrowed = new LinkedList<LoanableItem>();
	private List<Hold> itemsOnHold = new LinkedList<Hold>();
	private List<Transaction> transactions = new LinkedList<Transaction>();
	private double fine = 0.0;

	public double getFine() {
		return fine;
	}
	public void setFine(double fine) {
		this.fine = fine;
	}
	/**
	 * Represents a single member
	 * @param name name of the member
	 * @param address address of the member
	 * @param phone phone number of the member
	 */
	public  Member (String name, String address, String phone) {
		this.name = name;
		this.address = address;
		this.phone = phone;
		id = MEMBER_STRING + (MemberIdServer.instance()).getId();
	}
	/**
	 * Stores the item as issued to the member
	 * @param item the item to be issued
	 * @return true iff the item could be marked as issued. always true currently 
	 */
	public boolean issue(LoanableItem item) {
		if (itemsBorrowed.add(item)) {
			transactions.add(new Transaction ("Item issued ", item.getTitle()));
			return true;
		}
		return false;
	}

	public double calculateFine(Book book) {
		long daysOverdue = book.getDaysOverdue();
		double dailyFine = 0, fine = 0;

		daysOverdue--;
		if (book instanceof NewBookDecorator && ((NewBookDecorator) book).isBookNew()) {
			fine += 0.25;
			dailyFine = 0.10;
		}
		else {
			fine += 0.15;
			dailyFine = 0.05;
		}
		while(daysOverdue > 0) {
			fine += dailyFine;
			daysOverdue--;
		}

		if (book.hasHold())
			fine *= 2;
		return fine;
	}

	/**
	 * Marks the item as not issued to the member
	 * @param item the item to be returned
	 * @return true iff the item could be marked as marked as returned 
	 */
	public boolean returnItem(LoanableItem item) {
		if (itemsBorrowed.remove(item)){
			transactions.add(new Transaction ("Item returned ", item.getTitle()));
			if (item instanceof Book) {
				Book book = (Book) item;
				if (book.isOverdue())
					fine += calculateFine(book);
				if (fine > 0.0)
					System.out.println("This member has a fine of " + fine + " lei");
			}
			return true;
		}
		return false;
	}
	/**
	 * Marks the item as renewed
	 * @param item the item to be renewed
	 * @return true iff the item could be renewed
	 */
	public boolean renew(LoanableItem item) {
		for (ListIterator<LoanableItem> iterator = itemsBorrowed.listIterator(); iterator.hasNext(); ) {
			LoanableItem anItem = (LoanableItem) iterator.next();
			String id = anItem.getId();
			if (id.equals(item.getId())) {
				transactions.add(new Transaction ("Item renewed ",  item.getTitle()));
				return true;
			}
		}
		return false;
	}
	/**
	 * Gets an iterator to the issued items
	 * @return Iterator to the collection of issued items
	 */
	public Iterator<LoanableItem> getItemsIssued() {
		return (itemsBorrowed.listIterator());
	}
	/**
	 * Places a hold for the item
	 * @param hold the item to be placed a hold
	 */
	public void placeHold(Hold hold) {
		transactions.add(new Transaction ("Hold Placed ", hold.getItem().getTitle()));
		itemsOnHold.add(hold);
	}
	/**
	 * Removes a hold
	 * @param itemId the item id for removing a hold
	 * @return true iff the hold could be removed
	 */
	public boolean removeHold(String itemId) {
		for (ListIterator<Hold> iterator = itemsOnHold.listIterator(); iterator.hasNext(); ) {
			Hold hold = (Hold) iterator.next();
			String id = hold.getItem().getId();
			if (id.equals(itemId)) {
				transactions.add(new Transaction ("Hold Removed ", hold.getItem().getTitle()));
				iterator.remove();
				return true;
			}
		}
		return false;
	}
	/**
	 * Gets an iterator to a collection of selected transactions
	 * @param date the date for which the transactions have to be retrieved
	 * @return the iterator to the collection
	 */
	public Iterator<Transaction> getTransactions(Calendar date) {
		List<Transaction> result = new LinkedList<Transaction>();
		for (Iterator<Transaction> iterator = transactions.iterator(); iterator.hasNext(); ) {
			Transaction transaction = (Transaction) iterator.next();
			if (transaction.onDate(date)) {
				result.add(transaction);
			}
		}
		return (result.iterator());
	}
	/**
	 * Getter for name
	 * @return member name
	 */
	public String getName() {
		return name;
	}
	/**
	 * Getter for phone number
	 * @return phone number
	 */
	public String getPhone() {
		return phone;
	}
	/**
	 * Getter for address
	 * @return member address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * Getter for id
	 * @return member id
	 */
	public String getId() {
		return id;
	}
	/**
	 * Setter for name
	 * @param newName member's new name
	 */
	public void setName(String newName) {
		name = newName;
	}
	/**
	 * Setter for address
	 * @param newName member's new address
	 */
	public void setAddress(String newAddress) {
		address = newAddress;
	}
	/**
	 * Setter for phone
	 * @param newName member's new phone
	 */
	public void setPhone(String newPhone) {
		phone = newPhone;
	}
	/**
	 * Checks whether the member is equal to the one with the given id
	 * @param id of the member who should be compared
	 * @return true iff the member ids match
	 */
	public boolean equals(String id) {
		return this.id.equals(id);
	}
	/** 
	 * String form of the member
	 * 
	 */
	@Override
	public String toString() {
		String string = "Member name " + name + " address " + address + " id " + id + "phone " + phone;
		string += " borrowed: [";
		for (Iterator<LoanableItem> iterator = itemsBorrowed.iterator(); iterator.hasNext(); ) {
			LoanableItem item = (LoanableItem) iterator.next();
			string += " " + item.getTitle();
		}
		string += "] holds: [";
		for (Iterator<Hold> iterator = itemsOnHold.iterator(); iterator.hasNext(); ) {
			Hold hold = (Hold) iterator.next();
			string += " " + hold.getItem().getTitle();
		}
		string += "] transactions: [";
		for (Iterator<Transaction> iterator = transactions.iterator(); iterator.hasNext(); ) {
			string += (Transaction) iterator.next();
		}
		string += "]";
		return string;
	}
}