package admin;
import java.io.*;
import java.sql.*;
import java.util.*;
import web.Flight;


/* Arhip Alin-Gabriel Tema2 SPRC
 * Admin.java - clasa administrator reprezinta aplicatia de administrare a
 * datelor din baza de date , are 2 functii : 
 * addFlight() = adauga zborul in baza de date
 * cancelFlight() = sterge zborul din baza de date */
public class Admin {
	
	private static Connection connection=null;		// conexiunea admin-ului cu baza de date
	
	
	public static void cancelFlight(StringTokenizer st) throws SQLException {
		String flightId = "";
		if (st.hasMoreTokens())
			flightId = st.nextToken();
		else 
			System.out.println("Incorrect command arguments!\nTry like this: anulare_Zbor flightId");
		Statement statement = connection.createStatement();	/* creeaza si executa operatia */
		statement.executeUpdate("UPDATE Flight set state = " + Flight.CANCELED +
								" WHERE flight_id_official = " + flightId);
		statement.close();
		System.out.println("Flight " + flightId + " has been canceled.");
	}
	
	public static void addFlight(StringTokenizer tok) throws SQLException {
		String source = tok.nextToken();
		String dest = tok.nextToken();						/* obtine toate informatiile necesare */
		int departureDay = Integer.parseInt(tok.nextToken());
		int departureHour = Integer.parseInt(tok.nextToken());		
		int duration = Integer.parseInt(tok.nextToken());
		int numberOfSeats = Integer.parseInt(tok.nextToken());
		String flightId = tok.nextToken();
		Statement statement = connection.createStatement();	/* creeaza si executa operatia */
		statement.executeUpdate("INSERT INTO Flight (flight_id_official, source, destination, hour, "
				+ "day, duration, state, total_seats, booked_seats ) value (\"" + flightId + "\", \"" 
				+ source + "\", \"" + dest + "\", " + departureHour + ", " + departureDay+ ", " 
				+ duration + ", " + Flight.AVAILABLE + ", " + numberOfSeats + ", 0)");
		statement.close();
		System.out.println("Flight added successfuly");
	}

	public static void main(String [] args) throws Exception {
		Admin admin = new Admin(); 					
		Properties properties = new Properties();	/* folosind credentialele din fisierul de proprietati */
		InputStream in = admin.getClass().getClassLoader().getResourceAsStream("admin_login.properties");
        properties.load(in);			  /* incarca fisierul de proprietati ale admin-ului */
	    Class.forName(properties.getProperty("DRIVER_CLASS_NAME")).newInstance();
	    connection = DriverManager.getConnection(properties.getProperty("DB_CONN_STRING"),
	    		  properties.getProperty("USER_NAME"), properties.getProperty("PASSWORD"));
	    System.out.println("Coonected to airservice DB"); /* Creeaza conexiunea cu BD folosind JDBC via DriveManager */
		if (connection == null) {
			System.out.println("[ADMIN] Cannot connect to DB using credentials file");
			System.exit(0);
		}
		Scanner sc = new Scanner(System.in);
		while(true) {								/* asteapta si prelucreaza inputul primit de la administrator */
			System.out.flush();
			String command = sc.nextLine();
			StringTokenizer st = new StringTokenizer(command, ", ");
			String comm = st.nextToken();
			if (command.equals("exit") || command.equals("quit")) 		// parseaza inputul primit
				break;
			else if (command.length() == 0)
				continue;
			else if (comm.equals("adaugare_Zbor")) 
				addFlight(st);
			else if (comm.equals("anulare_Zbor")) 
				cancelFlight(st);
			else 
				System.out.println("Unknown command. Try one of these two:\n"
						+ "adaugare_Zbor source, destination, departureDay, departureHour, "
						+ "duration, numberOfSeats, flightId\nanulare_Zbor flightId");
		}
		sc.close();
		connection.close();					// inchide conexiunea
	}
}