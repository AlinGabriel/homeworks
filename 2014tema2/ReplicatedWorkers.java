import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

/* Tema 2 APD
 * Arhip Alin-Gabriel 342C3
 * Noiembrie 2014
 * ReplicatedWorkers.java - Implementare paralela utilizand 
 * paradigma Replicated Workers si modelul MapReduce. 
 */

/* Clasa ajutatoare de tip pereche(nume_fisier,hash_partial)
 * hash_partial rezultat in urma unui MAP pe un fragment
 * din nume_fisier */
class Pair<A, B> {
    private A first;
    private B second;

    public Pair(A first, B second) {
    	super();
    	this.first = first;
    	this.second = second;
    }

    public A getFirst() {
    	return first;
    }

    public void setFirst(A first) {
    	this.first = first;
    }

    public B getSecond() {
    	return second;
    }

    public void setSecond(B second) {
    	this.second = second;
    }
}

/**
 * Clasa ce reprezinta o solutie partiala pentru problema de rezolvat. Aceste
 * solutii partiale constituie task-uri care sunt introduse in workpool.
 */
class OperationMap {
	RandomAccessFile doc;
	int start,d;
	String filename;
	byte[] b;
	/* rezultatul partial al unui MAP (cheie->cuvant,valoare->aparitie) */
	HashMap<String,Integer> rez_map;
	/* Pereche ( nume_fisier , rezultat partial al unui MAP ) */
	static ArrayList<Pair<String,HashMap<String,Integer>>> p =
			 new ArrayList<Pair<String,HashMap<String,Integer>>>();
				
	public OperationMap(String filename,RandomAccessFile doc,int start,int d)
	{
		this.filename = filename;
		this.doc = doc;
		this.start = start;
		this.d = d;
		this.rez_map = new HashMap<String,Integer>(); 
	}
	public void map() throws IOException 
	{
		synchronized(doc) {	// sincronizez accesul la doc 
			doc.seek(start);	// pozitionez offsetul de inceput	
			b = new byte[d];	 
			doc.read(b); 		// citesc fragmentul
		}
		String fragment = new String(b);	
		int end = fragment.length() - 1;
		
		while(Character.isLetterOrDigit(fragment.charAt(end)))	{						
			end--;				// sa nu se termine fragmentul in
		}						// mijlocul unui fragment
		
		fragment = fragment.substring(0,end+1).toLowerCase();	
		StringTokenizer tokens = new StringTokenizer(fragment,
				"_;:/?~\\.,><~`[]{}()!@#$%^&-+\'=*\"| \t\n");
		
		while(tokens.hasMoreTokens()) 				// cat timp mai exista cuvinte
		{
			String cuv = tokens.nextToken();		// extrage un cuvant
			if(rez_map.containsKey(cuv)) 			// daca exista in hash
			{
				rez_map.put(cuv,rez_map.get(cuv)+1);//incrementeaza aparitia lui	
			}
			else 									// altfel adauga cuv in hash
			{
				rez_map.put(cuv,1);					// cu o aparitie
			}
		}
		synchronized(p) {
			p.add(new Pair<String,HashMap<String,Integer>>(filename, rez_map));
		}
	}
}

class OperationReduce {
	String filename;
	ArrayList<HashMap<String,Integer>> lista_hashuri;// lista de hash-uri partiale
	
	/* rezultatul unui REDUCE (nume_fisier,(cuvant,aparitii)) */
	static HashMap<String,HashMap<String,Integer>> rez_reduce  = 	
	  		new HashMap<String,HashMap<String,Integer>>(); 
	
	/* in intermediar retin temporar (daca exista) hash-ul 
	 * lui filename din rez_reduce pentru a il actualiza */
	HashMap<String,Integer> intermediar;
	
	/* primesc numele fisierului si lista cu hash-urile partiale */
	public OperationReduce(String filename,ArrayList<HashMap<String,Integer>> 
	lista_hashuri) 
	{
		this.filename = filename;
		this.lista_hashuri=new ArrayList<HashMap<String, Integer>>(lista_hashuri);
		this.intermediar = new HashMap<String, Integer>();
	}	
	
	public void reduce() 
	{		
		synchronized(rez_reduce) {
			for (HashMap<String,Integer> rez_map : lista_hashuri ) 
			{
				if(rez_reduce.containsKey(filename)) // daca reduce contine documentul filename 
				{
					intermediar=rez_reduce.get(filename);	 // extrag hash-ul (cuvant,aparitii) din lista
					
					for(String cuv:rez_map.keySet()) 	// si pentru fiecare cuvant din hash-ul partial al doc
					{
						if(intermediar.containsKey(cuv))	// daca exista si in hash-ul general al doc
						{
							intermediar.put(cuv,intermediar.get(cuv) + rez_map.get(cuv));
						}
						else
						{
							intermediar.put(cuv,rez_map.get(cuv));
						}
					}
					rez_reduce.put(filename, intermediar);	// pun rezultatul in reduce;
				}
				else 
				{
					rez_reduce.put(filename,rez_map);
				}
			}
		}
	}
}

