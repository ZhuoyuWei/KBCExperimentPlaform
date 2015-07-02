package wzy.model;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import wzy.io.FileTools;
import wzy.meta.TripletHash;
import wzy.model.para.SpecificParameter;
import wzy.tool.MatrixTool;

/**
 * It is a base class for embedding-based models, i.e., TransE.
 * Any embedding-based model can inherit this class, and overwrite its functions, specifically.
 * However, there are several functions are empty and must to be overwrote:
 * 		void InitEmbeddingsRandomly();
 * 		double CalculateSimilarity(int[] triplet);
 * 		void CalculateGradient(int[] triplet,List<Object> gradientList);
 * 		List<Object> ListingEmbedding();
 *      List<Object> ListingGradient();
 * 		void PreTesting(int[][] test_triplets);
 * 		void InitGradients();
 *      void SetSpecificParameterStream(SpecificParameter para);
 *      void InitEmbeddingsMemory();
 *      void RegularEmbedding();
 *      void InitEmbeddingFromFile(String filename);
 * @author Zhuoyu Wei
 * @version 1.0
 */
public class EmbeddingModel {

	//protected double[][] entity_embedding
	
	protected boolean L1regular=false;
	protected boolean project=false;
	protected boolean trainprintable=false;//true;	
	protected Random rand=new Random();	
	
	protected int Epoch=1000;
	protected int minibranchsize=4800;
	protected double gamma=0.01;
	protected double margin=1.;
	protected int random_data_each_epoch=100000;
	protected boolean bern=false;//true;//false;//true;
	protected Set<TripletHash> filteringSet; 
	
	protected double lammadaL1=0.;
	protected double lammadaL2=0;
	
	protected int entityNum;
	protected int relationNum;
	protected int[][] relation_entity_counts;
	
	protected String printMiddleModel_dir=null;
	protected int printEpoch=100;
	
	//for debug
	protected int errcount=0;
	protected boolean quiet=false;
	protected String print_log_file=null;
	
	public void Training(int[][] train_triplets,int[][] validate_triplets)
	{
		int branch=train_triplets.length/minibranchsize;
		//if(train_triplets.length%minibranchsize>0) //if the size of minibranch didn't touch minibranch size
			//branch++;
		double lasttrain_point_err=Double.MAX_VALUE;
		double lasttrain_pair_err=Double.MAX_VALUE;				
		double lastvalid_point_err=Double.MAX_VALUE;
		double lastvalid_pair_err=Double.MAX_VALUE;	
		for(int epoch=0;epoch<Epoch;epoch++)
		{
			//Disrupt the order of training data set
			long start=System.currentTimeMillis();
			for(int i=0;i<random_data_each_epoch;i++)
			{
				int a=Math.abs(rand.nextInt())%train_triplets.length;
				int b=Math.abs(rand.nextInt())%train_triplets.length;	
				int[] t=train_triplets[a];
				train_triplets[a]=train_triplets[b];
				train_triplets[b]=t;
			}
			
			for(int i=0;i<branch;i++)
			{
				int sindex=i*minibranchsize;
				int eindex=(i+1)*minibranchsize-1;
				if(eindex>=train_triplets.length)
					eindex=train_triplets.length-1;
				InitGradients();
				OneBranchTraining(train_triplets,sindex,eindex);	
			}
			long end=System.currentTimeMillis();
			if(trainprintable)
			{
				//you can change printable information which is your interets.
				//Or you can change System.out to file
				double train_point_err=0.;
				double train_pair_err=0.;				
				double valid_point_err=0.;
				double valid_pair_err=0.;				
				for(int i=0;i<train_triplets.length;i++)
				{				
					train_point_err+=CalculatePointError(train_triplets[i]);
					train_pair_err+=CalculatePairError(train_triplets[i]);
				}
				for(int i=0;i<validate_triplets.length;i++)
				{
					valid_point_err+=CalculatePointError(validate_triplets[i]);
					valid_pair_err+=CalculatePairError(validate_triplets[i]);					
				}
				end=System.currentTimeMillis();
				train_point_err/=train_triplets.length;
				train_pair_err/=train_triplets.length;
				valid_point_err/=validate_triplets.length;
				valid_pair_err/=validate_triplets.length;
				System.err.println("Epoch "+epoch+" is end at "+(end-start)/1000+"s");
				System.err.println("\t"+train_point_err+"\t"+train_pair_err+
						"\t"+valid_point_err+"\t"+valid_pair_err+
						"\n\t"+(lasttrain_point_err-train_point_err)+"\t"+(lasttrain_pair_err-train_pair_err)+
						"\t"+(lastvalid_point_err-valid_point_err)+"\t"+(lastvalid_pair_err-valid_pair_err));
				
				lasttrain_point_err=train_point_err;
				lasttrain_pair_err=train_pair_err;
				lastvalid_point_err=valid_point_err;
				lastvalid_pair_err=valid_pair_err;
				
			}
			else
			{
				System.err.println("Epoch "+epoch+" is end at "+(end-start)/1000+"s");
			}
			
			if(printMiddleModel_dir!=null)
			{
				if(epoch%printEpoch==printEpoch-1)
				{
					this.PrintModel(printMiddleModel_dir+"/epoch"+epoch);
				}
			}
			
		}
	}
	
