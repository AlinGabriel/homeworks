import java.util.LinkedList;

/* Tema 2 APD
 * Arhip Alin-Gabriel 342C3
 * Noiembrie 2014
 * WorkPool.java - fisier preluat din laboratorul 5 de APD,
 * modificat astfel incat sa am metode de GET si PUT pentru 
 * fiecare din cele 3 operatii : MAP, REDUCE si COMPARE. 
 */

/**
 * Clasa ce implementeaza un "work pool" conform modelului "replicated workers".
 * Task-urile introduse in work pool sunt obiecte de tipul OperationMap,
 * OperationReduce si OperationCompare.
 */
public class WorkPool {
	int nThreads; // nr total de thread-uri worker
	int nWaiting = 0; // nr de thread-uri worker care sunt blocate asteptand un task
	public boolean ready = false; // daca s-a terminat complet rezolvarea problemei 
	
	LinkedList<OperationMap> map_tasks = new LinkedList<OperationMap>();
	LinkedList<OperationReduce> reduce_tasks = new LinkedList<OperationReduce>();
	LinkedList<OperationCompare> compare_tasks = new LinkedList<OperationCompare>();

	/**
	 * Constructor pentru clasa WorkPool.
	 * @param nThreads - numarul de thread-uri worker
	 */
	public WorkPool(int nThreads) {
		this.nThreads = nThreads;
	}

	/**
	 * Functie care incearca obtinera unui task din workpool.
	 * Daca nu sunt task-uri disponibile, functia se blocheaza pana cand 
	 * poate fi furnizat un task sau pana cand rezolvarea problemei este complet
	 * terminata
	 * @return Un task de rezolvat, sau null daca rezolvarea problemei s-a terminat 
	 */
	public synchronized OperationMap getMapWork() {
		if (map_tasks.size() == 0) { // workpool gol
			nWaiting++;
			/* condtitie de terminare:
			 * nu mai exista nici un task in workpool si nici un worker nu e activ 
			 */
			if (nWaiting == nThreads) {
				ready = true;
				/* problema s-a terminat, anunt toti ceilalti workeri */
				notifyAll();
				return null;
			} else {
				while (!ready && map_tasks.size() == 0) {
					try {
						this.wait();
					} catch(Exception e) {e.printStackTrace();}
				}
				
				if (ready)
				    /* s-a terminat prelucrarea */
				    return null;

				nWaiting--;
				
			}
		}
		return map_tasks.remove();

	}
	public synchronized OperationReduce getReduceWork() {
		if (reduce_tasks.size() == 0) { // workpool gol
			nWaiting++;
			/* condtitie de terminare:
			 * nu mai exista nici un task in workpool si nici un worker nu e activ 
			 */
			if (nWaiting == nThreads) {
				ready = true;
				/* problema s-a terminat, anunt toti ceilalti workeri */
				notifyAll();
				return null;
			} else {
				while (!ready && reduce_tasks.size() == 0) {
					try {
						this.wait();
					} catch(Exception e) {e.printStackTrace();}
				}
				
				if (ready)
				    /* s-a terminat prelucrarea */
				    return null;

				nWaiting--;
				
			}
		}
		return reduce_tasks.remove();

	}
	public synchronized OperationCompare getCompareWork() {
		if (compare_tasks.size() == 0) { // workpool gol
			nWaiting++;
			/* condtitie de terminare:
			 * nu mai exista nici un task in workpool si nici un worker nu e activ 
			 */
			if (nWaiting == nThreads) {
				ready = true;
				/* problema s-a terminat, anunt toti ceilalti workeri */
				notifyAll();
				return null;
			} else {
				while (!ready && compare_tasks.size() == 0) {
					try {
						this.wait();
					} catch(Exception e) {e.printStackTrace();}
				}
				
				if (ready)
				    /* s-a terminat prelucrarea */
				    return null;

				nWaiting--;
				
			}
		}
		return compare_tasks.remove();

	}


	/**
	 * Functie care introduce un task in workpool.
	 * @param sp - task-ul care trebuie introdus 
	 */
	synchronized void putMapWork(OperationMap om) {
		map_tasks.add(om);
		//System.out.println("WorkPool - adaugare task MAP: " 
		//							+ map_tasks.size());
		/* anuntam unul dintre workerii care asteptau */
		this.notify();

	}
	synchronized void putReduceWork(OperationReduce or) {
		reduce_tasks.add(or);
		//System.out.println("WorkPool - adaugare task REDUCE: " 
		//							+ reduce_tasks.size());
		/* anuntam unul dintre workerii care asteptau */
		this.notify();

	}
	synchronized void putCompareWork(OperationCompare oc) {
		compare_tasks.add(oc);
		//System.out.println("WorkPool - adaugare task COMPARE: " 
		//							+ compare_tasks.size());
		/* anuntam unul dintre workerii care asteptau */
		this.notify();

	}


}


