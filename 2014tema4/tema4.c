/*** 
	ARHIP ALIN-GABRIEL, GRUPA 342C3 
	TEMA 4 APD - Simularea unei retele in MPI
	Ianuarie 2015
***/ 


#include "mpi.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <unistd.h>


int main(int argc, char **argv) {

	if (argc < 3) 
	{
		printf("\nUtilizare: mpirun -np <nr_procese> main <fis_topologie> <fis_mesaje>\n");
		return 1;
	}
	
	MPI_Status status1,status2;
	int N=100;
	int rank,size,i,j,k,next[N],adiac[N][N],dist[N][N],cycle[N][N];
	
	MPI_Init(&argc, &argv); 		/* pasam argumentele prin adresa catre mediul MPI */
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);	/* obtine rangul(numarul) procesorului curent in executie */
	MPI_Comm_size(MPI_COMM_WORLD,&size);	/* obtine numarul total de procesoare disponibile */
	
	srand(time(NULL)*rank);
	int leg[N][N],nr=0,parinte=-1,lider=-1,adjunct=-1,one=1,two=2,third=3,counter=0,n,x,broadcast = 1024;
	char *over = (char*)calloc(sizeof(char), size); 
	char t[N],*tok,mesaj[N],y[20];
	
	/* FAZA 1 - Stabilirea topologiei */

	FILE *f = fopen(argv[1], "r"); /* fisierul de topologie */
	while (!feof(f)) {
		fgets(t,N, f);
		int c = atoi(strtok(t," "));
		
		if (c == rank) {
			while( (tok = strtok(NULL," ")) != NULL ) {
				c = atoi(tok);
				adiac[rank][c] = 1;
			}
      			break;
		}
	}
	fclose(f);
	
	if (rank == 0) 	// Daca sunt procesul cu rank 0
		for ( i = 0; i < size; i++)	
			if (adiac[rank][i] != 0) {	// fac un Send la vecini
				MPI_Send(&one, 1, MPI_INT, i, 1, MPI_COMM_WORLD); 
				counter++;
			}

	while (1) {
		int buf;	// primeste de la orice proces
		MPI_Recv(&buf, 1, MPI_INT, MPI_ANY_SOURCE, MPI_ANY_TAG, MPI_COMM_WORLD, &status1);
		if (buf == 1) {	// daca am primit one de la rank-ul 0
			if (parinte == -1) {	// daca parintele nu este setat (nu am ciclu) 
				parinte = status1.MPI_SOURCE;	// setez parintele nodului
				for (i = 0; i < size; i++) 	// fac broadcast la copii 
					if (i != parinte && adiac[rank][i] != 0 ) {
				    		MPI_Send(&one, 1, MPI_INT, i, 1, MPI_COMM_WORLD);
				    		counter++;
				  	}
			} else {	// altfel inseamna ca l-am setat deja si am ciclu
				  counter--;
				  cycle[rank][status1.MPI_SOURCE] = 1;
			}
		}
		
		if (buf == 2) {	// inseamna ca nu am primit de la rank-ul 0 ci de la un copil al lui
			MPI_Recv(&leg, size*N, MPI_INT, status1.MPI_SOURCE, status1.MPI_TAG, MPI_COMM_WORLD, &status2);
			i=0;
			while (i < size) {
				j=0;
				while(j<size) {
			  		adiac[i][j]|=leg[i][j];
					j++;
				}
				i++;			
			}
			counter--; // Am primit buf de la status1.MPI_SOURCE , mai ramane counter
		}
		if (buf == 3) {	// primeste de la copilul copilului rank-ului 0
			MPI_Recv(&leg, size*N, MPI_INT, status1.MPI_SOURCE, status1.MPI_TAG, MPI_COMM_WORLD, &status2);
			i=0;
			while (i < size) {
				j=0;
				while(j<size) {
					adiac[i][j]|=leg[i][j];
					j++;
				}
				i++;			
			}
			i=0;		
			while ( i < size ) {	// daca am muchie si nu sunt parinte 
				if ( adiac[rank][i]==1)
					if( i!=parinte )
						if( !cycle[rank][i] ) {
							MPI_Send(&third, 1, MPI_INT, i, 3, MPI_COMM_WORLD);
							nr++;
							MPI_Send(&adiac, N*size, MPI_INT,i, 3, MPI_COMM_WORLD); }
				i++; }
			break;	// Trimite topologia la toti copii
		}
		if (counter == 0) {
			if ( parinte != -1) {	// daca am parinte
				MPI_Send(&two, 1, MPI_INT, parinte, 2, MPI_COMM_WORLD);	// startul s-a terminat
				nr++;
				MPI_Send(&adiac, N*size, MPI_INT, parinte, 2, MPI_COMM_WORLD); }// nodul a terminat
			if ( rank == 0) {	// daca sunt rank 0
				MPI_Send(&third, 1, MPI_INT, 0, 3, MPI_COMM_WORLD);
				nr++;
				MPI_Send(&adiac, N*size, MPI_INT,0, 3, MPI_COMM_WORLD); } // S-a terminat rutarea 
		}
	}

	/*  Folosind algoritmul Floyd-Warshall calculam toate distantele minime */
	memcpy(dist, adiac, sizeof(adiac)); 
	for (k=0;k<size;k++)
		for (i=0;i<size;i++)
			for (j=0;j<size;j++)
				if (( dist[i][j] == 0 || dist[i][k] + dist[k][j] < dist[i][j] ) && 
									i!=j && dist[i][k] && dist[k][j] )
	 				 dist[i][j] = dist[i][k] + dist[k][j];

	printf("\nTabela de rutare a procesului %d este: \n",rank);
	for (i=0;i<size;i++)
		for (j=0;j<size;j++)
			if (dist[rank][i]-1 == dist[j][i] && adiac[rank][j]) {
				next[i] = j;
				printf("next hop: %d catre destinatia: %d\n",next[i],i);
				break;
			}

	if (rank == 0) {
		printf("\nMatricea de adiacenta afisata de procesul %d este: \n",rank);
		for(i = 0; i < size; i++) {
			for (j = 0; j < size; j++)
				printf("%d ",adiac[i][j]);
			printf("\n");		
		}
	}

	/* ETAPA 2 - trimiterea mesajelor intre noduri */
	
	f = fopen(argv[2],"r");
	fscanf(f,"%d", &n);
	counter = 0;
	i=0;
	while(i<n) { 
		fscanf(f,"%d %s ",&x, y);
		int dest;
		fgets(mesaj, N, f);
		int lungime = strlen(mesaj)-1;
		mesaj[lungime] = 0;
		
		if (x == rank) {
			if (y[0] != 'B') {
				sscanf(y,"%d",&dest);	/* trimit catre destinatie mesajul */
				nr++;
				MPI_Send(&dest, 1, MPI_INT, next[dest],4, MPI_COMM_WORLD);
				nr++;
				MPI_Send(&mesaj, N, MPI_CHAR, next[dest],4, MPI_COMM_WORLD); } 
			else {
				j=0;
				while(j<size) {
					if(adiac[rank][j] != 0) { 
						/* fac broadcast la mesaj */
						printf("%d face broadcast la %s \n",rank,mesaj);
						MPI_Send(&broadcast, 1, MPI_INT, j, 4, MPI_COMM_WORLD);
						MPI_Send(&mesaj, N, MPI_CHAR, j, 4, MPI_COMM_WORLD); 
					}
					j++;
				}
			}
		} else if (y[0] != 'B') {
				sscanf(y,"%d",&dest);
				if (rank == dest)
					counter++;
			}
			else 
				counter++;
		i++;
	}
  	fclose(f);
  	MPI_Barrier(MPI_COMM_WORLD);
  
	/* Rutarea mesajelor */
	while (1) {
		int buf,t;
		MPI_Recv(&buf, 1, MPI_INT, MPI_ANY_SOURCE, 4, MPI_COMM_WORLD, &status1);

		if (buf == 1025) { // daca am primit un mesaj
			MPI_Recv(&t, 1, MPI_INT,status1.MPI_SOURCE, status1.MPI_TAG, MPI_COMM_WORLD, &status2);
			int ok = 1;
			if(!over[t]) {
				i=0;
				while( i < size ) {
					if (i!=status1.MPI_SOURCE && adiac[rank][i]) {
						MPI_Send(&buf, 1, MPI_INT, i, 4, MPI_COMM_WORLD);
						nr++;
						MPI_Send(&t, 1, MPI_INT, i, 4, MPI_COMM_WORLD); }
					i++; }
			}
			over[t] = 1;
			i=0;
			while( i < size ) {
				ok&=over[i];
				i++;
			}
			if (ok != 0 )
				break;

		} else if (buf == 1024) {	// daca am primit mesaj de broadcast
			MPI_Recv(&mesaj, N, MPI_CHAR, status1.MPI_SOURCE, status1.MPI_TAG, MPI_COMM_WORLD, &status2);
			printf("%d a primit de la nodul %d si face rebroadcast la %s \n",rank,status1.MPI_SOURCE,mesaj);
			i=0;
			while( i < size ) { 	// verific sa nu am ciclu:  cycle[rank][i] != 1
				if (!cycle[rank][i] && adiac[rank][i] && i!=status1.MPI_SOURCE ) { 
					MPI_Send(&broadcast, 1, MPI_INT, i, 4, MPI_COMM_WORLD);
					nr++;
					MPI_Send(&mesaj, N, MPI_CHAR, i, 4, MPI_COMM_WORLD); }
				i++; }
			--counter;
		} else if (buf == rank) {
			MPI_Recv(&mesaj, N, MPI_CHAR, status1.MPI_SOURCE, status1.MPI_TAG, MPI_COMM_WORLD, &status2);
			printf("Mesajul %s plecat din nodul %d a ajuns la destinatie in nodul %d \n",
											mesaj,status1.MPI_SOURCE,rank);
			--counter;
		} else {
			MPI_Recv(&mesaj, N, MPI_CHAR, status1.MPI_SOURCE, status1.MPI_TAG, MPI_COMM_WORLD, &status2);
			printf("Mesajul %s plecat din nodul %d a ajuns in nodul %d si se trimite catre nodul %d\n",
									mesaj,status1.MPI_SOURCE,rank,next[buf]);
			MPI_Send(&buf, 1, MPI_INT, next[buf], 4, MPI_COMM_WORLD);
			nr++;
			MPI_Send(&mesaj, N, MPI_CHAR, next[buf], 4, MPI_COMM_WORLD); }
		if (over[rank] == 0 && counter == 0 ) {
			buf = 1025; 	// Nodul nu mai are de trimis mesaje
			MPI_Send(&buf, 1, MPI_INT, rank, 4, MPI_COMM_WORLD);
			nr++;
			MPI_Send(&rank, 1, MPI_INT, rank, 4, MPI_COMM_WORLD); }	/* S-a terminat rutarea mesajelor */
	}

	/* ETAPA 3 - ALEGEREA LIDERULUI SI LIDERULUI ADJUNCT */
	MPI_Barrier(MPI_COMM_WORLD);	
	while (lider == -1 || adjunct == -1) {
		if (rank!=0) {		// daca nu sunt rank 0 am drept de vot
			if (lider == -1) {	// daca nu a fost ales liderul
				int vot;	
				do {	// votez liderul
					vot = rand()%size;
				} while (vot==rank);
				MPI_Send(&vot, 1, MPI_INT, parinte, 5, MPI_COMM_WORLD);
				nr++;
				MPI_Recv(&vot, 1, MPI_INT, MPI_ANY_SOURCE, 5, MPI_COMM_WORLD, &status1);  
				for ( ;status1.MPI_SOURCE != parinte;) {
					MPI_Send(&vot, 1, MPI_INT, parinte, 5, MPI_COMM_WORLD);
					nr++;
					MPI_Recv(&vot, 1, MPI_INT, MPI_ANY_SOURCE, 5, MPI_COMM_WORLD, &status1);  
				}
				i=0;
				lider = vot;
				while( i < size ) {
					if ( i != rank && adiac[rank][i] && !cycle[rank][i] && i!= parinte )
						MPI_Send(&lider, 1, MPI_INT, i, 5, MPI_COMM_WORLD);
					i++;
				}	
			}
			if (adjunct == -1) {	// daca nu a fost ales liderul adjunct
				int vot;	
				do {	// votez liderul adjunct
					vot = rand()%size;
				} while (vot==rank);
				MPI_Send(&vot, 1, MPI_INT, parinte, 6, MPI_COMM_WORLD);
				nr++;
				MPI_Recv(&vot, 1, MPI_INT, MPI_ANY_SOURCE, 6, MPI_COMM_WORLD, &status1);  
				for (;status1.MPI_SOURCE != parinte;) {
					MPI_Send(&vot, 1, MPI_INT, parinte, 6, MPI_COMM_WORLD);
					nr++;
					MPI_Recv(&vot, 1, MPI_INT, MPI_ANY_SOURCE, 6, MPI_COMM_WORLD, &status1);  
				}
				i=0;
				adjunct = vot;
				while( i < size ) {
					if (adiac[rank][i] && i!= rank && !cycle[rank][i] && i!= parinte ) 
						MPI_Send(&adjunct, 1, MPI_INT, i, 6, MPI_COMM_WORLD);
					i++;
				}
			}
		} else {	// Altfel daca sunt procesul cu rank 0 , fac centralizarea voturilor
			if (lider == -1) { 	// mai intai adun voturile pentru lider
				int a[N],cnt = 0, m = -1, t;
				memset(a, 0, sizeof(a));
	
				i=0;
				while( i < size-1) {
					int buf; // apoi adun voturile pentru alegerea liderului
					MPI_Recv(&buf,1,MPI_INT,MPI_ANY_SOURCE,5,MPI_COMM_WORLD,&status1);
					a[buf]++;
					i++;
				}

				i=0;
				while( i < size ) {
					if (m == a[i])
						++cnt;
					else if (m < a[i]) {
						m = a[i];
						cnt = 1;
						t = i;
					}
					i++;
				}
				if (cnt == 1 && t!=adjunct)
					lider = t;
					
				i=0;
				while( i < size ) {
					if (i != rank && cycle[rank][i] == 0 && adiac[rank][i] != 0) 
						MPI_Send(&lider,1,MPI_INT,i,5,MPI_COMM_WORLD);
					i++;
				}
			}

			if (adjunct == -1) {  	// apoi adun voturile pentru alegerea liderului adjunct
				int a[N], cnt = 0,m = -1,t;
				memset(a, 0, sizeof(a));
				
				i=0;
				while( i < size-1 ) {
					int buf;	// primeste votul pentru liderului adjunct 
					MPI_Recv(&buf, 1, MPI_INT, MPI_ANY_SOURCE, 6, MPI_COMM_WORLD,&status1);
					a[buf]++;
					i++;
				}
	
				i=0;
				while( i < size ) {
					if (m == a[i])
						++cnt;
					else if (m < a[i]) {
						m = a[i]; 
						cnt = 1; 
						t = i;
					}
					i++;
				}
				
				if (cnt == 1 && t!=lider)
					adjunct = t;
					
				i=0;
				while( i < size ) {
					if (i != rank && cycle[rank][i] == 0 && adiac[rank][i] != 0 ) 
						MPI_Send(&adjunct, 1, MPI_INT, i, 6, MPI_COMM_WORLD);
					i++;
				}
			}
		}
	}
	free(over);	
	printf("Rank-ul %d are liderul: %d si adjunctul: %d\n",rank,lider,adjunct);
	
	MPI_Finalize();		/* iesirea din MPI*/
	return 0;
}  
