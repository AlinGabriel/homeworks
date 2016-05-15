package client;
import java.util.*;
import java.net.*;
import javax.xml.namespace.*;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import web.Flight;

/* Arhip Alin-Gabriel Tema2 SPRC
 * Implementarea unui serviciu ce gestiune a tichetelor de avion
 * Client.java - clasa client care apeleaza serviciul web.
 */
public class Client {

	public static String URL=null;		// adresa url a serviciului web
	public static String meniu = 	// crearea meniului
			"==============================================================\n"	
			+ "getOptimalRoute source, destination, maxFlights, departureDay\n"
			+ "bookTicket flightID1, flightId2, ... , flightIdn\n"
			+ "buyTicket reservationId, creditCardInfo\n"
			+ "==============================================================";	
	

	/* 
	 * apeleaza metoda buyTicket din serviciul AirService
	 */
	public void buyTicket(StringTokenizer tok)  throws Exception {
		
		String reservationId = tok.nextToken();
		String creditCardInfo = tok.nextToken();
		
		URL endpoint = new URL(Client.URL);
		Service service = new Service();
		
		Call call = (Call)service.createCall();
		call.setTargetEndpointAddress(endpoint);
		call.setOperationName(new QName("buyTicket"));						// numele operatiei
		
		call.setProperty(Call.ENCODINGSTYLE_URI_PROPERTY, "");
		QName string = new QName("http://echo.demo.oracle/", "string");
		call.addParameter("reservationId", string, String.class, javax.xml.rpc.ParameterMode.IN);
		call.addParameter("creditCardInfo", string, String.class, javax.xml.rpc.ParameterMode.IN);
		call.setTargetEndpointAddress(Client.URL);
		call.setReturnClass(String.class);

		Object[] inParams = new Object[]{reservationId, creditCardInfo};	// parametrii operatiei

		String ret = (String)call.invoke(inParams);							// invocarea operatiei
		
		if (ret.equals(""))
			System.out.println("Unable to buy ticket");
		else
			System.out.println("Bought ticket " + ret);
	}

	/* 
	 * apeleaza metoda bookTicket din serviciul AirService
	 */
	public void bookTicket(StringTokenizer tok)  throws Exception {
		
		ArrayList<String> flights = new ArrayList<String>();
		while(tok.hasMoreTokens())				
			flights.add(tok.nextToken());	// lista cu toate zborurile
		
		String[] flightsArray = new String[flights.size()];
		flightsArray = flights.toArray(flightsArray);

		URL endpoint = new URL(Client.URL); 
		Service service = new Service();
		
		Call call = (Call)service.createCall();
		call.setTargetEndpointAddress(endpoint);
		call.setOperationName(new QName("bookTicket"));						//numele operatiei
		
		call.setProperty(Call.ENCODINGSTYLE_URI_PROPERTY, "");
		call.setProperty(Call.SOAPACTION_USE_PROPERTY, new Boolean(true));
		call.setProperty(Call.SOAPACTION_URI_PROPERTY, "");
		call.setProperty(Call.ENCODINGSTYLE_URI_PROPERTY, "http://schemas.xmlsoap.org/soap/encoding/");
		call.setProperty(Call.OPERATION_STYLE_PROPERTY, "rpc");
		QName stringArray = new QName("http://echo.demo.oracle/", "stringArray");
		call.addParameter("flightIds", stringArray, String[].class, javax.xml.rpc.ParameterMode.IN);
		call.setTargetEndpointAddress(Client.URL);
		call.setReturnClass(String.class);
		
		Object[] inParams = new Object[]{flightsArray};						// parametrii operatiei

		String ret = (String) call.invoke(inParams);						// invocarea operatiei
		
		if (ret.equals(""))
			System.out.println("The reservation could not be made. There is a canceled or a full flight.");
		else
			System.out.println("Created reservation " + ret);
	}

	/* 
	 * apeleaza metoda getOptimalRoute din serviciul AirService
	 */
	public void getOptimalRoute(StringTokenizer tok)  throws Exception {
		
		String source = tok.nextToken();
		String dest = tok.nextToken();
		int maxFlights = Integer.parseInt(tok.nextToken());
		int departureDay = Integer.parseInt(tok.nextToken());

		URL endpoint = new URL(Client.URL); 
		Service service = new Service();
		
		Call call = (Call)service.createCall();
		call.setTargetEndpointAddress(endpoint);
		call.setOperationName(new QName("getOptimalRoute"));						// numele operatiei
		
		call.setProperty(Call.ENCODINGSTYLE_URI_PROPERTY, "");
		QName string = new QName("http://echo.demo.oracle/", "string");
		QName intType = new QName("http://echo.demo.oracle/", "int");
		call.addParameter("source", string, String.class, javax.xml.rpc.ParameterMode.IN);
		call.addParameter("dest", string, String.class, javax.xml.rpc.ParameterMode.IN);
		call.addParameter("maxFlights", intType, javax.xml.rpc.ParameterMode.IN);
		call.addParameter("departureDay", intType, javax.xml.rpc.ParameterMode.IN);
		call.setTargetEndpointAddress(Client.URL);
		call.setReturnClass(String[].class);
		
		Object[] inParams = new Object[]{source, dest, maxFlights, departureDay };	// parametrii operatiei

		String[] ret = (String[]) call.invoke(inParams);							// invocarea operatiei
		
		for (int i = 0; i < ret.length; i++)
			System.out.println(ret[i]);
	}
	
	/* executa comenzile */
	public static void waitForInputComand(Client cl)  throws Exception {
		Scanner sc = new Scanner(System.in);
		while(true) {
			System.out.flush();
			String com=sc.nextLine();
			StringTokenizer token=new StringTokenizer(com,", ");
			String next = token.nextToken();
			if(com.equals("quit") || com.equals("exit")) {
				System.out.println("Closing Application... : \n " + Client.meniu);
				break;
			}
			else if(com.length() == 0)
					continue;
			else if(next.equals("getOptimalRoute")) 
					cl.getOptimalRoute(token);
			else if(next.equals("bookTicket")) 
					cl.bookTicket(token);
			else if(next.equals("buyTicket"))
					cl.buyTicket(token);
			else 
				System.out.println("Unknown command. Try again!\n");
		}
		sc.close();
	}
	
	public static void main(String[] args)  throws Exception {
		if(args.length < 1 ) {
			System.out.println("Format:java Client <webservice_URL>");
			System.exit(0);
		}	
		Client.URL = args[0];				/* initializarea serviciului web */
		System.out.println(Client.meniu);	/* afisarea meniului initial */
		waitForInputComand(new Client());	/* asteapta comenzi de la utilizator */
	}
}