import java.io.*;
import java.util.*;


class Triplet<A,B,C>{
	  A f;
	  B s;
	  C t;
	  
	  public Triplet(A f, B s, C t) {
		  this.f = f;
		  this.s = s;
		  this.t = t;
      }
		  
	  public A getFirst() {
		  return f;
	  }
	  
	  public B getSecond() {
		  return s;
	  }
	
	  public C getThird() {
		  return t;
	  }
}

/* A map task */
class PartialSolution {
	int start, d;
	String f;
	
	public PartialSolution(String f, int start, int d) {
		this.f = f;
		this.start = start;
		this.d = d;
	}
}

/* A reduce task */
class Solution {
	String f;
	ArrayList<HashMap<Integer, Integer>> res;
	ArrayList<ArrayList<String>> l;
	 
	public Solution(String f, ArrayList<HashMap<Integer, Integer>> res,
			ArrayList<ArrayList<String>> l) {
		this.f = f;
		this.res = res;
		this.l = l;
	}	
}

/* A worker can execute either a map task or a reduce task */
class Worker extends Thread {
	WorkPool w;
	
	static ArrayList<Triplet<String, HashMap<Integer,Integer>, ArrayList <String>>> t = 
			new	ArrayList<Triplet<String, HashMap<Integer,Integer>, ArrayList <String>>>();
	
	static HashMap< String, Triplet<Double, HashMap<Integer,Integer>, ArrayList <String>>> tt = 
			new	HashMap< String, Triplet<Double, HashMap<Integer,Integer>, ArrayList <String>>>();
	
	public Worker(WorkPool w) {
		this.w = w;
	}
	
	long fib(final long n) {
		double p = (1 + Math.sqrt(5)) / 2;
		double q = 1 / p;
		return (long) ((Math.pow(p, n) + Math.pow(q, n)) / Math.sqrt(5));
	}

	synchronized void doMap(PartialSolution o) {
		
		RandomAccessFile doc;
		try {
			String lit, frag;
			File f = new File(o.f);
			int start = o.start - 1, end = start + o.d - 1;
			long size = f.length();
			byte[] b1 = new byte[1];
			
			doc = new RandomAccessFile(f, "r");

			/* daca fragmentul incepe in mijlocul unui cuvant,
			 * worker-ul va "sari peste" acel cuvant */
			do {
				start++;
				doc.seek(start);
				doc.read(b1);
				lit = new String(b1);
			} while (Character.isLetterOrDigit(lit.charAt(0)));

			/* daca fragmentul se termina in mijlocul unui cuvant,
			 * worker-ul va prelucra si acel cuvant */
			do  {
				end++;
				if (end > size) {
					end = (int) size;
					break;
				}
				doc.seek(end);
				doc.read(b1);
				lit = new String(b1);
			} while (Character.isLetterOrDigit(lit.charAt(0)));
			
			byte[] b = new byte[ end - start];
			doc.seek(start);
			doc.read(b);
			frag = new String(b).toLowerCase();
			doc.close();
			
			StringTokenizer ts = new StringTokenizer(frag, 
					";:/?~\\.,><~`[]{}()!@#$%^&-_+\'=*\"| \t\n");
			
			String max = "";
			HashMap<Integer,Integer> res = new HashMap<Integer,Integer>();
			ArrayList <String> l = new ArrayList<String>();
			while (ts.hasMoreTokens()) {
				String t = ts.nextToken();
				int key = t.length();
				if (res.containsKey(key))
					res.put(key, res.get(key) + 1);
				else
					res.put(key, 1);
				if (key == max.length())
					l.add(t);
				else if ( key > max.length()) {
					max = t;
					l.clear();
					l.add(max);
				}
			}
			
			t.add(new Triplet<String, HashMap<Integer,Integer>, ArrayList <String>>(
					o.f, res, l)); 
		} catch(Exception e) {
				e.printStackTrace();
		}
	}
	
