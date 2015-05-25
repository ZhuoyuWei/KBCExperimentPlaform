package wzy.main;

import wzy.thread.KBCProcess;
import wzy.model.TransE;
import wzy.model.TransH;
import wzy.model.TransR;
import wzy.model.para.TransEParameter;
import wzy.model.para.TransHParameter;
import wzy.model.para.TransRParameter;

public class LinkPrediction {

	public static TransEParameter SetTransEParameter(int entitydim,int relationdim)
	{
		TransEParameter ptranse=new TransEParameter();
		ptranse.setEntityDim(entitydim);
		ptranse.setRelationDim(relationdim);
		return ptranse;
	}
	
	public static TransHParameter SetTransHParameter(int entitydim,int relationdim)
	{
		TransHParameter ptranse=new TransHParameter();
		ptranse.setEntityDim(entitydim);
		ptranse.setRelationDim(relationdim);
		return ptranse;
	}	
	public static TransRParameter SetTransRParameter(int entitydim,int relationdim)
	{
		TransRParameter ptranse=new TransRParameter();
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
		kbc_raw_tester.setEm(new TransR());
		kbc_raw_tester.SetEmbeddingModelSpecificParameter(SetTransRParameter(50,50));
		kbc_raw_tester.Processing();
		
		
	}
}