class OperationCompare {
	HashMap<String,Integer> hash1,hash2;
	String file1,file2;
	// frecventele de aparitie a cuvintelor din file1,file2:
	HashMap<String,Double> frecvente1,frecvente2; 
	// hash cu cele 2 fisiere:
	HashMap<String,String> files;
	/* rezultatul operatiei COMPARE */
	static TreeMap<Double,HashMap<String,String>>  result = 
			new TreeMap<Double,HashMap<String,String>>(Collections.reverseOrder());
	
	public OperationCompare(String file1,HashMap<String,Integer> hash1,
				String file2,HashMap<String,Integer> hash2) 
	{
		this.file1=file1;
		this.hash1=hash1;
		this.file2=file2;
		this.hash2=hash2;
		this.frecvente1 = new HashMap<String,Double>();
		this.frecvente2 = new HashMap<String,Double>();
		this.files = new HashMap<String,String>();
	}
	
	public void compare() 
	{
		int total1=0,total2=0;	// nr total de cuvinte
		double nr1=0,nr2=0,grad=0;
		
		for(String cuv: hash1.keySet()) // pentru fiecare cuvant din hash1
		{
			total1+=hash1.get(cuv); 	// aduna aparitiile lui in "total"
		}							
		for (String cuv:hash1.keySet()) 	// pentru fiecare cuvant din hash1
		{							
			/* calculeaza frecventa de aparitii a cuv din file1 */
			nr1=((double)hash1.get(cuv)/total1)*100;
			nr1=(double)((long)(nr1*10000))/10000; //retin 4 zecimale prin cast
			frecvente1.put(cuv,nr1);		
		}
	
		for(String cuv: hash2.keySet()) // pentru fiecare cuvant din hash2
		{
			total2+=hash2.get(cuv); 	// aduna aparitiile lui in "total"
		}							
		for (String cuv:hash2.keySet()) 	// pentru fiecare cuvant din hash2
		{							
			/* calculeaza frecventa de aparitii a cuv din file2 */
			nr2=((double)hash2.get(cuv)/total2)*100;
			nr2=(double)((long)(nr2*10000))/10000; //retin 4 zecimale prin cast
			frecvente2.put(cuv,nr2);		
		}

		/* calcuam gradul de similaritate */
		for(String cuv:frecvente1.keySet())	// pentru fiecare cuvant din file1
		{
			if(frecvente2.containsKey(cuv))	// daca se gaseste in file2
			{
				grad += frecvente1.get(cuv) * frecvente2.get(cuv);
			}										
		}
		grad=(double)grad/100;

		synchronized(files) {
		files.put(file1,file2);
		}
		// Sincronizare rezultat
		synchronized(result) {
			result.put(grad,files);
		}
	}
}

/**
 * Clasa ce reprezinta un thread worker.
 */
class Worker extends Thread {
	WorkPool wp;
	int type;
	
