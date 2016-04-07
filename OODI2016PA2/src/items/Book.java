package items;
import java.util.Calendar;
import java.util.GregorianCalendar;

import patterns.LoanableItemVisitor;

/**
 * Represents a single book
 * @author Brahma Dathan and Sarnath Ramnath
 *
 */
public class Book extends LoanableItem {
	private static final long serialVersionUID = 1L;

	public Book(String title, String author, String id) {
		super(title, author, id);
	}

	/* Checks if a book is overdue */
	public boolean isOverdue() {
		Calendar currDate = new GregorianCalendar();
		currDate.setTimeInMillis(System.currentTimeMillis());

		if (getDueDate().before(currDate))
			return true;

		return false;
	}
	
	/* Returns the number of days overdue when a book is returned */
	public long getDaysOverdue() {
		Calendar currDate = new GregorianCalendar();
		currDate.setTimeInMillis(System.currentTimeMillis());
		Calendar date = (Calendar) getDueDate().clone();
		long daysOverdue = 0;

		while (date.before(currDate)) {
			date.add(Calendar.DAY_OF_MONTH, 1);
			daysOverdue++;
		}

		return daysOverdue;
	}
	
	public void accept(LoanableItemVisitor loanableItemVisitor) {
		loanableItemVisitor.visit(this);
	}
}