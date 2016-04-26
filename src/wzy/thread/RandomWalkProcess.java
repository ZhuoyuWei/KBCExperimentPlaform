package wzy.thread;

import java.util.concurrent.Callable;

import wzy.model.RandomExactly;
import wzy.model.RandomWalkModel;
import wzy.model.TransE;

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
		
		RandomWalkModel rw=new RandomExactly();
		rw.train_triplets=kbc.getTrain_triplets();
		rw.validate_triplets=kbc.getValidate_triplets();
		rw.test_triplets=kbc.getTest_triplets();
		rw.entityNum=kbc.getEm().getEntityNum();
		rw.relationNum=kbc.getEm().getRelationNum();
		rw.ReadFormulas(dir+"paths.txt");
		rw.InitPathWeights();
		
		ConstructFormulas cc=new ConstructFormulas();
		cc.setEntityNum(rw.entityNum);
		cc.setRelNum(rw.relationNum);
		cc.setTrain_triplets(rw.train_triplets);
		cc.Init();
		cc.BuildGraph();
		rw.triplet_graph=cc.getTriplet_graph();
		
		
		rw.Training(rw.train_triplets, rw.validate_triplets);
		rw.TestAllCandidates(dir+"cand.txt", cand_size);
		
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
