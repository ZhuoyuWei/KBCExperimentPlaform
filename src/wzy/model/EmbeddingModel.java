package wzy.model;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import wzy.meta.TripletHash;
import wzy.model.para.SpecificParameter;

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
 * @author Zhuoyu Wei
 * @version 1.0
 */
public class EmbeddingModel {

	//protected double[][] entity_embedding
	
	protected boolean L1regular=false;
	protected boolean project=true;
	protected boolean trainprintable=true;	
	protected Random rand=new Random();	
	
	protected int Epoch=1000;
	protected int minibranchsize=4800;
	protected double gamma=0.001;
	protected double margin=1.;
	protected int random_data_each_epoch=100000;
	protected boolean bern=true;
	protected Set<TripletHash> filteringSet; 
	
	protected double lammadaL1=0.;
	protected double lammadaL2=0.;
	
	protected int entityNum;
	protected int relationNum;
	protected int[][] relation_entity_counts;
	
	/**
	 * Need to be overwrote
	 * init all embeddings
	 * because different model has its different structure of entity and relation 
	 * and different methods of initializing
	 * @need Override
	 */
	public void InitEmbeddingsRandomly()
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
			if(embeddingList.get(i) instanceof double[][])
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
		
/*		for(int i=0;i<gradientList.size();i++)
		{
			double[][]gradient=(double[][])gradientList.get(i);
			for(int j=0;j<gradient.length;j++)
			{
				for(int k=0;k<gradient[j].length;k++)
				{
					if(Math.abs(gradient[j][k])>1e-6)
					{
						System.out.println(i+"\t"+j+"\t"+k);
					}
				}
			}
		}*/
		
		
		UpgradeGradients(embeddingList,gradientList);
		if(project)
			BallProjecting(embeddingList);
	}
	
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
		int[][] falsetriplets=new int[2][3];
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<3;j++)
			{
				falsetriplets[i][j]=triplet[j];
			}
		}
		falsetriplets[0][2]=Math.abs(rand.nextInt())%entityNum;
		falsetriplets[1][0]=Math.abs(rand.nextInt())%entityNum;
		
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
	
	public void Training(int[][] train_triplets,int[][] validate_triplets)
	{
		int branch=train_triplets.length/minibranchsize;
		if(train_triplets.length%minibranchsize>0)
			branch++;
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
			
		}
	}
	
	/**
	 * Need to be overwrote.
	 * In this function, you can change your embeddings, weights, even test triplets.
	 * @need Override
	 */
	protected void PreTesting(int[][] test_triplets)
	{}
	
	

	/**
	 * 
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
	}
	protected int[] copyints(int[] s)
	{
		int[] r=new int[s.length];
		for(int i=0;i<s.length;i++)
			r[i]=s[i];
		return r;
	}
	/**
	 * Form a set of all true tirplets in train and validate set.
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
	
}