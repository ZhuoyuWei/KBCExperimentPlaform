package wzy.main;

import wzy.io.FileTools;
import wzy.thread.KBCProcess;

public class LinkPrediction {

	
	
	
	
	
	public static void main(String[] args)
	{
		String dir="F:\\emnlp2015\\fb15k\\FB15k\\";
		
		//Read Three DB Files
		KBCProcess kbc_raw_tester=new KBCProcess();
		KBCProcess kbc_filter_tester=new KBCProcess();
		kbc_raw_tester.SetThreeTriplets(dir+"liuzhiyuan.train.txt"
				, dir+"liuzhiyuan.valid.txt"
				, dir+"liuzhiyuan.test.txt"
				, "\t");
		kbc_filter_tester.CopyThreeDataSets(kbc_raw_tester);
		kbc_filter_tester.FilterDataSet();
		//debug print filter
		FileTools.PrintIntegralTriplets(kbc_filter_tester.getTrain_triplets(),dir+"liuzhiyuan.train.txt.filter", "\t");
		FileTools.PrintIntegralTriplets(kbc_filter_tester.getValidate_triplets(),dir+"liuzhiyuan.validate.txt.filter", "\t");		
		FileTools.PrintIntegralTriplets(kbc_filter_tester.getTest_triplets(),dir+"liuzhiyuan.test.txt.filter", "\t");		
		
	}
}
