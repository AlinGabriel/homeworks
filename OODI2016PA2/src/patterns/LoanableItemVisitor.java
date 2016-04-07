package patterns;
import items.Book;
import items.Camera;
import items.Cd;
import items.Dvd;
import items.Laptop;
import items.MusicCd;
import items.Periodical;

public interface LoanableItemVisitor {
	
	void visit(Book book);
	void visit(Camera camera);
	void visit(Cd cd);
	void visit(Dvd dvd);
	void visit(Laptop laptop);
	void visit(MusicCd musicCd);
	void visit(Periodical periodical);
	
}