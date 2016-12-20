package control;

import java.util.Arrays;

public abstract class StatisticControl 
{

	/**
	 * Questo metodo si occupa di calcolare le statistiche importanti per la ricerca 
	 * @param result l'id delle immagini recuperate in formato String[]
	 * @param query in formato String
	 * @param alsoPart boolean, indica se si vogliono considerare anche i risultati parzialmente rilevanti 
	 * @return float[] {precision , recall , precision@10, recall@10, average precision, average precison@10, F1, F1@10}
	 */
	public static float[] findStatistc(String[] result, String query, boolean alsoPart)
	{
		//System.out.println("Sono in findStatistc con query: "+query);
		String [] gold_standard=GoldStandardControl.findGoldStandard(query, alsoPart);
		
		if (gold_standard==null)
		{
			return null;
		}
		
		//precision, recall e Average Precision globale 
		float [] pre_rec=findPrecisionRecallAvPre(result, gold_standard);
		
		int size=result.length;
		//Nei primi 10 risultati
		String[] resul_ten;
		if (size>10)
		{
			resul_ten=Arrays.copyOfRange(result, 0, 10);
		}
		else
		{
			resul_ten=Arrays.copyOfRange(result, 0, size);
		}
		
		float [] pre_rec_ten=findPrecisionRecallAvPre(resul_ten, gold_standard);
		
		//F1= (2 x precision x recall) / (precision + recall)
		float f1=(2*pre_rec[0]*pre_rec[1]) / (pre_rec[0]+pre_rec[1]);
		float f1_ten=(2*pre_rec_ten[0]*pre_rec_ten[1]) / (pre_rec_ten[0]+pre_rec_ten[1]);
		
		
		//Nei primi 5 risultati 
		String[] resul_five;
		if (size>5)
		{
			resul_five=Arrays.copyOfRange(result, 0, 5);
		}
		else
		{
			resul_five=Arrays.copyOfRange(result, 0, size);
		}
		float [] pre_rec_five=findPrecisionRecallAvPre(resul_five, gold_standard);
		float f1_five=(2*pre_rec_five[0]*pre_rec_five[1]) / (pre_rec_five[0]+pre_rec_five[1]);
		
		//Nei primi 20 risultati 
		String[] resul_20;
		if (size>20)
		{
			resul_20=Arrays.copyOfRange(result, 0, 20);
		}
		else
		{
			resul_20=Arrays.copyOfRange(result, 0, size);
		}
		float [] pre_rec_20=findPrecisionRecallAvPre(resul_20, gold_standard);
		float f1_20=(2*pre_rec_20[0]*pre_rec_20[1]) / (pre_rec_20[0]+pre_rec_20[1]);
		
		//Nei primi 30 risultati 
		String[] resul_30;
		if (size>30)
		{
			resul_30=Arrays.copyOfRange(result, 0, 30);
		}
		else
		{
			resul_30=Arrays.copyOfRange(result, 0, size);
		}
		float [] pre_rec_30=findPrecisionRecallAvPre(resul_30, gold_standard);
		float f1_30=(2*pre_rec_30[0]*pre_rec_30[1]) / (pre_rec_30[0]+pre_rec_30[1]);
				
		//Nei primi 40 risultati 
		String[] resul_40;
		if (size>40)
		{
			resul_40=Arrays.copyOfRange(result, 0, 40);
		}
		else
		{
			resul_40=Arrays.copyOfRange(result, 0, size);
		}
		float [] pre_rec_40=findPrecisionRecallAvPre(resul_40, gold_standard);
		float f1_40=(2*pre_rec_40[0]*pre_rec_40[1]) / (pre_rec_40[0]+pre_rec_40[1]);
		
		
		float[] statistic={pre_rec[0], pre_rec[1],pre_rec_ten[0],pre_rec_ten[1],pre_rec[2],pre_rec_ten[2],f1,f1_ten, pre_rec_five[0], pre_rec_five[1],pre_rec_five[2],f1_five,pre_rec_20[0], pre_rec_20[1],pre_rec_20[2],f1_20,pre_rec_30[0], pre_rec_30[1],pre_rec_30[2],f1_30,pre_rec_40[0], pre_rec_40[1],pre_rec_40[2],f1_40};		
		return statistic;
	}
	
	/**
	 * Questo metodo si occupa di calcolare la precisione ed il recall di un risultato dato il gold standard
	 * @param result insieme dei risultati in formato String[]
	 * @param gold_standard il gs in formato String[]
	 * @return float[] {precision, recall, average precision}
	 */
	private static float[] findPrecisionRecallAvPre(String[] result, String [] gold_standard)
	{
		System.out.println("PRINT DI CONTROLLO: Sono in findPrecisionRecallAvPre con result size= "+result.length);
		//Scorro tutta la lista dei risultati
		int relevant_retrieved=0;
		float average_precision=0;
		int i;
		for (i=0;i<result.length;i++)
		{
			String element=result[i];
			//verifico se l'elemento esaminato è un risultato rilevante
			int j;
			for (j=0;j<gold_standard.length;j++)
			{
				if (element==null)
				{
					//System.out.println("PRINT DI CONTROLLO: Sono al passo "+(i+1)+" con l'elemento: "+element);
				}
				
				if (element.equals(gold_standard[j])) 
				{
					relevant_retrieved++;
					float rrf= (float)relevant_retrieved;
					float step= (float) i+1;
					float precision=rrf/step;
					average_precision=average_precision+precision;
					//System.out.println("PRINT DI CONTROLLO: Sono al passo "+(i+1)+" l'elemento: "+element+" è rilevante, relevant_retrieved attuale è "+relevant_retrieved+" e la precision attuale è "+precision);
					break;
				}
			}		
		}
		//System.out.println("PRINT DI CONTROLLO: relevant_retrieved finale: "+relevant_retrieved);
		
		//average_precision=Sum (precision)/total number of relevant doc
		average_precision=average_precision/gold_standard.length;
		
		//recall=relevant doc retrieved / total number of relevant doc
		float rel_ret= (float) relevant_retrieved;
		float total_rel=(float) gold_standard.length;
		float recall= rel_ret/total_rel;
		
		//precision=relevant doc retrieved / total number of doc retrieved
		float total_ret=(float) result.length;
		float precision= rel_ret/total_ret;
		
		float[] pre_rec_ap={precision,recall,average_precision};
		return pre_rec_ap;
	}
}
