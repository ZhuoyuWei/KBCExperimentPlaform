package wzy.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wzy.io.FileTools;
import wzy.model.TransE;
import wzy.model.TransF;
import wzy.model.TransH;
import wzy.model.TransR;
import wzy.thread.KBCProcess;

public class AdjustParameter {
	

	
	public static void main(String[] args)
	{
		String dir=args[0];
		FileTools.makeDir(dir+"/adjust_parameter");
		
		//Read Three DB Files
		KBCProcess readdataProcess=new KBCProcess();
		readdataProcess.SetThreeTriplets(dir+"liuzhiyuan.train.txt"
				, dir+"liuzhiyuan.valid.txt"
				, dir+"liuzhiyuan.test.txt"
				, "\t");
		
		double[] gammas={1,1e-1,1e-2,1e-3,1e-4,1e-5,1e-6};
		
		
		ExecutorService exec = Executors.newFixedThreadPool(7); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		
			
		String initembeddingfile=args[2];

		
		
		for(int i=0;i<gammas.length;i++)
		{
			String kbc_output_dir=dir+"/adjust_parameter/gammas"+gammas[i];
			FileTools.makeDir(kbc_output_dir);
			KBCProcess kbc_tester=new KBCProcess();
			kbc_tester.setEmbedding_init_file(initembeddingfile);
			kbc_tester.CopyThreeDataSets(readdataProcess);
			kbc_tester.setEm(new TransR());
			kbc_tester.SetEmbeddingModelSpecificParameter(LinkPrediction.SetTransRParameter(50,50));
			kbc_tester.SetGamma(gammas[i]);
			kbc_tester.setPrint_log_file(kbc_output_dir+"/result.log");
			kbc_tester.setPrint_model_file(kbc_output_dir+"/embedding.model");
			kbc_tester.setPrintMiddleModel_dir(kbc_output_dir+"/middleModel");
			//kbc_tester.Processing();
			alThreads.add(kbc_tester);
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
