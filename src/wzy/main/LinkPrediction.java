package wzy.main;

import wzy.io.FileTools;
import wzy.thread.KBCProcess;
import wzy.model.*;

public class LinkPrediction {

	
	
	
	
	
	public static void main(String[] args)
	{
		String dir="F:\\emnlp2015\\fb15k\\FB15k\\";
		
		//Read Three DB Files
		KBCProcess kbc_raw_tester=new KBCProcess();
		kbc_raw_tester.SetThreeTriplets(dir+"liuzhiyuan.train.txt"
				, dir+"liuzhiyuan.valid.txt"
				, dir+"liuzhiyuan.test.txt"
				, "\t");
		kbc_raw_tester.setEm(new EmbeddingModel());

		
		
	}
}
