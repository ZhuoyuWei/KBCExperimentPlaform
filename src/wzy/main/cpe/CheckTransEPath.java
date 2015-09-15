package wzy.main.cpe;

import java.io.IOException;
import java.util.List;

import wzy.io.FileTools;
import wzy.meta.GroundPath;
import wzy.model.TransE;
import wzy.model.TransEAndPathModel;
import wzy.model.para.TransEParameter;
import wzy.model.para.TransFParameter;
import wzy.model.para.TransHParameter;
import wzy.model.para.TransRParameter;
import wzy.thread.KBCProcess;

public class CheckTransEPath {
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
	

	
	
	public static void main(String[] args) throws IOException
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
		
		TransE ptrans=new TransE();
		kbc_raw_tester.setEm(ptrans);
		kbc_raw_tester.SetEmbeddingModelSpecificParameter(SetTransEParameter(50,50));

		kbc_raw_tester.StatisticTrainingSet();
		kbc_raw_tester.getEm().InitEmbeddingFromFile(dir+args[1]);
		
		List<GroundPath>[] pathList=FileTools.ReadGroundPath(dir+"paths/");
		
		for(int i=1;i<pathList.length;i++)
		{
			long start=System.currentTimeMillis();
			double score=ptrans.CheckPaths100(pathList[i], Integer.parseInt(args[2]));
			long end=System.currentTimeMillis();
			System.out.println(i+"\t"+score+"\t"+(end-start));
		}
		
	}
}
