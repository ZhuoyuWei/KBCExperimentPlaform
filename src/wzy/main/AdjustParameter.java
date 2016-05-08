package wzy.main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wzy.io.FileTools;
import wzy.model.NoTrans;
import wzy.model.NoTransS;
import wzy.model.TransE;
import wzy.model.TransEAndPathModel;
import wzy.model.TransF;
import wzy.model.TransF_JF;
import wzy.model.TransH;
import wzy.model.TransR;
import wzy.model.TransS;
import wzy.thread.KBCProcess;

public class AdjustParameter {
	

	
	public static void main(String[] args)
	{
		String dir=args[0];
		//String dir="F:\\emnlp2015\\fb15k\\FB15k\\";
		
		FileTools.makeDir(dir+"adjust_parameter");
		
		//Read Three DB Files
		KBCProcess readdataProcess=new KBCProcess();
		readdataProcess.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		double[] L2norms={0.1,10};
		double[] gammas={1e-2,1e-3,1e-4,1e-5};
		double[] margins={1,0.5,0.125};
		//int[]  minibanchs={1440,2400,4800,9600};
		int[]  minibanchs={4800};
		
		ExecutorService exec = Executors.newFixedThreadPool(24); 
		List<Callable<Integer>> alThreads=new ArrayList<Callable<Integer>>();
		
			
		//String initembeddingfile=args[2];

		
		
		for(int i=0;i<minibanchs.length;i++)
		{
			for(int j=0;j<margins.length;j++)
			{
				for(int k=0;k<gammas.length;k++)
				{
					for(int h=0;h<L2norms.length;h++)
					{
						String kbc_output_dir=dir+"/adjust_parameter/"+minibanchs[i]+"_"+margins[j]+"_"+gammas[k]+"_"+L2norms[h];
						FileTools.makeDir(kbc_output_dir);
						KBCProcess kbc_tester=new KBCProcess();
						kbc_tester.setEmbedding_init_file(dir+args[1]);
						kbc_tester.setPath_structure_file(dir+args[2]);
						kbc_tester.CopyThreeDataSets(readdataProcess);
						kbc_tester.setEm(new TransEAndPathModel());
						kbc_tester.SetEmbeddingModelSpecificParameter(LinkPrediction.SetTransEParameter(50,50));
					
						//kbc_tester
						kbc_tester.SetMiniBranch(minibanchs[i]);
						kbc_tester.SetMargin(margins[j]);
						kbc_tester.SetGamma(gammas[k]);
						kbc_tester.getEm().setLammadaL2(L2norms[h]);
						kbc_tester.setQuiet(true);
					
						kbc_tester.setPrint_log_file(kbc_output_dir+"/result.log");
						//kbc_tester.setPrint_model_file(kbc_output_dir+"/embedding.model");
						//kbc_tester.setPrintMiddleModel_dir(kbc_output_dir+"/middleModel");
						alThreads.add(kbc_tester);
					}
				}
			}
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
