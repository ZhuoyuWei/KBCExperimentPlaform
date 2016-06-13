package wzy.main;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wzy.io.FileTools;
import wzy.model.EmbeddingModel;
import wzy.model.RandomWalkModel;
import wzy.model.TransE;
import wzy.model.randwk.DFSAllPath;
import wzy.model.randwk.RandomAttention;
import wzy.model.randwk.RandomExactly;
import wzy.model.randwk.RandomPRA;
import wzy.thread.ConstructFormulas;
import wzy.thread.KBCProcess;
import wzy.thread.RandomWalkProcess;
import wzy.tool.MatrixTool;

public class RandomWalMultiThreadDebug {

	public static int relNum;
	public static int entNum;
	
	/**
	 * Split the train/valid/test set into subsets for each relation
	 * 2016.5.13 by wzy
	 * @param triplets
	 * @return
	 */
	public static int[][][] SplitData(int[][] triplets)
	{
		List<int[]>[] tmpLists=new List[relNum];
		for(int i=0;i<relNum;i++)
			tmpLists[i]=new ArrayList<int[]>();
		for(int i=0;i<triplets.length;i++)
		{
			tmpLists[triplets[i][1]].add(triplets[i]);
		}
		int[][][] res=new int[relNum][][];
		for(int i=0;i<relNum;i++)
			res[i]=tmpLists[i].toArray(new int[0][]);
		return res;
	}
	
	
	public static void main(String[] args)
	{
		System.out.println("Finally");
		//String dir=args[0];
		String dir="C:\\Users\\Administrator\\Documents\\data\\fb15k_doubledirect\\";
		KBCProcess kbc=new KBCProcess();
		kbc.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		kbc.setEm(new TransE());
		kbc.StatisticTrainingSet();
		
		RandomWalkModel.entityNum=kbc.getEm().getEntityNum();
		RandomWalkModel.relationNum=kbc.getEm().getRelationNum();
		entNum=kbc.getEm().getEntityNum();
		relNum=kbc.getEm().getRelationNum();
		
		RandomWalkModel.ReadFormulas(dir+"paths.txt");
		System.out.println("FormulaForest "+RandomWalkModel.ff.length);
		
		int[][][] splited_train_triplets=SplitData(kbc.getTrain_triplets());
		int[][][] splited_valid_triplets=SplitData(kbc.getValidate_triplets());
		int[][][] splited_test_triplets=SplitData(kbc.getTest_triplets());
		
		//int modelindex=Integer.parseInt(args[1]);
		//int emb_dim=Integer.parseInt(args[2]);
		//RandomWalkProcess.cand_size=Integer.parseInt(args[2]);
		int modelindex=3;
		int emb_dim=50;
		kbc.SetEmbeddingModelSpecificParameter(LinkPrediction.SetTransEParameter(emb_dim,emb_dim));
		kbc.getEm().InitEmbeddingFromFile(dir+"emb");
		
		ConstructFormulas cc=new ConstructFormulas();
		cc.setEntityNum(kbc.getEm().getEntityNum());
		cc.setRelNum(kbc.getEm().getRelationNum());
		cc.setTrain_triplets(kbc.getTrain_triplets());
		cc.Init();
		cc.BuildGraph();
		RandomWalkModel.triplet_graph=cc.getTriplet_graph();
		
		//ExecutorService exec = Executors.newFixedThreadPool(36); 
		ExecutorService exec = Executors.newFixedThreadPool(1); 		
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		
		RandomWalkProcess.teststate=1;
		//RandomWalkProcess.cand_size=Integer.parseInt(args[5]);
		RandomWalkProcess.cand_size=500;
		
		//debug, select a suitable sim function
		/*double[][] relEmb=(double[][])(kbc.getEm().ListingEmbedding_public().get(1));
		try {
			PrintStream ps=new PrintStream("rel_sim_log");
			for(int i=0;i<36;i++)
			{
				for(int j=0;j<36;j++)
				{
					double score=MatrixTool.VectorCosSim(relEmb[i],relEmb[j]);
					ps.println(i+"\t"+j+"\t"+score+"\t"+Math.exp(score));
				}
				ps.println("******************************");
			}
			ps.close();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.exit(-1);*/

		EmbeddingModel tmpem=kbc.getEm().CopySeft();
		//for(int i=0;i<relNum;i++)
		for(int i=0;i<1;i++)
		{
			//if(!FileTools.Exists(dir+"split_candidates/1/"+i))
				//continue;
			
			RandomWalkModel rw=null;
			switch(modelindex)
			{
			case 0:{
				rw=new DFSAllPath();
				
				break;
			}
			case 1:{
				rw=new RandomExactly();
				
				break;
			}
			case 2:{
				RandomAttention ra=new RandomAttention();
				//ra.DeepCopyEM(kbc.getEm());
				ra.ShallowCopyEm(tmpem);
				ra.ShallowCopyEmbedding(ra.em_randwalk.ListingEmbedding_public());
				ra.em_randwalk.setGamma(0.001);
				rw=ra;
				
				break;
			}
			case 3:{
				rw=new RandomPRA();
				RandomPRA.StaticGraphForRelations(RandomWalkModel.triplet_graph);
				break;
			}
			}
			
			
			//rw.setEpoch(Integer.parseInt(args[3]));
			//rw.max_round=Integer.parseInt(args[4]);
			rw.setEpoch(100);
			rw.max_round=1;
			
			rw.train_triplets=splited_train_triplets[i];
			rw.validate_triplets=splited_valid_triplets[i];
			rw.test_triplets=splited_test_triplets[i];
			rw.em=kbc.getEm();
			rw.rel_dir=dir+"split_candidates/1/";
			rw.singlerel=i;
			
			alThreads.add(rw);

		}
		
		try {
			//Thread.sleep(10000);
			exec.invokeAll(alThreads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		exec.shutdown();
		
		
		
	}
}
