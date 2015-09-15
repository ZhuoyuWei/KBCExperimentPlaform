package wzy.thread;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import wzy.io.FileTools;
import wzy.io.busi.ReadTriplets;
import wzy.model.*;
import wzy.model.para.SpecificParameter;

public class KBCProcess implements Callable{


	private String print_model_file=null;
	private String print_log_file=null;
	private String printMiddleModel_dir=null;
	private String embedding_init_file=null;
	private String path_structure_file=null;
	
	private int[][] train_triplets;
	private int[][] validate_triplets;
	private int[][] test_triplets;
	private EmbeddingModel em;
	
	private boolean quiet=false;
	
	
	public void Processing()
	{
		if(em==null)
		{
			System.err.println("There is no available embedding model, and please set it in main().");
			System.exit(-1);
		}
		StatisticTrainingSet();
		if(print_log_file!=null)
		{
			//FileTools.ReDirectOutputStreamToFile(print_log_file);
			em.setPrint_log_file(print_log_file);
		}
		if(printMiddleModel_dir!=null)
		{
			em.setPrintMiddleModel_dir(printMiddleModel_dir);
			FileTools.makeDir(printMiddleModel_dir);
		}
		//em.SetBestParameter();
		
		if(this.path_structure_file!=null)
			em.InitPathFromFile(path_structure_file);
		if(this.embedding_init_file!=null)
			em.InitEmbeddingFromFile(embedding_init_file);
		else
			em.InitEmbeddingsRandomly(train_triplets);

		em.CountEntityForRelation(train_triplets);
		em.BuildTrainAndValidTripletSet(train_triplets, validate_triplets);
		
		if(!quiet)
		{
			System.err.println("Train process is starting.");
		}
		em.Training(train_triplets, validate_triplets);
		if(print_model_file!=null)
			em.PrintModel(print_model_file);
		
		em.Testing(test_triplets);
	}
	
	
	
	//just for debug
	public void Processing_PathTesting()
	{
		if(em==null)
		{
			System.err.println("There is no available embedding model, and please set it in main().");
			System.exit(-1);
		}
		StatisticTrainingSet();
		if(print_log_file!=null)
		{
			//FileTools.ReDirectOutputStreamToFile(print_log_file);
			em.setPrint_log_file(print_log_file);
		}
		if(printMiddleModel_dir!=null)
		{
			em.setPrintMiddleModel_dir(printMiddleModel_dir);
			FileTools.makeDir(printMiddleModel_dir);
		}
		//em.SetBestParameter();
		
		if(this.path_structure_file!=null)
			em.InitPathFromFile(path_structure_file);
		if(this.embedding_init_file!=null)
			em.InitEmbeddingFromFile(embedding_init_file);
		else
			em.InitEmbeddingsRandomly(train_triplets);

		em.CountEntityForRelation(train_triplets);
		em.BuildTrainAndValidTripletSet(train_triplets, validate_triplets);
	}	
	
	
	public void Processing_produceForMLN()
	{
		if(em==null)
		{
			System.err.println("There is no available embedding model, and please set it in main().");
			System.exit(-1);
		}
		StatisticTrainingSet();
		if(print_log_file!=null)
		{
			//FileTools.ReDirectOutputStreamToFile(print_log_file);
			em.setPrint_log_file(print_log_file);
		}
		if(printMiddleModel_dir!=null)
		{
			em.setPrintMiddleModel_dir(printMiddleModel_dir);
			FileTools.makeDir(printMiddleModel_dir);
		}
		//em.SetBestParameter();
		
		if(this.path_structure_file!=null)
			em.InitPathFromFile(path_structure_file);
		if(this.embedding_init_file!=null)
			em.InitEmbeddingFromFile(embedding_init_file);
		else
			em.InitEmbeddingsRandomly(train_triplets);

		em.CountEntityForRelation(train_triplets);
		em.BuildTrainAndValidTripletSet(train_triplets, validate_triplets);
		
		if(!quiet)
		{
			System.err.println("Train process is starting.");
		}
		em.Training(train_triplets, validate_triplets);
		if(print_model_file!=null)
			em.PrintModel(print_model_file);
		
		em.ProduceCandidateForMLN(test_triplets);
	}
	
	
	
	/**
	 * Read train, validate, test data sets from three files.
	 * @param trainfile
	 * @param validfile
	 * @param testfile
	 * @param separator
	 */
	public void SetThreeTriplets(String trainfile,String validfile,String testfile,String separator)
	{
		train_triplets=ReadTriplets.ReadTripletsFromFile(trainfile,separator);
		validate_triplets=ReadTriplets.ReadTripletsFromFile(validfile,separator);
		test_triplets=ReadTriplets.ReadTripletsFromFile(testfile,separator);
	}	
	
	/**
	 * Get three data set from the existing data set.
	 * @param kbc
	 */
	public void CopyThreeDataSets(KBCProcess kbc)
	{
		train_triplets=ReadTriplets.ReadTripletsFromTriplets(kbc.getTrain_triplets());
		validate_triplets=ReadTriplets.ReadTripletsFromTriplets(kbc.getValidate_triplets());		
		test_triplets=ReadTriplets.ReadTripletsFromTriplets(kbc.getTest_triplets());		
	}
	
