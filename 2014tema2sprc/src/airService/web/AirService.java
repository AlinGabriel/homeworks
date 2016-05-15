package web;
import java.sql.*;
import javax.sql.*;
import javax.naming.*;


/* Arhip Alin-Gabriel Tema 2 SPRC  
 * Implementarea Serviciului Web contine cele 3 operatii de baza care pot fi efectuate:
 *  	getOptimalRoute - returneaza ruta de durata minima cu maxim maxFlights zboruri.
 *  	bookTicket		- face o rezervare pentru o anumita ruta.
 *  	buyTicket 		- cumpara un bilet pentru o ruta pentru care s-a facut deja rezervare */
public class AirService {
	
	public Connection connection=null;
	public static final String JNDI_NAME = "java:comp/env/jdbc/airservice";

	
	/* buyTicket - cumpara bilet pentru ruta deja rezervata. Rezultatul este un boarding
	 pass continand toate informatiile despre zboruri, sau sirul vid in caz de eroare */
	public String buyTicket(String reservationId, String creditCardInfo) throws NamingException, SQLException {
		int ticketId = 0;				// se conecteaza la baza de date airservice
		connection = ((DataSource) (new InitialContext()).lookup(JNDI_NAME)).getConnection();	
		Statement st = connection.createStatement();	
		ResultSet res = st.executeQuery("SELECT max(id) from Ticket;");	
		while (res.next())
			ticketId = res.getInt(1);	// obtine ultimul id al biletului
		ticketId++;						// creaza un bilet nou
		st.executeUpdate("INSERT INTO Ticket (id, reservation_id, creditCardInfo) " + "value (" 
				+ ticketId + ", " + reservationId + ", " + creditCardInfo + ")");
		st.close();
		connection.close();				// inchide conexiunea la baza de date
		return "" + ticketId;		
	}
	
	
	/* getOptimalRoute - returneaza zborul cu durata cea mai mica si intoarce un tablou de siruri ce va contine:
	 * pe prima linie: un sir cu informatii despre fiecare zbor din ruta (id,sursa,dest,ora,durata,etc)
	 * pe urmatoarele pozitii: id-urile fiecarui zbor din ruta , in ordinea in sursa -> dest */
	public String[] getOptimalRoute(String source, String dest, int maxFlights, int departureDay) 
			throws NamingException, SQLException {
		String [] finalFlightIds = null;
		int minCost = 99999999;
		// se conecteaza la baza de date airservice
		connection = ((DataSource) (new InitialContext()).lookup(JNDI_NAME)).getConnection();		
		Statement st = connection.createStatement();
		/* extrage calea optima ce are maxim maxFlights zboruri  */
		for (int i = 1; i <= maxFlights; i++) {
			int maxFlights2 = i;
			String []flights = new String[maxFlights2];
			String sql = "SELECT ";
			for (int j = 0; j < maxFlights2; j++) {
				flights[j] = "f" + j;
				sql += flights[j] + ".flight_id_official, ";
			}
			sql += "if(" + flights[maxFlights2 - 1] + ".day = " + flights[0] + ".day, " + 
					flights[maxFlights2 - 1] + ".hour - " + flights[0] + ".hour, (" + 
					flights[maxFlights2 - 1] + ".day - " + flights[0] + ".day - 1) * 24 + " + 
					flights[maxFlights2 - 1] + ".hour + (24 - " + flights[0] + ".hour)) + "  +
					flights[maxFlights2 - 1] + ".duration cost from Flight " + flights[0] + " ";
			for (int j = 1; j < maxFlights2; j++) {
				sql += " join Flight " + flights[j] + " on " + flights[j - 1] + ".destination = " 
						+ flights[j] + ".source and ((" + flights[j - 1] + ".day < " + flights[j] +
						".day) or (" +flights[j-1]+ ".day = " +flights[j]+ ".day and " +flights[j-1] 
							+ ".hour + " + flights[j - 1] + ".duration < " + flights[j] + ".hour))";
			}
			sql += " where f" + 0 + ".source = \"" + source + "\" and f" + (maxFlights2 - 1) + 
					".destination = \"" + dest + "\" order by cost ASC limit 1; \n";
			ResultSet rs = st.executeQuery(sql);
			String [] fligthIds = new String[i + 1];
			while (rs.next()) {
				int cost = rs.getInt(i + 1);
				if (cost < minCost) {
					for (int j = 1; j <= i; j++)
						fligthIds[j] = rs.getString(j);
					minCost = cost;
					finalFlightIds = fligthIds;
				}
			}
		}
		connection.close();				// inchide conexiunea la baza de date
		if (finalFlightIds != null) {	// daca a gasit ruta , obtine detalii
			String details = "";		// extrage toate detaliile fiecarui zbor din ruta 
			connection = ((DataSource) (new InitialContext()).lookup(JNDI_NAME)).getConnection();	
			Statement st2 = connection.createStatement();
			for (int i = 1; i < finalFlightIds.length; i++) {
				ResultSet res = st2.executeQuery("SELECT source, destination, day, hour, duration, total_seats, "
						+ "booked_seats from Flight where flight_id_official = " + finalFlightIds[i]);
				while (res.next())
					details += "Flight " + finalFlightIds[i] + " (" + res.getString(1) + " - " + 
							res.getString(2) +", day " + res.getInt(3) + ", hour " + res.getInt(4) 
							+ ", duration " + res.getInt(5) + ", total_seats " + res.getInt(6) + 
							", booked_seats " + res.getInt(7) + ") ";
			}
			connection.close();			// inchide conexiunea la baza de date
			finalFlightIds[0] = details;
			return finalFlightIds;
		} 
		else 
			return new String[] {"No route found"};
	}
	
	
	/* bookTicket - rezerva o anumita ruta. Metoda intoarce ID-ul rezervarii, sau vid daca nu se poate
	 * face rezervarea - nu mai sunt locuri disponibile, unul din zboruri a fost anulat etc. */ 
	public String bookTicket(String[] flightIds) throws NamingException, SQLException {
		if (flightIds == null)
			return "No arguments";				// se conecteaza la baza de date airservice
		connection = ((DataSource) (new InitialContext()).lookup(JNDI_NAME)).getConnection();	
		System.out.println("Len is " + flightIds.length);
		boolean ok = true;
		for (int i = 0; i < flightIds.length; i++) {
			System.out.println("Processing flight " + flightIds[i]);
			int total = 0, booked = 0, state = Flight.CANCELED;
			Statement statement = connection.createStatement();
			ResultSet rs = statement.executeQuery("SELECT total_seats, booked_seats, state from Flight where " +
					"flight_id_official = " + flightIds[i]);
			while(rs.next()) {
				total = rs.getInt(1);
				booked = rs.getInt(2);
				state = rs.getInt(3);
			}
			// testez daca s-a facut rezervare deja sau daca s-a anulat zborul curent
			if (booked + 1 > (total * 1.1) || state == Flight.CANCELED) 
				ok = false;
			rs.close();
			statement.close();
			if (!ok)
				return "";
		}
		connection.close();					// inchide conexiunea la baza de date
			connection = ((DataSource) (new InitialContext()).lookup(JNDI_NAME)).getConnection();	
			Statement st = connection.createStatement();	// obtine ultimul ID de rezervare
			ResultSet rs = st.executeQuery("SELECT max(id) from Reservation;");
			int lastReservation = 0;
			while (rs.next())
				lastReservation = rs.getInt(1);
			rs.close();
			lastReservation++;			// creeaza o noua rezervare
			st.executeUpdate("INSERT into Reservation (id) value (" + lastReservation + ")");
			for (int i = 0; i < flightIds.length; i++) {	// creeaza rezervare la zboruri
				st.executeUpdate("INSERT INTO FlightReservation (flight_id_official, reservation_id) " +
						"value (" + flightIds[i] + ", " + lastReservation + ")");
				st.executeUpdate("UPDATE Flight set booked_seats = booked_seats + 1 " +
						" where flight_id_official = " + flightIds[i]); // actualizeaza nr. de locuri rezervate 
				System.out.println("Reserved flight" + flightIds[i]);
			}
			connection.close();					// inchide conexiunea la baza de date
			return "" + lastReservation;			// returneaza ID-ul rezervarii daca se poate face 
	}	
}