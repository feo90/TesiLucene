package control;

import java.util.Arrays;

public abstract class StatisticControl 
{

	/**
	 * Questo metodo si occupa di calcolare le statistiche importanti per la ricerca 
	 * @param result l'id delle immagini recuperate in formato String[]
	 * @param query in formato String
	 * @return float[] {precision , recall , precision@10, recall@10, average precision, average precison@10, F1, F1@10}
	 */
	public static float[] findStatistc(String[] result, String query)
	{
		System.out.println("Sono in findStatistc con query: "+query);
		String [] gold_standard=GoldStandardControl.findGoldStandard(query);
		
		if (gold_standard==null)
		{
			return null;
		}
		
		//precision, recall e Average Precision globale 
		float [] pre_rec=findPrecisionRecallAvPre(result, gold_standard);
		
		//Nei primi 10 risultati
		String[] resul_ten=Arrays.copyOfRange(result, 0, 10);
		float [] pre_rec_ten=findPrecisionRecallAvPre(resul_ten, gold_standard);
		
		//F1= (2 x precision x recall) / (precision + recall)
		float f1=(2*pre_rec[0]*pre_rec[1]) / (pre_rec[0]+pre_rec[1]);
		float f1_ten=(2*pre_rec_ten[0]*pre_rec_ten[1]) / (pre_rec_ten[0]+pre_rec_ten[1]);
		
		System.out.println("Precision: "+pre_rec[0]+" Recall: "+pre_rec[1]+" Average Precision: "+pre_rec[2]+" F1: "+f1);
		System.out.println("In top 10, Precision "+pre_rec_ten[0]+" Recall: "+pre_rec_ten[1]+" Average Precision: "+pre_rec_ten[2]+" F1: "+f1_ten);
		
		float[] statistic={pre_rec[0], pre_rec[1],pre_rec_ten[0],pre_rec_ten[1],pre_rec[2],pre_rec_ten[2],f1,f1_ten};		
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
				if (element.equals(gold_standard[j])) 
				{
					relevant_retrieved++;
					float rrf= (float)relevant_retrieved;
					float step= (float) i+1;
					float precision=rrf/step;
					average_precision=average_precision+precision;
					System.out.println("PRINT DI CONTROLLO: Sono al passo "+(i+1)+" l'elemento: "+element+" è rilevante, relevant_retrieved attuale è "+relevant_retrieved+" e la precision attuale è "+precision);
					break;
				}
			}		
		}
		System.out.println("PRINT DI CONTROLLO: relevant_retrieved finale: "+relevant_retrieved);
		
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
