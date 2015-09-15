package wzy.main.cpe;

import java.io.FileNotFoundException;
import java.util.List;

import wzy.io.FileTools;
import wzy.meta.GroundPath;
import wzy.model.TransE;
import wzy.thread.ConstructFormulas;
import wzy.thread.KBCProcess;

public class RunTimeForSearch {

	
	
	public static void main(String[] args) throws FileNotFoundException
	{
		System.out.println(Math.log(0.3));
		System.out.println(Math.log(0.4));	
		System.out.println(Math.log(0.5));
		System.out.println(Math.log(0.6));
		System.out.println(Math.log(0.7));		
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
		
		for(int i=1;i<=10;i++)
		{
			gpList[i]=gpList[i].subList(0, 500);
		}
		
		cf.DFSforCPE(gpList);
		
		FileTools.makeDir(dir+"paths");
		FileTools.PrintPaths(gpList, dir+"paths/");
	}
}
