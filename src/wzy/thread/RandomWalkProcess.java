package wzy.thread;

import java.util.concurrent.Callable;

import wzy.model.RandomWalkModel;
import wzy.model.TransE;
import wzy.model.randwk.DFSAllPath;
import wzy.model.randwk.RandomExactly;

public class RandomWalkProcess implements Callable{

	public String dir;
	public static int cand_size=500;
	
	public void Processing()
	{
		KBCProcess kbc=new KBCProcess();
		kbc.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		kbc.setEm(new TransE());
		
		kbc.StatisticTrainingSet();
		
		//RandomWalkModel rw=new RandomExactly();
		RandomWalkModel rw=new DFSAllPath();
		rw.train_triplets=kbc.getTrain_triplets();
		rw.validate_triplets=kbc.getValidate_triplets();
		rw.test_triplets=kbc.getTest_triplets();
		rw.entityNum=kbc.getEm().getEntityNum();
		rw.relationNum=kbc.getEm().getRelationNum();
		rw.ReadFormulas(dir+"paths.txt");
		//rw.InitPathWeights();
		rw.InitPathWeightsByDef();
		
		ConstructFormulas cc=new ConstructFormulas();
		cc.setEntityNum(rw.entityNum);
		cc.setRelNum(rw.relationNum);
		cc.setTrain_triplets(rw.train_triplets);
		cc.Init();
		cc.BuildGraph();
		rw.triplet_graph=cc.getTriplet_graph();
		
		
		rw.Training(rw.train_triplets, rw.validate_triplets);
		System.err.println("Training data "+rw.train_true_count+" "+rw.train_false_count+" "
				+(rw.train_true_count/(double)rw.train_false_count));	
		//rw.TestAllCandidates(dir+"cand.txt", cand_size);
		rw.TestCandidatesForRel(dir+"split_candidates", cand_size, "\t");
		System.err.println("Training data "+rw.train_true_count+" "+rw.train_false_count/998+" "
				+(rw.train_true_count/((double)rw.train_false_count)/998));		
		
		rw.PrintPathsWeight(dir+"path.weight");
		rw.PrintTopWeightFormula(dir+"path.weight.top_l1", 10, dir+"relation2id.txt");
	}
	
	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		Processing();
		return null;
	}

	
}
