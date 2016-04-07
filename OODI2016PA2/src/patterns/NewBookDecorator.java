package patterns;
import java.util.Calendar;
import java.util.GregorianCalendar;

import items.Book;
import library.Member;

public class NewBookDecorator extends BookDecorator {
	private static final long serialVersionUID = 1L;
	
	public NewBookDecorator(Book decoradtedBook) {
		super(decoradtedBook);
	}

	/* New Books cannot be renewed */
	@Override
	public boolean renew(Member member) {
		if (isBookNew())
			return false;
		
		return super.renew(member);
	}
	
	/* Check if a book is still new or not.
	 * A book is new if it has less than 90 days since it was added */
	public boolean isBookNew() {
		Calendar currDate = new GregorianCalendar();
		currDate.setTimeInMillis(System.currentTimeMillis());
		Calendar date = (Calendar) getAddDate().clone();
		long daysSinceBookAdded = 0;

		while (date.before(currDate)) {
			date.add(Calendar.DAY_OF_MONTH, 1);
			daysSinceBookAdded++;
		}
		if (daysSinceBookAdded > 90)
			return false;

		return true;
	}
	
	public void accept(LoanableItemVisitor loanableItemVisitor) {
		loanableItemVisitor.visit(this);
	}
	
}