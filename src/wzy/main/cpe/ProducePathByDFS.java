package wzy.main.cpe;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import wzy.io.FileTools;
import wzy.io.busi.ReadTriplets;
import wzy.main.LinkPrediction;
import wzy.meta.GroundPath;
import wzy.model.TransE;
import wzy.model.TransEAndPathModel;
import wzy.thread.ConstructFormulas;
import wzy.thread.KBCProcess;

public class ProducePathByDFS {


	public static void ProduceFalseTriplets()
	{
		
	}

	
	public static void main(String[] args) throws FileNotFoundException
	{
		String dir=args[0];
		
		//Read Three DB Files
		KBCProcess kbc_raw_tester=new KBCProcess();
		
		/*kbc_raw_tester.SetThreeTriplets(dir+"liuzhiyuan.train.txt"
				, dir+"liuzhiyuan.valid.txt"
				, dir+"liuzhiyuan.test.txt"
				, "\t");*/
		
		kbc_raw_tester.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		
		kbc_raw_tester.setEm(new TransE());
		kbc_raw_tester.StatisticTrainingSet();
		
		ConstructFormulas cf=new ConstructFormulas();
		cf.setTrain_triplets(kbc_raw_tester.getTrain_triplets());
		cf.setEntityNum(kbc_raw_tester.getEm().getEntityNum());
		cf.setRelNum(kbc_raw_tester.getEm().getRelationNum());
		cf.Init();
		cf.BuildGraph();
		
		List<GroundPath>[] gpList=cf.DFSMiningPaths();
		FileTools.makeDir(dir+"paths");
		FileTools.PrintPaths(gpList, dir+"paths/");
		

	}
	
	
}
