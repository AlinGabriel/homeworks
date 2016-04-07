package patterns;
import items.Book;
import items.Camera;
import items.Cd;
import items.Dvd;
import items.Laptop;
import items.MusicCd;
import items.Periodical;

public class ComputeLoanableItemVisitor implements LoanableItemVisitor {
	private int books;
	private int cds;
	private int musicCds;
	private int dvds;
	private int laptops;
	private int cameras;
	private int periodicals;

	@Override
	public void visit(Book book) {
		if (book.getBorrower() != null)
			books++;
	}

	@Override
	public void visit(Camera camera) {
		if (camera.getBorrower() != null)
			cameras++;		
	}

	@Override
	public void visit(Cd cd) {
		if (cd.getBorrower() != null)
			cds++;		
	}

	@Override
	public void visit(Dvd dvd) {
		if (dvd.getBorrower() != null)
			dvds++;		
	}

	@Override
	public void visit(Laptop laptop) {
		if (laptop.getBorrower() != null)
			laptops++;		
	}

	@Override
	public void visit(MusicCd musicCd) {
		if (musicCd.getBorrower() != null)
			musicCds++;		
	}
	
	@Override
	public void visit(Periodical periodical) {
		if (periodical.getBorrower() != null)
			periodicals++;		
	}

	public String toString(){
		return "Books: " + books + ", CDs: " + cds + ", MusicCds: " + musicCds + ", DVDs: " 
				+ dvds + ", Laptops: " + laptops + ", Cameras: " + cameras + ", Periodicals: " + periodicals;
	}

}
