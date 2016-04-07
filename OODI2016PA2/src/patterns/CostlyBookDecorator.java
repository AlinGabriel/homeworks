package patterns;
import java.util.Calendar;
import java.util.GregorianCalendar;

import items.Book;
import library.Member;

public class CostlyBookDecorator extends BookDecorator {
	private static final long serialVersionUID = 1L;

	public CostlyBookDecorator(Book decoradtedBook) {
		super(decoradtedBook);
	}

	/* Costly Books cannot be issued to a member who has unpaid fines */
	@Override
	public boolean issue(Member member) {
		if (isBookCostly() && member.getFine() > 0.0)
			return false;

		return super.issue(member);
	}
	
	/* Checks if the book is still costly.
	 * A book is costly if it has less than a year since it was added */
	public boolean isBookCostly() {
		Calendar currDate = new GregorianCalendar();
		currDate.setTimeInMillis(System.currentTimeMillis());
		Calendar date = (Calendar) getAddDate().clone();
		long daysSinceBookAdded = 0;

		while (date.before(currDate)) {
			date.add(Calendar.DAY_OF_MONTH, 1);
			daysSinceBookAdded++;
		}
		if (daysSinceBookAdded > 365)
			return false;

		return true;
	}
	
	/* Marks a former costly book as being costly again by setting
	 * the date the book was added as being the current day */
	public void makeBookCostlyAgain() {
		setAddDate();
	}

	public void accept(LoanableItemVisitor loanableItemVisitor) {
		loanableItemVisitor.visit(this);
	}

}