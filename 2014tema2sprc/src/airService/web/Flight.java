package web;


/**
 * Clasa ce reprezinta un zbor cu toate informatiile acestuia: 
 * id-ul zborului,sursa,destinatie,data,durata, locuri libere.
 */
public class Flight {
	
	public int id, hour,day,duration; 
	public String source, dest, flightIdOfficial;
	public int state, totalSeats, bookedSeats;
	public int cost = 600000, noFlights = 0;
	
	public Flight previous = null;
	public static final int AVAILABLE	= 0;
	public static final int CANCELED	= 1;
	
	public Flight(String source, String dest) {
		this.dest = dest;
		this.source = source;
	}
	public Flight(int id, int hour, int day, int duration, int state, int totalSeats,
			int bookedSeats, String flightIdOfficial, String source, String dest) {
		this.id = id;
		this.source = source;
		this.dest = dest;
		this.hour = hour;
		this.day = day;
		this.duration = duration;
		this.state = state;
		this.totalSeats = totalSeats;
		this.bookedSeats = bookedSeats;
		this.flightIdOfficial = flightIdOfficial;
	}
	public int compareTo(Flight flight) {
		return this.cost - flight.cost;
	}
	public String toString() {
		if (source.equals(dest))
			return "Dummy root flight";
		return "Flight " + flightIdOfficial +" (" + source + " - " + dest + " day " + day + 
				" hour " + hour + " duration " + duration + " state " + state + " booked " +
				bookedSeats + " seats out of " + totalSeats + "[noFlights " + noFlights + "]) ";
	}	
}