	/**
	 * Need to be overwrote
	 * init all embeddings
	 * because different model has its different structure of entity and relation 
	 * and different methods of initializing
	 * @need Override
	 */
	public void InitEmbeddingsRandomly(int[][] triplets)
	{}
	
	protected void InitEmbeddingsMemory()
	{}
	
	public void InitEmbeddingFromFile(String filename)
	{}
	public void InitPathFromFile(String filename)
	{}
	
	public void CountEntityForRelation(int[][] train_triplets)
	{
		relation_entity_counts=new int[relationNum][2];
		Set<Integer>[][] entitySets=new Set[relationNum][2];
		for(int i=0;i<relationNum;i++)
		{
			entitySets[i][0]=new HashSet<Integer>();
			entitySets[i][1]=new HashSet<Integer>();
		}
		for(int i=0;i<train_triplets.length;i++)
		{
			entitySets[train_triplets[i][1]][0].add(train_triplets[i][0]);
			entitySets[train_triplets[i][1]][1].add(train_triplets[i][2]);
		}
		for(int i=0;i<relationNum;i++)
		{
			relation_entity_counts[i][0]=entitySets[i][0].size();
			relation_entity_counts[i][1]=entitySets[i][1].size();
		}
	}
	
	/**
	 * Need to be overwrote
	 * calculate similarity score for a triplet
	 * because different model has its different structure of entity and relation 
	 * and have different usage of entity or relation embeddings.
	 * Also, it can be L1-norm, L2-norm, or product between several vectors.
	 * @param triplet
	 * @return the similarity score of a triplet
	 * @need Override
	 */
	public double CalculateSimilarity(int[] triplet)
	{
		return 0;
	}
	
	/**
	 * Need to be overwrote
	 * Calculate gradients for both entity and relation embeddings, and may include other weights matrix and so on.
	 * However, different model has different method of calculating gradients.
	 * This function need be overwrote.
	 * @param triplet calculate gradient for it
	 * and they need cast the objects to embeddings in this method by themselves.
	 * @need Override
	 */
	protected void CalculateGradient(int[] triplet)
	{}
	
	/**
	 * Upgrade all parameters in your model.
	 * @param embeddingList 
	 * @param gradientList
	 */
	protected void UpgradeGradients(List<Object> embeddingList,List<Object> gradientList)
	{
		for(int i=0;i<embeddingList.size();i++)
		{
			if(embeddingList.get(i) instanceof double[][][])
			{
				UpgradeGradients((double[][][])embeddingList.get(i),(double[][][])gradientList.get(i));
			}
			else if(embeddingList.get(i) instanceof double[][])
			{
				UpgradeGradients((double[][])embeddingList.get(i),(double[][])gradientList.get(i));	
			}
			else if(embeddingList.get(i) instanceof double[])
			{
				UpgradeGradients((double[])embeddingList.get(i),(double[])gradientList.get(i));			
			}
			else
			{
				UpgradeGradients(embeddingList,gradientList,i);
			}
		}
	}
	protected void UpgradeGradients(double[] embedding,double[] gradient)
	{
		for(int i=0;i<embedding.length;i++)
		{
			embedding[i]+=gamma*gradient[i]*(-1);
		}
	}
	protected void UpgradeGradients(double[][] embedding,double[][] gradient)
	{
		for(int i=0;i<embedding.length;i++)
		{
			UpgradeGradients(embedding[i],gradient[i]);
		}
	}	
	protected void UpgradeGradients(double[][][] embedding,double[][][] gradient)
	{
		for(int i=0;i<embedding.length;i++)
		{
			UpgradeGradients(embedding[i],gradient[i]);
		}
	}	
	/**
	 * Need to be overwrote. Other type parameters for your model.
	 * For example, a double value.
	 * @param embeddingList
	 * @param gradientList
	 * @param index
	 * @need Override
	 */
	protected void UpgradeGradients(List<Object> embeddingList,List<Object> gradientList,int index)
	{}
	

