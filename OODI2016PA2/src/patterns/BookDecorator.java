package patterns;
import items.Book;

public abstract class BookDecorator extends Book {
	private static final long serialVersionUID = 1L;
	protected Book decoratedBook;

	public BookDecorator(Book decoratedBook) {
		super(decoratedBook.getTitle(), decoratedBook.getAuthor(), decoratedBook.getId());
		this.decoratedBook = decoratedBook;
	}
}