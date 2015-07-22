package wzy.main;

import wzy.io.busi.ReadTriplets;
import wzy.model.TransEAndPathModel;
import wzy.model.para.TransEParameter;
import wzy.thread.ConstructFormulas;
import wzy.thread.KBCProcess;
import wzy.thread.PathEmbeddingTester;

public class TestingPath {

	
	public static TransEParameter SetTransEParameter(int entitydim,int relationdim)
	{
		TransEParameter ptranse=new TransEParameter();
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
		kbc_raw_tester.Processing_PathTesting();
		
		
		ConstructFormulas cf=new ConstructFormulas();
		cf.setTrain_triplets(kbc_raw_tester.getTrain_triplets());
		int[] entityAndRelationSizes=ReadTriplets.StaticTrainSet(cf.getTrain_triplets());
		cf.setEntityNum(entityAndRelationSizes[0]);
		cf.setRelNum(entityAndRelationSizes[1]);
		cf.Init();
		cf.SplitRelationTrainingData();
		cf.BuildGraph();
		
		
		
		PathEmbeddingTester pt=new PathEmbeddingTester();
		pt.setCf(cf);
		pt.setEm(kbc_raw_tester.getEm());
		pt.setPathtester_log_file(dir+"tester_path.log");
		
		pt.Init();
		pt.Testing(kbc_raw_tester.getTest_triplets());
	}
	
}