	/**
	 * @deprecated
	 */
	public void FilterDataSet()
	{
		class ThreeInt
		{
			int a;
			int b;
			int c;
			public int hashCode()
			{
				return (a*14951+c)*1345+b;
			}
			public boolean equals(Object o)
			{
				ThreeInt t=(ThreeInt)o;
				return (a==t.a&&b==t.b&&c==t.c);
			}
		};
		Set<ThreeInt> testSet=new HashSet<ThreeInt>();
		for(int i=0;i<test_triplets.length;i++)
		{
			ThreeInt t1=new ThreeInt();
			t1.a=test_triplets[i][0];
			t1.b=test_triplets[i][1];
			t1.c=-1;
		
			ThreeInt t2=new ThreeInt();
			t2.a=-1;
			t2.b=test_triplets[i][1];
			t2.c=test_triplets[i][2];
			
			testSet.add(t1);
			testSet.add(t2);
		}
		
		
		
		Set<ThreeInt> testfilter=new HashSet<ThreeInt>();
		
		int[][][] ptr=new int[2][][];
		ptr[0]=train_triplets;
		ptr[1]=validate_triplets;
		List<int[]>[] filted=new List[3];
		
		for(int k=0;k<2;k++)
		{
			filted[k]=new ArrayList<int[]>();
			for(int i=0;i<ptr[k].length;i++)
			{
				ThreeInt t1=new ThreeInt();
				t1.a=ptr[k][i][0];
				t1.b=ptr[k][i][1];
				t1.c=-1;
			
				ThreeInt t2=new ThreeInt();
				t2.a=-1;
				t2.b=ptr[k][i][1];
				t2.c=ptr[k][i][2];
			
				if(testSet.contains(t1))
				{
					testfilter.add(t1);
				}
				else if(testSet.contains(t2))
				{
					testfilter.add(t2);
				}
				else
				{
					filted[k].add(ptr[k][i]);
				}
			}
		}
		train_triplets=filted[0].toArray(new int[0][0]);
		validate_triplets=filted[1].toArray(new int[0][0]);
		
		filted[2]=new ArrayList<int[]>();
		for(int i=0;i<test_triplets.length;i++)
		{
			ThreeInt t1=new ThreeInt();
			t1.a=test_triplets[i][0];
			t1.b=test_triplets[i][1];
			t1.c=-1;
		
			ThreeInt t2=new ThreeInt();
			t2.a=-1;
			t2.b=test_triplets[i][1];
			t2.c=test_triplets[i][2];
				
			if(testfilter.contains(t1)||testfilter.contains(t2))
			{}
			else
			{
				filted[2].add(test_triplets[i]);
			}
		}
		test_triplets=filted[2].toArray(new int[0][0]);
	}
	
	public void StatisticTrainingSet()
	{
		int maxEntityId=-1;
		int maxRelationId=-1;
		for(int i=0;i<train_triplets.length;i++)
		{
			if(train_triplets[i][0]>maxEntityId)
				maxEntityId=train_triplets[i][0];
			if(train_triplets[i][1]>maxRelationId)
				maxRelationId=train_triplets[i][1];
			if(train_triplets[i][2]>maxEntityId)
				maxEntityId=train_triplets[i][2];
		}
		em.setEntityNum(maxEntityId+1);
		em.setRelationNum(maxRelationId+1);
		
	}
	
	public void SetEmbeddingModelSpecificParameter(SpecificParameter para)
	{
		em.SetSpecificParameterStream(para);
	}
	

	/**
	 * Adjust the learning rate. It is a usual parameter you need adjust.
	 * @param gamma
	 */
	public void SetGamma(double gamma)
	{
		em.setGamma(gamma);
	}
	public void SetMiniBranch(int mini)
	{
		em.setMinibranchsize(mini);
	}
	public void SetMargin(double margin)
	{
		em.setMargin(margin);
	}
	
	public int[][] getTrain_triplets() {
		return train_triplets;
	}
	public void setTrain_triplets(int[][] train_triplets) {
		this.train_triplets = train_triplets;
	}
	public int[][] getValidate_triplets() {
		return validate_triplets;
	}
	public void setValidate_triplets(int[][] validate_triplets) {
		this.validate_triplets = validate_triplets;
	}
	public int[][] getTest_triplets() {
		return test_triplets;
	}
	public void setTest_triplets(int[][] test_triplets) {
		this.test_triplets = test_triplets;
	}
	public EmbeddingModel getEm() {
		return em;
	}
	public void setEm(EmbeddingModel em) {
		this.em = em;
	}
	
	public String getPrint_model_file() {
		return print_model_file;
	}
	public void setPrint_model_file(String print_model_file) {
		this.print_model_file = print_model_file;
	}
	
	public String getPrint_log_file() {
		return print_log_file;
	}
	public void setPrint_log_file(String print_log_file) {
		this.print_log_file = print_log_file;
	}
	
	public String getPrintMiddleModel_dir() {
		return printMiddleModel_dir;
	}
	public void setPrintMiddleModel_dir(String printMiddleModel_dir) {
		this.printMiddleModel_dir = printMiddleModel_dir;
	}
	
	public String getEmbedding_init_file() {
		return embedding_init_file;
	}
	public void setEmbedding_init_file(String embedding_init_file) {
		this.embedding_init_file = embedding_init_file;
	}
	
	public boolean isQuiet() {
		return quiet;
	}
	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
		em.setQuiet(quiet);
	}

	public String getPath_structure_file() {
		return path_structure_file;
	}

	public void setPath_structure_file(String path_structure_file) {
		this.path_structure_file = path_structure_file;
	}


	@Override
	public Object call() throws Exception {
		// TODO Auto-generated method stub
		Processing();
		return null;
	}

}
