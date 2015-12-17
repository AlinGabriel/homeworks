/* Tema 1 APD
 * Arhip Alin-Gabriel 342C3
 * Octombrie 2014
 * tema1.c - Implementare seriala + paralelizare utilizand OpenMP
 */

#include <stdio.h>
#include <stdlib.h>
#include <omp.h>

// functie ce returneaza numarul de vecini ai unui individ:
int getNeighborCount(int i,int j,int h,int w,int n[h+10][w+10]) 
{ 
	int nr=0;
	if(n[i-1][j-1] == 1) {
		nr++;
	}
	if(n[i-1][j] == 1) {
		nr++;
	}
	if(n[i-1][j+1] == 1) {
		nr++;
	}
	if(n[i][j+1] == 1) {
		nr++;
	}
	if(n[i+1][j+1] == 1) {
		nr++;
	}
	if(n[i+1][j] == 1) {
		nr++;
	}
	if(n[i+1][j-1] == 1) {
		nr++;
	}
	if(n[i][j-1] == 1) {
		nr++;
	}
	return nr;
}

// functie care converteste matricea intr-un toroid
void makeToroidalMatrix(int h,int w,int a[h+10][w+10]) 
{
	int i,j;
	#pragma omp parallel for private(j) shared(h,w,a)
	for(j=1;j<w+1;j++) 
	{
		a[0][j]=a[h][j];	// initializeaza linia 0 cu linia h
		a[h+1][j]=a[1][j];	// initializeaza linia h+1 cu linia 1
	}	
	#pragma omp parallel for private(i) shared(h,w,a)
	for(i=1;i<h+1;i++) 
	{
		a[i][0]=a[i][w];	// initializeaza coloana 0 cu coloana w
		a[i][w+1]=a[i][1];	// initializeaza coloana w+1 cu coloana 1
	}
	// acum initializam colturile:
	a[0][0]=a[h][w];	// coltul stanga sus 
	a[0][w+1]=a[h][1];	// coltul dreapta sus
	a[h+1][0]=a[1][w];	// coltul stanga jos
	a[h+1][w+1]=a[1][1];	// coltul dreapta jos
}


int main(int argc, char *argv[]) {

	char letter;
	int w_harta,h_harta,w,h,i,j,k,n;
	FILE *fd,*fd2;
	
	omp_set_num_threads(atoi(argv[1]));	//setez numarul de threaduri
	n = atoi(argv[2]);			// n=nr. de etape
	fd = fopen(argv[3],"r");
	
	// citire date din fisierul de intrare
	if( fd == NULL ) 
	{
		perror("eroare la deschiderea fisierului\n");
		exit(EXIT_FAILURE);
	}
	fscanf(fd, "%c %d %d %d %d",&letter,&w_harta,&h_harta,&w,&h);
	int m[h+10][w+10];	// matricea citita initial din fisier
	int term[h>h_harta?h+10:h_harta+10][w>w_harta?w+10:w_harta+10];
	int rez[h+10][w+10];	// matricea rezultata/simulata dupa a N-a etapa

	for(i=1;i<h_harta+1;i++) 
	{
		for(j=1;j<w_harta+1;j++) 
		{
			fscanf(fd, "%d",&term[i][j]);
		}
	}
	fclose(fd);
	
	for(i=1;i<h+1;i++) 
	{
		for(j=1;j<w+1;j++) 
		{
			m[i][j]=term[i][j];
		}
	}

	if (letter == 'T')			// daca matricea este toroidala
	{
		// Algoritmul propriu-zis incepe de aici:
		// pentru n= numarul de etape:
		for(k=0;k<n;k++) {
		
			makeToroidalMatrix(h,w,m);	// transforma matricea intr-un toroid
		
			// pentru o etapa parcurg toata harta de simulat
			#pragma omp parallel for collapse(2) private(i,j) shared(m,rez,w,h)
			for(i=1;i<h+1;i++)  
			{
				for(j=1;j<w+1;j++) 
				{	 
					if(getNeighborCount(i,j,h,w,m) == 3)	// daca are 3 vecini 
					{
						rez[i][j]=1;	// este creat un nou individ
					}
						else  
						if(getNeighborCount(i,j,h,w,m) == 2 && m[i][j] == 1) 
						{
							rez[i][j]=1;	// daca un individ are 2 vecini
						} 			// el continua sa exista
							else
							{
								rez[i][j]=0;	// altfel devine gol
							}
				}
			}	
		
			// dupa etapa egalez matricea initiala cu matricea rezultata
			#pragma omp parallel for collapse(2) private(i,j) shared(h,w,m,rez)
			for(i=1;i<h+1;i++) 
			{
				for(j=1;j<w+1;j++) 
				{
					m[i][j] = rez[i][j];
				}
			}
			// la urmatoarea iteratie se refac marginile ca sa se pastreze proprietatea de toroid
		}
	}
	else 		// else am presupus ca matricea este in plan
	{
		// pentru n= numarul de etape:
		for(k=0;k<n;k++) {		
				// pentru o etapa parcurg toata harta de simulat
				#pragma omp parallel for collapse(2) private(i,j) shared(m,rez,w,h)
				for(i=1;i<h+1;i++)  
				{
					for(j=1;j<w+1;j++) 
					{	 
						if(getNeighborCount(i,j,h,w,m) == 3)	// daca are 3 vecini 
						{
							rez[i][j]=1;	// este creat un nou individ
						}
							else  
							if(getNeighborCount(i,j,h,w,m) == 2 && m[i][j] == 1) 
							{
								rez[i][j]=1;	// daca un individ are 2 vecini
							} 			// el continua sa exista
								else
								{
									rez[i][j]=0; 	// altfel devine gol
								}
					}
				}	
		
				// dupa etapa egalez matricea initiala cu matricea rezultata
				#pragma omp parallel for collapse(2) private(i,j) shared(m,rez,w,h)
				for(i=1;i<h+1;i++) 
				{
					for(j=1;j<w+1;j++) 
					{
						m[i][j] = rez[i][j];
					}
				}
				// la urmatoarea iteratie se refac marginile ca sa se pastreze proprietatea de toroid
			}
	}	
	
	// retinem coordonatele individului/indivizilor cel/cei mai din dreapta jos
	int max1=0,max2=0;
	for(i=h;i>0;i--) 
	{
		for(j=w;j>0;j--) 
		{
			if (m[i][j] == 1)
			{
			max1=i;	// caut cel mai de jos individ si ii retin linia
			break;
			}
		}		
		if (max1 != 0) 
		{
			break;
		}
	}
	
	for(j=w;j>0;j--)  
	{
		for(i=h;i>0;i--)
		{
			if (m[i][j] == 1)
			{
			max2=j;	// caut cel mai din dreapta individ si ii retin coloana
			break;
			}
		}
		if (max2 != 0) 
		{
			break;
		}
	}
	
	// salvarea hartii in fisierul de output
	fd2 = fopen(argv[4],"w");
	fprintf(fd, "%c %d %d %d %d\n",letter,max2,max1,w,h);
	for(i=1;i<max1+1;i++) 
	{
		for(j=1;j<max2+1;j++) 
		{
			fprintf(fd2,"%d ",m[i][j]);
		}
		fprintf(fd2,"\n");
	}
	fclose(fd2);

	return 0;
}
