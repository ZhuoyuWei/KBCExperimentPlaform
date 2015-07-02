package wzy.main;

import wzy.thread.KBCProcess;
import wzy.model.NoTrans;
import wzy.model.NoTransS;
import wzy.model.NoTrans_feature;
import wzy.model.RevTransE;
import wzy.model.TransE;
import wzy.model.TransEAndPathModel;
import wzy.model.TransE_rl;
import wzy.model.TransF;
import wzy.model.TransF_JF;
import wzy.model.TransH;
import wzy.model.TransR;
import wzy.model.TransS;
import wzy.model.TransT;
import wzy.model.para.TransEParameter;
import wzy.model.para.TransFParameter;
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
	
	public static TransFParameter SetTransFParameter(int entitydim,int relationdim)
	{
		TransFParameter ptranse=new TransFParameter();
		ptranse.setEntityDim(entitydim);
		ptranse.setRelationDim(relationdim);
		return ptranse;
	}		
	
	public static void main(String[] args)
	{
		//String dir="F:\\emnlp2015\\fb15k\\FB15k\\";
		String dir=args[0];
		
		//Read Three DB Files
		KBCProcess kbc_raw_tester=new KBCProcess();
		kbc_raw_tester.SetThreeTriplets(dir+"liuzhiyuan.train.txt"
				, dir+"liuzhiyuan.valid.txt"
				, dir+"liuzhiyuan.test.txt"
				, "\t");
		kbc_raw_tester.setEm(new TransEAndPathModel());
		//kbc_raw_tester.setPrint_model_file(dir+"/TransE_l1ball.model");
		kbc_raw_tester.setEmbedding_init_file(dir+"embedding.model");
		kbc_raw_tester.setPath_structure_file(dir+"formulas_length2");
		kbc_raw_tester.SetEmbeddingModelSpecificParameter(SetTransEParameter(50,50));
		kbc_raw_tester.Processing();
		
		
	}
}
