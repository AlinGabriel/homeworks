/*** 
	ARHIP ALIN-GABRIEL, GRUPA 342C3 
	TEMA 3 APD - Calcul paralel folosind MPI
	Decembrie 2014
***/ 

#include "mpi.h"
#include <stdio.h>
#include <stdlib.h>
#include <math.h>


int algorithm(int tip,double x_min,double y_min,double res,
		int max_steps,double a,double b, int i, int j) 
{
	int step = 0;
	double Zre,Zim,Cre,Cim,copyOfZre;

	if(tip == 1 ) 
	{
		/* inializare numere complexe Julia */
		Zre = x_min + j * res;
		Zim = y_min + i * res;
		Cre = a;
		Cim = b;
		copyOfZre = Zre;
	}
	else 
	{
		/* inializare numere complexe Mandelbrot */
		Zre = 0;
		Zim = 0;
		Cre = x_min + j * res;
		Cim = y_min + i * res;
		copyOfZre = Zre;
	}
	
	/* algoritmul propriu-zis */
	while(Zre * Zre + Zim * Zim < 4 && step < max_steps) {
		Zre = Zre * Zre - Zim * Zim;
		Zim = 2 * copyOfZre * Zim;
		Zre += Cre;
		Zim += Cim;
		copyOfZre = Zre;
		step++;
	}
	
	return step % 256;
	
}

void worker() {

	int i,j,tagS=2,tagR=1;
	double recvbuf[80];
	MPI_Status Stat;
	
	/* PAS 1 - colectare informatii de la master */
	
	MPI_Recv(recvbuf,12,MPI_DOUBLE,0,tagR,MPI_COMM_WORLD,&Stat);
	int *sendbuf = malloc(((int)recvbuf[9]+1) * sizeof(int));
	
	/* PAS 2 - aplica algoritmul corespunzator in punctul(i,j) in functie de tipul multimii */
	
	for(i=(int)recvbuf[0];i<(int)recvbuf[1];i++) 
	{
		sendbuf[0] = i;

		for(j=0;j<(int)recvbuf[9];j++)
			sendbuf[j+1] = algorithm((int)recvbuf[2],recvbuf[3],recvbuf[5],recvbuf[7],
			 			(int)recvbuf[8],recvbuf[10],recvbuf[11],i,j);

		MPI_Send(sendbuf,(int)recvbuf[9]+1,MPI_INT,0,tagS,MPI_COMM_WORLD);
	}
	
}

void master(FILE *fin, FILE *fout,int size) {

	int tip,max_steps,width,height,lines,rest=0,i,j,start=0,tagS=1,tagR=2;
	double x_min,x_max,y_min,y_max,res,a=0,b=0,sendbuf[80];
	MPI_Status Stat;
	
	/* PAS 1 - citire fisier input */
	
	/* tip: 0 pentru Mandelbrot; 1 pentru Julia) */
	fscanf(fin,"%d",&tip);
	/* Intervalele pe care se va lucra sunt [x_min,x_max) si [y_min,y_max). */
	fscanf(fin,"%lf %lf %lf %lf",&x_min, &x_max, &y_min, &y_max);
	/* rezoluția (pasul) în cadrul subspațiului ales */
	fscanf(fin,"%lf",&res);
	/* numărul maxim de iterații pentru generarea mulțimilor */
	fscanf(fin,"%d",&max_steps);
	
	/* în cazul în care se realizeaza calcului mulțimii Julia  */
	if(tip == 1)
		/* parametrul complex  al funcției */
		fscanf(fin,"%lf %lf",&a,&b);
	
	fclose(fin);
	
	/* PAS 2 - creare si pasare lucru catre fiecare worker */
	
	width = floor((x_max-x_min)/res);
	height = floor((y_max-y_min)/res);
	int *recvbuf = malloc((width+1) * sizeof(int));
	int **output = malloc(height * sizeof(int*)); 
	/* cate linii prelucreaza fiecare proces (inclusiv master-ul)*/
	lines = height / size;
	/* linii ramase de prelucrat de ultimul proces */
	rest = height % size;
	/* setam startul de la "lines" pentru celelalte procese 
	ca sa avem rezervate primele "lines" linii pentru 
	a fi prelucrate de catre procesul master */ 
	start = lines;	
	
	for(i=1;i<size;i++) 
	{
		if (i == size - 1 && rest != 0)
			lines += rest;
		
		sendbuf[0] = start;
		sendbuf[1] = start + lines;
		sendbuf[2] = tip;
		sendbuf[3] = x_min;
		sendbuf[4] = x_max;
		sendbuf[5] = y_min;
		sendbuf[6] = y_max;
		sendbuf[7] = res;
		sendbuf[8] = max_steps;
		sendbuf[9] = width;
		sendbuf[10] = a;
		sendbuf[11] = b;

		/* Semnatura MPI_Send(buffer,count,type,dest,tag,comm) */
		MPI_Send(sendbuf,12,MPI_DOUBLE,i,tagS,MPI_COMM_WORLD);	 	
	 	start += lines;
	 }
	 
	 /* PAS 3 - prelucrarea liniilor de catre procesul master */
	 
	for(i=0;i<height;i++) 
		output[i] = malloc(width * sizeof(int)); 
	 
	for(i=0;i<lines;i++) 
	{
		for(j=0;j<width;j++)
			output[i][j] = algorithm(tip,x_min,y_min,res,max_steps,a,b,i,j);

	}
	 
	 /* PAS 4 - colectare rezultate linie cu linie, de la celelalte procese, creare output */
	 
	for(i=lines;i<height;i++) 
	{
		/* Semnatura MPI_Recv(buffer,count,type,source,tag,comm,status) */
		MPI_Recv(recvbuf,width+1,MPI_INT,MPI_ANY_SOURCE,tagR,MPI_COMM_WORLD,&Stat);
		for(j=0;j<width;j++)
			output[(int)recvbuf[0]][j] = recvbuf[j+1];
	} 
	
	/* PAS 5 - afisare output in format pgm*/
	
	fprintf(fout,"%s\n","P2");
	fprintf(fout,"%d %d\n",width,height);
	fprintf(fout,"%d\n",255);
	
	for(i=height-1;i>=0;i--) 
	{
		for(j=0;j<width;j++)
			fprintf(fout,"%d ",output[i][j]);
		fprintf(fout,"\n");
	}
	
	fclose(fout);
	
}


int main(int argc, char* argv[] ) {

	if (argc < 3) 
	{
		printf("\nUtilizare: mpirun -np <nr_procese> <executabil> <input> <output>\n");
		return 1;
	}
	
	int rank, size;
	FILE *fin = fopen(argv[1],"r");	/* fisierul de input */
	FILE *fout = fopen(argv[2],"w"); /* fisierul de output */

	/* pasam argumentele prin adresa catre procesul MPI */
	MPI_Init(&argc, &argv); 
	/* obtine rangul(numarul) procesorului curent in executie */
	MPI_Comm_rank(MPI_COMM_WORLD,&rank);	
	/* obtine numarul total de procesoare disponibile */
	MPI_Comm_size(MPI_COMM_WORLD,&size);

	switch(rank) 
	{
		case 0: 
			/* initalizare proces master */
			master(fin,fout,size);
			break;
		default:
			/* efectuarea lucrului de catre fiecare worker */
			worker();
			break;
	}
	
	/* iesirea din MPI*/
	MPI_Finalize();
	
	return 0;
}  
