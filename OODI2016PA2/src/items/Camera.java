package items;

import patterns.LoanableItemVisitor;

public class Camera extends LoanableItem {
	private static final long serialVersionUID = 1L;

	public Camera(String title, String author, String id) {
		super(title, author, id);
	}

	public void accept(LoanableItemVisitor loanableItemVisitor) {
		loanableItemVisitor.visit(this);		
	}

}