	/**
	 * After changing any parameter vector when training,it need 
	 * @param embeddingList
	 */
	protected void BallProjecting(List<Object> embeddingList)
	{
		for(int i=0;i<embeddingList.size();i++)
		{
			if(embeddingList.get(i) instanceof double[][][])
			{
				if(L1regular)
					L1BallProjecting((double[][][])embeddingList.get(i));
				else
					L2BallProjecting((double[][][])embeddingList.get(i));	
			}			
			else if(embeddingList.get(i) instanceof double[][])
			{
				if(L1regular)
					L1BallProjecting((double[][])embeddingList.get(i));
				else
					L2BallProjecting((double[][])embeddingList.get(i));	
			}
			else if(embeddingList.get(i) instanceof double[])
			{
				if(L1regular)
					L1BallProjecting((double[])embeddingList.get(i));
				else
					L2BallProjecting((double[])embeddingList.get(i));				
			}

		}
	}
	protected void L1BallProjecting(double[][][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			double x=MatrixTool.MatrixNorm1(embeddings[i]);
			if(x>1)
			{
				for(int j=0;j<embeddings[i].length;j++)
				{
					for(int k=0;k<embeddings[i][j].length;k++)
					{
						embeddings[i][j][k]/=x;
					}
				}
			}
		}
	}
	protected void L2BallProjecting(double[][][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			double x=MatrixTool.MatrixNorm2(embeddings[i]);
			if(x>1)
			{
				for(int j=0;j<embeddings[i].length;j++)
				{
					for(int k=0;k<embeddings[i][j].length;k++)
					{
						embeddings[i][j][k]/=x;
					}
				}
			}
		}
	}	
	protected void L1BallProjecting(double[][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			L1BallProjecting(embeddings[i]);
		}
	}
	protected void L2BallProjecting(double[][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			L2BallProjecting(embeddings[i]);
		}
	}	
	protected void L1BallProjecting(double[] embeddings)
	{
		double x=0.;
		for(int i=0;i<embeddings.length;i++)
		{
			x+=Math.abs(embeddings[i]);
		}
		if(x>1.)
		{
			x=1./x;
			for(int i=0;i<embeddings.length;i++)
			{
				embeddings[i]*=x;
			}
		}
	}
	protected void L2BallProjecting(double[] embeddings)
	{
		double x=0.;
		for(int i=0;i<embeddings.length;i++)
		{
			x+=embeddings[i]*embeddings[i];
		}
		if(x>1.)
		{
			x=1./Math.sqrt(x);
			for(int i=0;i<embeddings.length;i++)
			{
				embeddings[i]*=x;
			}
		}
	}	
	
	
	/**
	 * Need to be overwrote
	 * For CalculateGradient() and UpgradeGradients()
	 * add entity, relation, other weight matrixes to the list
	 * @return
	 * @need Override
	 */
	protected List<Object> ListingEmbedding()
	{
		return new ArrayList<Object>();
	}
	/**
	 * For UpgradeGradients(),
	 * create gradients matrixes for variables for entity, relation, and other weight matrixes.
	 * @return
	 * @need Override
	 */
	protected List<Object> ListingGradient()
	{
		return new ArrayList<Object>();		
	}

	/**
	 * In training process, this function is a unit of learning embeddings at one branch.
	 * It includes : (1) calculate gradients for each triplet in this branch; 
	 *               (2) upgrade embeddings by using gradients calculated;
	 *               (3) project each embedding vector to a L1 or L2 ball. 
	 * @param train_triplets
	 * @param sindex
	 * @param eindex
	 */
	protected void OneBranchTraining(int[][] train_triplets,int sindex,int eindex)
	{
		List<Object> embeddingList=ListingEmbedding();
		List<Object> gradientList=ListingGradient();
		for(int i=sindex;i<=eindex;i++)
		{
			CalculateGradient(train_triplets[i]);
		}
		
		UpgradeGradients(embeddingList,gradientList);
		if(project)
			BallProjecting(embeddingList);
		else
			RegularEmbedding(train_triplets,sindex,eindex);
	}
	