	public Worker(WorkPool workpool,int type) {
		this.wp = workpool;
		this.type = type;
	}
	/**
	 * Procesarea unei solutii partiale. Aceasta poate implica generarea unor
	 * noi solutii partiale care se adauga in workpool folosind putWork().
	 * Daca s-a ajuns la o solutie finala, aceasta va fi afisata.
	 * @throws IOException 
	 */
	void processOperationMap(OperationMap op) throws IOException {
		op.map();				// creeaza toate map-urile
	}
	void processOperationReduce(OperationReduce or) {
		or.reduce();			// realizeaza combinarea hash-urilor
	}
	void processOperationCompare(OperationCompare oc) {
		oc.compare();			// compara doua documente
	}
	public void run() {
		//System.out.println("Thread-ul worker " + this.getName() + " a pornit...");
		while (true) {
			if (type == 1) 
			{
				OperationMap op = wp.getMapWork();
				if (op == null)
					break;
				try {
					processOperationMap(op);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (type == 2) 
			{
				OperationReduce or = wp.getReduceWork();
				if (or == null)
					break;
				processOperationReduce(or);
			}
			if (type == 3) 
			{
				OperationCompare oc = wp.getCompareWork();
				if (oc == null)
					break;
				processOperationCompare(oc);
			}
		}
		//System.out.println("Thread-ul worker " + this.getName() + " s-a terminat...");
	}
}


public class ReplicatedWorkers {
	public static RandomAccessFile fin=null;

	public static void main(String args[]) throws NumberFormatException, 
	IOException, InterruptedException 
	{	
		if (args.length != 3)
	    {
			System.out.println(" Format de utilizare: java " +
					"ReplicatedWorkers numar_threaduri " +
					"fisier_input fisier_output ");
			System.exit(1);
	    }
		
		int nt = Integer.parseInt(args[0]);	// nt - numar de threaduri
		fin = new RandomAccessFile(new File(args[1]),"r");
		Writer fout = new FileWriter(args[2]);
		int d = Integer.parseInt(fin.readLine());
		float x = Float.parseFloat(fin.readLine());
		int nd = Integer.parseInt(fin.readLine());	
		
		int i,j,k,start,fragments,end;
		byte[] octet = new byte[1];
		String filename,literal,file1,file2;
		Vector<String> filenames = new Vector<String>();
		HashMap<String,Integer> hash1,hash2;
		HashMap<String,String> files;
		ArrayList<HashMap<String,Integer>> lista_hashuri ;
		
		Worker workers[] = new Worker[nt];
		/* cele 3 pool-uri: unul de map, unul de reduce si unul de compare */
		WorkPool map_pool = new WorkPool(nt);		
		WorkPool reduce_pool = new WorkPool(nt);
		WorkPool compare_pool = new WorkPool(nt);
		
		for(i=0;i<nd;i++) // pentru fiecare document
		{														
			filename=fin.readLine();	// ii citesc numele si il deschid ptr citire
			filenames.add(filename);
			RandomAccessFile doc = new RandomAccessFile(new File(filename),"r");	
			fragments = (int) Math.ceil((float)doc.length()/d);// nr. de fragmente
			start = 0;		// offsetul de inceput in fragment e pe 0 
			end = d-1;  	// offsetul de sfarsit in fragment e pe 499 
			
			for(j=0;j<fragments;j++) 	
			{
				doc.seek(end);
				doc.read(octet);				// citeste 1 octet in b 
				literal = new String(octet);	// converteste-l la String
				
				while(Character.isLetterOrDigit(literal.charAt(0)))		
				{								
					end--;						
					doc.seek(end);				// daca e litera, inapoi
					doc.read(octet);   			// pana la primul nonliteral	
					literal = new String(octet);
				}
				/*	ptr fiecare fragment se creeaza cate un task MAP */
				map_pool.putMapWork(new OperationMap(filename,doc,start,d));
				start = end+1;	// offsetul urmatorului fragment
				end += d;		// map_tasks.size() = fragments 
			}
		}
		
		/* Realizez operatiile de MAP */
		for(k=0;k<nt;k++) {
			workers[k] = new Worker(map_pool,1);
		}
		for(k=0;k<nt;k++) {
			workers[k].start();	// threadurile realizeaza MAP
		}
		for(k=0;k<nt;k++) {		// astept threadurile sa termine toate MAP-urile
			workers[k].join();	// altfel lista_hashuri ar face add cu null
		}
		
		for(i=0;i<filenames.size();i++) {
			lista_hashuri = new ArrayList<HashMap<String,Integer>>(); 
			for(j=0;j<OperationMap.p.size();j++) {
				if (OperationMap.p.get(j).getFirst().equals(filenames.get(i)))
					lista_hashuri.add(OperationMap.p.get(j).getSecond());
			}
			if (filenames.get(i) != null && lista_hashuri != null)
				reduce_pool.putReduceWork(new OperationReduce
						(filenames.get(i),lista_hashuri));
		}
		
		for(k=0;k<nt;k++) {
			workers[k] = new Worker(reduce_pool,2);
		}
		for(k=0;k<nt;k++) {
			workers[k].start();	// threadurile realizeaza REDUCE
		}
		for(k=0;k<nt;k++) {		// astept threadurile sa termine toate REDUCE-urile
			workers[k].join();}	// altfel compare s-ar face cu null
				
		for(i=0;i<filenames.size()-1;i++) {	// ultimul file nu mai avem cu cine
			file1 = filenames.elementAt(i);	// sa l mai comparam
			hash1 = OperationReduce.rez_reduce.get(file1);
			for(j=i+1;j<filenames.size();j++) {
				file2 = filenames.elementAt(j);
				hash2 = OperationReduce.rez_reduce.get(file2);
				if (!file1.equals(file2)) //nu creeaza task COMPARE daca file1 == file2
					compare_pool.putCompareWork(
							new OperationCompare(file1,hash1,file2,hash2));
			}	
		}
		
		for(k=0;k<nt;k++) {
			workers[k] = new Worker(compare_pool,3);
		}
		for(k=0;k<nt;k++) {
			workers[k].start();
		}
		for(k=0;k<nt;k++) {		// asteapta threadurile COMPARE sa termine
			workers[k].join();	// pentru a muri inaintea threadului Main
		}
				
		for(Double grad:OperationCompare.result.keySet()) {
			files = OperationCompare.result.get(grad);
			if ( grad >= x )
				for(String file: files.keySet())
					fout.write(file + ";" + files.get(file) + ";" +
									String.format("%.4f",grad) + "\n");
		}
		
		fout.close();
	}
}