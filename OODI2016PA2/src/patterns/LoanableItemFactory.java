package patterns;
import items.Book;
import items.Camera;
import items.Cd;
import items.Dvd;
import items.Laptop;
import items.LoanableItem;
import items.MusicCd;
import items.Periodical;

public class LoanableItemFactory {

	public static LoanableItem getLoanableItem(String name, String title, String author, String id) {
		if(name == null){
			return null;
		}	
		if(name.equalsIgnoreCase("costly book")){
			return new CostlyBookDecorator(new NewBookDecorator(new Book(title, author, id)));
		
		} else if(name.equalsIgnoreCase("book")){
			return new NewBookDecorator(new Book(title, author, id));

		} else if(name.equalsIgnoreCase("camera")){
			return new Camera(title, author, id);

		} else if(name.equalsIgnoreCase("laptop")){
			return new Laptop(title, author, id);
			
		} else if(name.equalsIgnoreCase("cd")){
			return new Cd(title, author, id);
			
		} else if(name.equalsIgnoreCase("musiccd")){
			return new MusicCd(title, author, id);
			
		} else if(name.equalsIgnoreCase("dvd")){
			return new Dvd(title, author, id);
			
		} else if(name.equalsIgnoreCase("periodical")){
			return new Periodical(title, author, id);
		}

		return null;
	}
}