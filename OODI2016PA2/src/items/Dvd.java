package items;

import patterns.LoanableItemVisitor;

public class Dvd extends LoanableItem {
	private static final long serialVersionUID = 1L;

	public Dvd(String title, String author, String id) {
		super(title, author, id);
	}

	public void accept(LoanableItemVisitor loanableItemVisitor) {
		loanableItemVisitor.visit(this);
	}

}