	synchronized void doReduce(Solution o) {
		int c = -1;
		double sum = 0, nr = 0, rang = 0;
		
		/* combination stage */
		for (HashMap<Integer, Integer> r:o.res){
			c++;
			if (!tt.containsKey(o.f)) {
					tt.put(o.f, new Triplet<Double, HashMap<Integer,Integer>, ArrayList<String>>(
							rang, r, o.l.get(c)));
					continue;
			}
			
			HashMap<Integer, Integer> temp;
			ArrayList<String> temp2;
			temp = tt.get(o.f).getSecond();
			temp2 = tt.get(o.f).getThird();
			
			for (int j: r.keySet()){
				if (temp.containsKey(j))
					temp.put(j, temp.get(j) + r.get(j));
				else 
					temp.put(j, r.get(j));
			}
			
			for (int i = 0 ; i < o.l.get(c).size() ; i++)
				temp2.add(o.l.get(c).get(i));
			
			String max = "";
			ArrayList<String> rez = new ArrayList<String>();
			for (int k = 0 ; k < temp2.size() ; k++)
				if (temp2.get(k).length() == max.length())
					rez.add(temp2.get(k));
				else if (temp2.get(k).length() > max.length()){
					max = temp2.get(k);
					rez.clear();
					rez.add(max);
				}
		
			if (c + 1 == o.res.size()) {
				/* process stage */
				for (int key: tt.get(o.f).getSecond().keySet()) {
					sum += fib(key + 1) * tt.get(o.f).getSecond().get(key);
					nr += tt.get(o.f).getSecond().get(key);
				}
				rang = sum/nr;
			}

			tt.put(o.f, new Triplet<Double, HashMap<Integer,Integer>, ArrayList<String>>(
					rang, temp, rez));
		}
	}
	
	public void run() {
		while (true) {
			Object o = w.getWork();
			if (o instanceof PartialSolution)
				doMap((PartialSolution)o);
			else if (o instanceof Solution)
				doReduce((Solution)o);
			else
				break;
		}
	}
}
 
/* Main - master implementation */
public class tema2 {

	public static void main(String args[]) throws Exception {
		
		if (args.length != 3) {
			System.out.println("java tema2 NT INPUTFILE OUTPUTFILE");
			System.exit(args.length);
		}
		
		/* read input */
		int nt = Integer.parseInt(args[0]);
		RandomAccessFile fin = new RandomAccessFile(new File(args[1]), "r");
		int d = Integer.parseInt(fin.readLine());
		int nd = Integer.parseInt(fin.readLine());
		
		ArrayList<String> files = new ArrayList<String>();
		for (int i = 0 ; i < nd ; i++)
			files.add(fin.readLine());
		
		fin.close();
		WorkPool wp = new WorkPool(nt);
		
		/* put map work for every fragment of the file, for every file */
		for (int i = 0 ; i < files.size() ; i++) {
			String filename = files.get(i);
			File f = new File(filename);
			long size = f.length();
			int index = 0;
			
			
			while ( index < size) {
				wp.putWork(new PartialSolution(filename, index, d));
				index += d;
			}
		}
	
		/* workers execute map tasks */
		Worker w[] = new Worker[nt];
		for (int k = 0 ; k < nt ; k++) {
			w[k] = new Worker(wp);
			w[k].start();
		}
		
		for (int k = 0 ; k < nt ; k++)
			w[k].join();
		
		/* put reduce work for every file */
		for (int k = 0 ; k < files.size() ; k++) {
			 ArrayList<HashMap<Integer,Integer>> res = 
					 new ArrayList<HashMap<Integer,Integer>>();
			 ArrayList<ArrayList <String>> l = 
					 new ArrayList<ArrayList <String>>();
			 
			 for (int i = 0 ; i < Worker.t.size() ; i++) {
				if (Worker.t.get(i) != null)
					 if (files.get(k).equals(Worker.t.get(i).getFirst())) {
						 res.add(Worker.t.get(i).getSecond());
				 		 l.add(Worker.t.get(i).getThird());
					 }
			 }
			 
			 wp.putWork(new Solution(files.get(k), res, l));
		}
		
		/* workers execute reduce tasks */
		for (int k = 0 ; k < nt ; k++) {
			w[k] = new Worker(wp);
			w[k].start();
		}
		
		for (int k = 0 ; k < nt ; k++)
			w[k].join();
		
		/* write output */
		TreeMap <Double, String> res = 
				new TreeMap<Double, String>(Collections.reverseOrder());
		for (String f: Worker.tt.keySet())
			res.put(Worker.tt.get(f).getFirst(), f);

		Writer fout = new FileWriter(args[2]);
		for (Double rang: res.keySet()) {
			String f = res.get(rang);
			ArrayList <String> l = Worker.tt.get(f).getThird();
			HashSet<String> s = new HashSet<String>(l);
			fout.write(f + ";" + String.format("%.2f", rang) + ";" + 
					"[" + l.get(0).length() + "," + s.size() +"]\n");
		}
		fout.close();
	}
}