	/**
	 * Need to be overwrote.
	 * Regular embeddings, if project flag is set as false, it doesn't use standard projecting algorithm,
	 * but uses model's own regular function.
	 * @need override
	 * @param train_triplets
	 * @param sindex
	 * @param eindex
	 */
	protected void RegularEmbedding(int[][] train_triplets,int sindex,int eindex)
	{}
	
	protected boolean DiscriminateTripletMinium(double score)
	{
		return score<margin;
	}
	protected boolean DiscriminateTripletMinium(double score,double falsescore)
	{
		return score+margin<falsescore;
	}	
	protected boolean DiscriminateTripletMaxium(double score)
	{
		return score>margin;
	}
	protected boolean DiscriminateTripletMaxium(double score,double falsescore)
	{
		return score-margin>falsescore;
	}		
	
	/**
	 * It is used to calculate errors for training, and to judge whether the learning process can stop.
	 * This function return the least similarity score for triplet.
	 * @param triplet
	 * @return 
	 */
	protected double CalculatePointError(int[] triplet)
	{
		return CalculateSimilarity(triplet);
		//return DiscriminateTripletMinium(similarity)?(similarity-1):0;	
	}
	/**
	 * It is used to calculate errors for training, and to judge whether the learning process can stop.
	 * This function return the pairwise error for triplet.
	 * @param triplet
	 * @return 
	 */
	protected double CalculatePairError(int[] triplet)
	{
		int[][] falsetriplets=new int[2][];

		falsetriplets[0]=GenerateFalseTriplet(triplet,0);
		falsetriplets[1]=GenerateFalseTriplet(triplet,1);
		
		double truesimilarity=CalculateSimilarity(triplet);
		double leftfalsesimilarity=CalculateSimilarity(falsetriplets[0]);
		double rightfalsesimilarity=CalculateSimilarity(falsetriplets[1]);
		
		double err=0;
		if(!DiscriminateTripletMinium(truesimilarity,leftfalsesimilarity))
		{
			err+=truesimilarity+margin-leftfalsesimilarity;
		}
		if(!DiscriminateTripletMinium(truesimilarity,rightfalsesimilarity))
		{
			err+=truesimilarity+margin-rightfalsesimilarity;
		}	
		err/=2.;
		return err;
	}
	
	

	/**
	 * Need to be overwrote
	 * When train via a branch, we need initialize gradients for all parameters.
	 * @need Override
	 */
	protected void InitGradients()
	{}
	

	
	/**
	 * Need to be overwrote.
	 * In this function, you can change your embeddings, weights, even test triplets.
	 * @need Override
	 */
	protected void PreTesting(int[][] test_triplets)
	{}
	
	

