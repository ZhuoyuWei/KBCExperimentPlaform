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

public class TransEProduceCandidate {

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
		//String dir="F:\\Workspace\\KBCworkspace\\dataset\\" +
			//	"wordnet-mlj12\\wordnet-mlj12\\";		
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
		//kbc_raw_tester.setPrint_model_file(dir+args[1]);
		kbc_raw_tester.setEmbedding_init_file(dir+args[1]);
		//kbc_raw_tester.setPath_structure_file(dir+args[2]);
		kbc_raw_tester.SetEmbeddingModelSpecificParameter(SetTransEParameter(50,50));
		
		
		kbc_raw_tester.getEm().SetBestParameter();
		kbc_raw_tester.getEm().setEpoch(0);
		kbc_raw_tester.getEm().setTrainprintable(false);
		
		kbc_raw_tester.Processing();
		
		kbc_raw_tester.getEm().ProduceCandidateForRandomWalk(dir+args[2]
				, kbc_raw_tester.getTest_triplets(),500);
		
		
	}
}
