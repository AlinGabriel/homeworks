package items;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import library.Hold;
import library.Member;
import patterns.LoanableItemVisitor;

public abstract class LoanableItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private String title;
	private String author;
	private String id;
	private Member borrowedBy;
	private List<Hold> holds = new LinkedList<Hold>();
	private Calendar dueDate;
	private Calendar addDate;

	/**
	 * Creates an item with the given id, title, and author name
	 * @param title item title
	 * @param author author name
	 * @param id item id
	 */
	public LoanableItem(String title, String author, String id) {
		this.title = title;
		this.author = author;
		this.id = id;
		setAddDate();
	}

	/**
	 * Marks the book as issued to a member
	 * @param member the borrower
	 * @return true iff the book could be issued. True currently
	 */
	public boolean issue(Member member) {
		borrowedBy = member;
		dueDate = new GregorianCalendar();
		dueDate.setTimeInMillis(System.currentTimeMillis());
		dueDate.add(Calendar.MONTH, 1);
		return true;
	}

	/**
	 * Marks the item as returned
	 * @return The member who had borrowed the item
	 */
	public Member returnItem() {
		if (borrowedBy == null) {
			return null;
		} else {
			Member borrower = borrowedBy;
			borrowedBy = null;
			return borrower;
		}
	}

	/**
	 * Renews the book 
	 * @param member who wants to renew the book
	 * @return true iff the book could be renewed
	 */
	public boolean renew(Member member) {
		if (hasHold()) {
			return false;
		}
		if ((member.getId()).equals(borrowedBy.getId())) {
			return (issue(member));
		}
		return false;
	}

	/**
	 * Adds one more hold to the book
	 * @param hold the new hold on the book
	 */
	public void placeHold(Hold hold) {
		holds.add(hold);
	}

	/**
	 * Removes hold for a specific member
	 * @param memberId whose hold has to be removed
	 * @return true iff the hold could be removed
	 */
	public boolean removeHold(String memberId) {
		for (ListIterator<Hold> iterator = holds.listIterator(); iterator.hasNext(); ) {
			Hold hold = (Hold) iterator.next();
			String id = hold.getMember().getId();
			if (id.equals(memberId)) {
				iterator.remove();
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a valid hold
	 * @return the next valid hold
	 */
	public Hold getNextHold() {
		for (ListIterator<Hold> iterator = holds.listIterator(); iterator.hasNext(); ) {
			Hold hold = (Hold) iterator.next();
			iterator.remove();
			if (hold.isValid()) {
				return hold;
			}
		}
		return null;
	}

	/**
	 * Checks whether there is a hold on this book
	 * @return true iff there is a hold
	 */
	public boolean hasHold() {
		ListIterator<Hold> iterator = holds.listIterator();
		if (iterator.hasNext()) {
			return true;
		}
		return false;
	}

	/**
	 * Returns an iterator for the holds
	 * @return iterator for the holds on the book
	 */
	public Iterator<Hold> getHolds() {
		return holds.iterator();
	}

	/**
	 * Getter for author
	 * @return author name
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * getter for title
	 * @return title of the book
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Getter for id
	 * @return id of the book
	 */
	public String getId() {
		return id;
	}

	/**
	 * Getter for borrower
	 * @return the member who borrowed the book
	 */
	public Member getBorrower() {
		return borrowedBy;
	}

	/**
	 * Setter for borrower
	 * @return the member who borrowed the book
	 */
	public void setBorrower(Member member) {
		borrowedBy = member;
	}

	/**
	 * Getter for due date
	 * @return the date on which the book is due
	 */
	public Calendar getDueDate() {
		return dueDate;
	}

	public void setDueDate(Calendar dueDate) {
		this.dueDate = dueDate;
	}

	public Calendar getAddDate() {
		return addDate;
	}

	public void setAddDate() {
		addDate = new GregorianCalendar();
		addDate.setTimeInMillis(System.currentTimeMillis());
	}

	/** 
	 * String form of the item
	 * 
	 */
	public String toString() {
		return "title " + title + " author " + author + " id " + id + " borrowed by " + borrowedBy;
	}

	public abstract void accept(LoanableItemVisitor loanableItemVisitor);
}