	/**
	 * Testing methods: Link prediciton, which is described in NIPS2013 TransE.
	 * As default, we only evaluated 
	 */
	public void Testing(int[][] test_triplets)
	{
		PreTesting(test_triplets);
		int raw_hit10l=0;
		int raw_meanl=0;
		int filter_hit10l=0;
		int filter_meanl=0;
		int raw_hit10r=0;
		int raw_meanr=0;
		int filter_hit10r=0;
		int filter_meanr=0;	
		long start=System.currentTimeMillis();
		for(int i=0;i<test_triplets.length;i++)
		{
			int[] falsetriplet;
			int rawcount;
			int filtercount;
			double score=CalculateSimilarity(test_triplets[i]);
			//left testing
			falsetriplet=copyints(test_triplets[i]);
			rawcount=1;
			filtercount=1;
			for(int j=0;j<entityNum;j++)
			{
				if(j==test_triplets[i][2])
					continue;
				falsetriplet[2]=j;
				double tscore=CalculateSimilarity(falsetriplet);
				if(tscore<score)
				{
					rawcount++;
					TripletHash tri=new TripletHash();
					tri.setTriplet(falsetriplet);
					if(!filteringSet.contains(tri))
						filtercount++;
				}
			}
			raw_meanl+=rawcount;
			if(rawcount<=10)
				raw_hit10l++;
			filter_meanl+=filtercount;
			if(filtercount<=10)
				filter_hit10l++;
			//right testing
			falsetriplet=copyints(test_triplets[i]);
			rawcount=1;
			filtercount=1;
			for(int j=0;j<entityNum;j++)
			{
				if(j==test_triplets[i][0])
					continue;
				falsetriplet[0]=j;
				double tscore=CalculateSimilarity(falsetriplet);
				if(tscore<score)
				{
					rawcount++;
					TripletHash tri=new TripletHash();
					tri.setTriplet(falsetriplet);
					if(!filteringSet.contains(tri))
						filtercount++;
				}
			}
			raw_meanr+=rawcount;
			if(rawcount<=10)
				raw_hit10r++;
			filter_meanr+=filtercount;
			if(filtercount<=10)
				filter_hit10r++;			
		}
		long end=System.currentTimeMillis();
		if(test_triplets.length<=0)
			return;
		//Print testing results
		if(!quiet)
		{
			System.out.println("Testing is end at "+(end-start)/1000+"s. Final testing result:");
			System.out.println("Left:\t"+(double)raw_hit10l/test_triplets.length
					+"\t"+(double)raw_meanl/test_triplets.length
					+"\t"+(double)filter_hit10l/test_triplets.length
					+"\t"+(double)filter_meanl/test_triplets.length);
			System.out.println("Right:\t"+(double)raw_hit10r/test_triplets.length
					+"\t"+(double)raw_meanr/test_triplets.length
					+"\t"+(double)filter_hit10r/test_triplets.length
					+"\t"+(double)filter_meanr/test_triplets.length);		
			System.out.println("Final:\t"+(double)(raw_hit10l+raw_hit10r)/test_triplets.length/2.
					+"\t"+(double)(raw_meanl+raw_meanr)/test_triplets.length/2.
					+"\t"+(double)(filter_hit10l+filter_hit10r)/test_triplets.length/2.
					+"\t"+(double)(filter_meanl+filter_meanr)/test_triplets.length/2.);
			System.out.flush();
		}
		if(print_log_file!=null)
		{
			FileTools.PrintFinalResult(print_log_file, end-start, raw_hit10l,
					raw_meanl, filter_hit10l, filter_meanl, raw_hit10r, raw_meanr,
					filter_hit10r, filter_meanr, test_triplets.length);
		}
	}
	
	/**
	 * Just for methods who have paths, and embedding paths like relations.
	 */
	public void Testing_PathEmbedding(int[][] test_triplets)
	{}
	
	public void Testing_Classify(int[] test_triplets)
	{
		//for()
	}
	
	protected int[] copyints(int[] s)
	{
		int[] r=new int[s.length];
		for(int i=0;i<s.length;i++)
			r[i]=s[i];
		return r;
	}
	/**
	 * Form a set of all true triplets in train and validate set.
	 * It is useful in testing or other functions.
	 * @param train_triplets
	 * @param valid_triplets
	 * @return
	 */
	public void BuildTrainAndValidTripletSet(int[][] train_triplets,int[][] valid_triplets)
	{
		filteringSet=new HashSet<TripletHash>();
		int[][][] triplets=new int[2][][];
		triplets[0]=train_triplets;
		triplets[1]=valid_triplets;
		for(int m=0;m<2;m++)
		{
			for(int i=0;i<triplets[m].length;i++)
			{
				TripletHash tri=new TripletHash();
				tri.setTriplet(triplets[m][i]);
				filteringSet.add(tri);
			}
		}
	}
	
	/**
	 * Need to be overwrote
	 * It is used to set specific parameters for specific embedding model, such as entity_dim and weight_matrix.
	 * @param para
	 * @need override
	 */
	public void SetSpecificParameterStream(SpecificParameter para)
	{}

	/**
	 * Check whether a triplet is available. 
	 * @param triplet
	 * @return
	 * @deprecated
	 */
	public boolean CheckTripletAvailable(int[] triplet)
	{
		boolean flag=true;
		if(triplet[0]<0||triplet[0]>=entityNum)
			flag=false;
		if(triplet[1]<0||triplet[1]>=relationNum)
			flag=false;		
		if(triplet[2]<0||triplet[2]>=entityNum)
			flag=false;		
		if(!flag)
		{
			System.out.println("Error:\t"+triplet[0]+"\t"+triplet[1]+"\t"+triplet[2]);
		}
		return flag;
	}
	
	/**
	 * After learning embeddings, we may save the model, and print it to the file with the model's specific format.
	 * @param filename
	 */
	public void PrintModel(String filename)
	{
		List<Object> embeddingList=this.ListingEmbedding();
		FileTools.PrintEmbeddingList(filename, embeddingList);
	}
	
	/**
	 * Need to be overwrote
	 * laze, to save best model parameter, i.e., learning rate and margin
	 * @need overide
	 */
	public void SetBestParameter()
	{}
	
