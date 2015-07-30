package wzy.main;

import wzy.model.EmbeddingModel;
import wzy.thread.AnalyzeNetwork;
import wzy.thread.ConstructFormulas;
import wzy.thread.KBCProcess;


/**
 * We employ this class to analyze the structures of networks obtained from different data sets, i.e., FB15K and wn18.
 * 1. We want to know whether there are existing super large node in the network who have a large out-degree,
 *  and go through it we under one specific relation type we can get to a mass of nodes. These nodes may be a significant
 *  impact on performance of link prediction.
 * @author Zhuoyu Wei
 *
 */
public class StatisticNetworkStructure {

	
	
	
	
	public static void main(String[] args)
	{
		//String dir=args[0];
		//String dir="F:\\Workspace\\KBCworkspace\\dataset\\wordnet-mlj12\\wordnet-mlj12\\";
		String dir="F:\\emnlp2015\\fb15k\\FB15k\\";
		
		AnalyzeNetwork an=new AnalyzeNetwork();
		ConstructFormulas cf=new ConstructFormulas();
		KBCProcess kbc=new KBCProcess();
		kbc.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		kbc.setEm(new EmbeddingModel());
		kbc.StatisticTrainingSet();
		cf.setTrain_triplets(kbc.getTrain_triplets());
		cf.setEntityNum(kbc.getEm().getEntityNum());
		cf.setRelNum(kbc.getEm().getRelationNum());
		cf.Init();

		System.out.println(cf.getTrain_triplets().length);
		
		cf.BuildGraph();
		an.setCf(cf);
		an.Processing();
		an.PrintRes_EntityOrderByTripeltNum(dir+"analyze/entity_count", 50000);
		
	}
	
}
