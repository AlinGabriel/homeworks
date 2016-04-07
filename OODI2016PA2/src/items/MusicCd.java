package items;

import patterns.LoanableItemVisitor;

public class MusicCd extends LoanableItem {
	private static final long serialVersionUID = 1L;

	public MusicCd(String title, String author, String id) {
		super(title, author, id);
	}

	public void accept(LoanableItemVisitor loanableItemVisitor) {
		loanableItemVisitor.visit(this);
	}
}
