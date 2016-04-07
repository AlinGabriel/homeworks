package items;

import patterns.LoanableItemVisitor;

public class Cd extends LoanableItem {
	private static final long serialVersionUID = 1L;
	
	public Cd(String title, String author, String id) {
		super(title, author, id);
	}

	public void accept(LoanableItemVisitor loanableItemVisitor) {
		loanableItemVisitor.visit(this);
	}

}