	/**
	 * Generate a false triplet from the true one, it can be bern or unif, which is depend on 'bern' variable.
	 * @param triplet
	 * @return
	 */
	protected int[] GenerateFalseTriplet(int[] triplet)
	{
		double pr=0.5;
		if(bern)
			pr=(double)relation_entity_counts[triplet[1]][1]
					/(relation_entity_counts[triplet[1]][0]+relation_entity_counts[triplet[1]][1]);
		TripletHash falseTri=new TripletHash();
		falseTri.setTriplet(copyints(triplet));
		if(rand.nextDouble()<pr)
		{
			while(filteringSet.contains(falseTri)||falseTri.getTriplet()[2]<0)
				falseTri.getTriplet()[2]=(Math.abs(rand.nextInt()))%entityNum;
		}
		else
		{
			while(filteringSet.contains(falseTri)||falseTri.getTriplet()[0]<0)
				falseTri.getTriplet()[0]=(Math.abs(rand.nextInt()))%entityNum;			
		}
		return falseTri.getTriplet();
	}
	protected int[] GenerateFalseTriplet(int[] triplet,int lr)
	{
		TripletHash falseTri=new TripletHash();
		falseTri.setTriplet(copyints(triplet));
		if(lr==0)
		{
			while(filteringSet.contains(falseTri)||falseTri.getTriplet()[2]<0)
				falseTri.getTriplet()[2]=(Math.abs(rand.nextInt()))%entityNum;
		}
		else
		{
			while(filteringSet.contains(falseTri)||falseTri.getTriplet()[0]<0)
				falseTri.getTriplet()[0]=(Math.abs(rand.nextInt()))%entityNum;			
		}
		return falseTri.getTriplet();
	}	
	
//**********************************************************************************	
	public boolean isL1regular() {
		return L1regular;
	}

	public void setL1regular(boolean l1regular) {
		L1regular = l1regular;
	}

	public boolean isProject() {
		return project;
	}

	public void setProject(boolean project) {
		this.project = project;
	}

	public boolean isTrainprintable() {
		return trainprintable;
	}

	public void setTrainprintable(boolean trainprintable) {
		this.trainprintable = trainprintable;
	}

	public Random getRand() {
		return rand;
	}

	public void setRand(Random rand) {
		this.rand = rand;
	}

	public int getEpoch() {
		return Epoch;
	}

	public void setEpoch(int epoch) {
		Epoch = epoch;
	}

	public int getMinibranchsize() {
		return minibranchsize;
	}

	public void setMinibranchsize(int minibranchsize) {
		this.minibranchsize = minibranchsize;
	}

	public double getGamma() {
		return gamma;
	}

	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

	public double getMargin() {
		return margin;
	}

	public void setMargin(double margin) {
		this.margin = margin;
	}

	public int getRandom_data_each_epoch() {
		return random_data_each_epoch;
	}

	public void setRandom_data_each_epoch(int random_data_each_epoch) {
		this.random_data_each_epoch = random_data_each_epoch;
	}

	public boolean isBern() {
		return bern;
	}

	public void setBern(boolean bern) {
		this.bern = bern;
	}

	public Set<TripletHash> getFilteringSet() {
		return filteringSet;
	}

	public void setFilteringSet(Set<TripletHash> filteringSet) {
		this.filteringSet = filteringSet;
	}

	public double getLammadaL1() {
		return lammadaL1;
	}

	public void setLammadaL1(double lammadaL1) {
		this.lammadaL1 = lammadaL1;
	}

	public double getLammadaL2() {
		return lammadaL2;
	}

	public void setLammadaL2(double lammadaL2) {
		this.lammadaL2 = lammadaL2;
	}

	public int getEntityNum() {
		return entityNum;
	}

	public void setEntityNum(int entityNum) {
		this.entityNum = entityNum;
	}

	public int getRelationNum() {
		return relationNum;
	}

	public void setRelationNum(int relationNum) {
		this.relationNum = relationNum;
	}

	public String getPrintMiddleModel_dir() {
		return printMiddleModel_dir;
	}

	public void setPrintMiddleModel_dir(String printMiddleModel_dir) {
		this.printMiddleModel_dir = printMiddleModel_dir;
	}

	public boolean isQuiet() {
		return quiet;
	}

	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	public String getPrint_log_file() {
		return print_log_file;
	}

	public void setPrint_log_file(String print_log_file) {
		this.print_log_file = print_log_file;
	}

	

}
