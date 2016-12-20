package control;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import entities.Constants;

public abstract class WordMatrixControl 
{
	private static String MATRIX_FILE=Constants.getMATRIX_FILE();

	
	/**
	 * Questo metodo prende in input l'indirizzo di un file e restituisce una linked list che ne contiene le singole righe
	 * @param file il filename 
	 * @return LinkedList<String>
	 * @throws IOException 
	 */
	private static LinkedList<String> parseLn(String file) throws IOException
    {
		//System.out.println("Sono in parseLn del file: "+file);
		LinkedList<String> queries=new LinkedList<>();
        
        BufferedReader br = new BufferedReader(new FileReader(file));
	    try 
	    {
	        String line = br.readLine();

	        while (line != null) 
	        {
	        	queries.add(line);    	
	            line = br.readLine();
	        }
	    } 
	    finally 
	    {
	        br.close();
	    }
        return queries;
    }
	
	/**
	 * Questo metodo si occupa di costruire la words matrix partendo da un file salvato i memoria
	 * @return String[][] nel formato matrice[vettori][elementi] in posizione [i][0] vi sono le varie parole
	 * @throws IOException
	 */
	public static String[][] matrixMaker() throws IOException
	{
		System.out.println("Sono in matrixMaker del file: "+MATRIX_FILE);
		
		LinkedList<String> data=parseLn(MATRIX_FILE);
		
		//Nella prima linea vi sono il numero di parole e la lunghezza del vettore
		String first_line=data.get(0);
		String[] fl=first_line.split(" ");
		int numb=Integer.parseInt(fl[0]);
		int length=Integer.parseInt(fl[1]);
		
		System.out.println("Vi sono: "+numb+" parole e la lunghezza dei vettori è "+length);
		
		String[][] matrix=new String[numb][length+1];//il primo elemento del vettore è la parola 
		
		int i;
		for (i=1;i<numb+1;i++)
		{
			//Recupero i dati
			String line=data.get(i);
			String [] line_part=line.split("::");
			String word=line_part[0];
			String [] line_part_bis=line_part[1].split("    ");
			String type=line_part_bis[0];
			//String one=line_part_bis[1];
			String vector=line_part_bis[2].split(" ")[1];
			System.out.println("Ho trovato la parola: "+word+" di tipo "+type+" il cui vettore è "+vector);
	
			String[] vector_array=vector.split(",");
			
			//Inserisco i dati nella matrice
			matrix[i][0]=word;
			int j;
			for (j=0;j<length;j++)
			{
				matrix[i][j+1]=vector_array[j];
			}
		}
		return matrix;
	}
	
	/**
	 * Questo metodo si occupa di creare il vettore di una frase secondo la formula (1/Num(Vect))Sum(Vect)
	 * @param phrase la frase di cui voglio creare il vettore
	 * @param matrix la words matrix di riferimento
	 * @return int[] il vettore
	 */
	public static int[] calculatePhraseVector(String phrase, String[][] matrix)
	{
		System.out.println("Sono in calculatePhraseVector con la frase "+phrase);
		
		LinkedList<int[]> wordVectors=new LinkedList<>();
		
		String[] words=phrase.split(", |; |. |: |! |? | \"|\" |'| "); //recupero le singole parole
		
		//Creo i vettori delle singole parole
		int i;
		for (i=0;i<words.length;i++)
		{
			if (words[i].length()==1) //Riconosco gli acronimi es. t.v.
			{
				LinkedList<String> acronyms_sequence=FindAcronyms(words,i);
				if (acronyms_sequence.size()>1)
				{
					int j;
					String word="";
					for (j=0;j<acronyms_sequence.size();j++)
					{
						word=word+acronyms_sequence.get(j);
					}
					i=i+acronyms_sequence.size()-1; //aggiorno l'indice
					
					//Cerco la parola sulla matrice e creo il vettore
					for (j=0;j<matrix.length;j++)
					{
						if (word.equals(matrix[j][0]))
						{
							System.out.println("è presente nella matrice");
							int[] vector=new int[matrix[j].length-1];
							int k;
							for (k=0;k<matrix[j].length;k++)
							{
								vector[k]=Integer.parseInt(matrix[j][k+1]);
							}
							wordVectors.add(vector);
							break;
						}
					}
				}
			}
			else if (words[i].length()>1) //Verifico che l'elemento non sia trascurabile
			{
				System.out.println("Al passo "+i+" esamino la parola "+words[i]);
				
				//Cerco la parola sulla matrice e creo il vettore
				int j;
				for (j=0;j<matrix.length;j++)
				{
					if (words[i].equals(matrix[j][0]))
					{
						System.out.println("è presente nella matrice");
						int[] vector=new int[matrix[j].length-1];
						int k;
						for (k=0;k<matrix[j].length;k++)
						{
							vector[k]=Integer.parseInt(matrix[j][k+1]);
						}
						wordVectors.add(vector);
						break;
					}
				}
			}
		}
		
		//Creo il vettore della frase
		int[] phrase_vector=new int[matrix[0].length-1];
		
		//Gestisco una coordinata alla volta
		for (i=0;i<phrase_vector.length;i++)
		{
			//Sommo i valori di tutti i vettori delle parole
			int element=0;
			int j;
			for (j=0;j<wordVectors.size();j++)
			{
				element=element+wordVectors.get(j)[i];
			}
			
			//Divido per il numero dei vettori e lo inserisco
			element=element/wordVectors.size();
			phrase_vector[i]=element;
			System.out.println("Per la coordinata "+i+" il valore è "+element);
		}
		return phrase_vector;
	}
	
