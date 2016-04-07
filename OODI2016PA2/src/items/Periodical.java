package items;
import java.util.Calendar;
import java.util.GregorianCalendar;

import library.Member;
import patterns.LoanableItemVisitor;

public class Periodical extends LoanableItem {
	private static final long serialVersionUID = 1L;

	public Periodical(String title, String author, String id) {
		super(title, author, id);
	}

	/* New Periodicals cannot be checked out */
	@Override
	public boolean issue(Member member) {
		if (isPeriodicalNew())
			return false;

		return super.issue(member);
	}

	/* Checks if a periodical is new.
	 * A periodical is new it it has less than 90 days since it was added */
	public boolean isPeriodicalNew() {
		Calendar currDate = new GregorianCalendar();
		currDate.setTimeInMillis(System.currentTimeMillis());
		Calendar date = (Calendar) getAddDate().clone();
		long daysOverdue = 0;

		while (date.before(currDate)) {
			date.add(Calendar.DAY_OF_MONTH, 1);
			daysOverdue++;
		}

		if (daysOverdue > 90)
			return false;

		return true;
	}

	public void accept(LoanableItemVisitor loanableItemVisitor) {
		loanableItemVisitor.visit(this);
	}

}