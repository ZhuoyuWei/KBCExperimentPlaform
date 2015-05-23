package wzy.main;

import wzy.thread.KBCProcess;
import wzy.model.TransE;
import wzy.model.para.TransEParameter;

public class LinkPrediction {

	public static TransEParameter SetTransEParameter(int entitydim,int relationdim)
	{
		TransEParameter ptranse=new TransEParameter();
		ptranse.setEntityDim(entitydim);
		ptranse.setRelationDim(relationdim);
		return ptranse;
	}
	
	public static void main(String[] args)
	{
		String dir="F:\\emnlp2015\\fb15k\\FB15k\\";
		
		//Read Three DB Files
		KBCProcess kbc_raw_tester=new KBCProcess();
		kbc_raw_tester.SetThreeTriplets(dir+"liuzhiyuan.train.txt"
				, dir+"liuzhiyuan.valid.txt"
				, dir+"liuzhiyuan.test.txt"
				, "\t");
		kbc_raw_tester.setEm(new TransE());
		kbc_raw_tester.SetEmbeddingModelSpecificParameter(SetTransEParameter(50,50));
		kbc_raw_tester.Processing();
		
		
	}
}