	/**
	 * Questo metodo si occupa di riconoscere gli acronimi come t.v. 
	 * @param words la matrice delle parole
	 * @param i il puntatore all'elemento formato da una sola lettera
	 * @return una linked list di tutte le lettere che formano l'acronimo
	 */
	private static LinkedList<String> FindAcronyms(String[] words, int i) 
	{
		LinkedList<String> Acronym=new LinkedList<>();
	
		String first=words[i]; //l'elemento che ha fatto scattare il metodo
		Acronym.add(first);
		
		if (words[i+1].length()>1)
		{
			//nulla
		}
		else if ( (words[i].equals("t") && words[i+1].equals("v")) || (words[i].equals("T") && words[i+1].equals("V")) )
		{
			Acronym.add("television");
		}
		
		return Acronym;
	}

	/**
	 * Questo metodo calcola la distanza tra i due vettori
	 * @param vect_one
	 * @param vect_two
	 * @return int 
	 */
	public static int calculateDistance(int[] vect_one, int [] vect_two)
	{
		if (vect_one.length!=vect_two.length)
		{
			System.out.println("ERROR: the lenght of the two vector ISN'T the same!");
			return 0;
		}
		
		//Eseguo Sum(V1[i]xV2[i])
		int sum_of_mult=0; 
		int i;
		for (i=0;i<vect_one.length;i++)
		{
			int mult=vect_one[i]*vect_two[i];
			sum_of_mult=sum_of_mult+mult;
		}
		
		//Eseguo Sum(V1[i]^2) e Sum(V2[i]^2)
		int sum_square_one=0;
		int sum_square_two=0;
		for (i=0;i<vect_one.length;i++)
		{
			int square_one=vect_one[i]*vect_one[i];
			int square_two=vect_two[i]*vect_two[i];
			sum_square_one=sum_square_one+square_one;
			sum_square_two=sum_square_two+square_two;
		}
		
		//Eseguo Rad(Square1 x Square2)
		int denon=(int) Math.sqrt(sum_square_one*sum_square_two);
		
		//Eseguo la divisione finale e se viene un numero negativo restituisco 0
		int rel=sum_of_mult/denon;
		if (rel>0)
		{
			return rel;
		}
		else 
		{
			return 0;
		}
	}
	
	/**
	 * Questo metodo converte un array di interi in un'unica stringa in cui i valori sono separati da virgole
	 * @param array int[]
	 * @return String
	 */
	public static String createStringVector(int[] array)
	{
		String vector="";
		int i;
		for (i=0;i<array.length;i++)
		{
			String element=Integer.toString(array[i]);
			vector=vector+element+",";
		}
		//elimino l'ultima virgola
		vector=vector.substring(0, vector.length()-1);
		return vector;
	}
}

