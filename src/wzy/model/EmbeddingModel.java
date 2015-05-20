package wzy.model;

import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.Set;

import wzy.meta.TripletHash;

/**
 * It is a base class for embedding-based models, i.e., TransE.
 * Any embedding-based model can inherit this class, and overwrite its functions, specifically.
 * However, there are several functions are empty and must to be overwrote:
 * void InitEmbeddingsRandomly();
 * double CalculateSimilarity(int[] triplet);
 * void CalculateGradient(int[] triplet,List<Object> gradientList);
 * List<Object> ListingEmbedding();
 * @author Zhuoyu Wei
 * @version 1.0
 */
public class EmbeddingModel {

	//private double[][] entity_embedding
	
	private boolean L1regular=false;
	private boolean project=true;
	private boolean trainprintable=true;	
	private Random rand=new Random();	
	
	private int Epoch=500;
	private int minibranchsize=4800;
	private double gamma=0.1;
	private double margin=1.;
	private int random_data_each_epoch=0;
	
	//for debug
	private List<Double> pointerr_buffer_list;
	private List<Double> pairerr_buffer_list;
	
	private double lammadaL1=0.;
	private double lammadaL2=0.;
	

	

	private int entityNum;
	private int relationNum;
	
	/**
	 * Need to be overwrote
	 * init all embeddings
	 * because different model has its different structure of entity and relation 
	 * and different methods of initializing
	 * @need Override
	 */
	public void InitEmbeddingsRandomly()
	{}
	
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
	 * @param gradientList different model has different objects in this list, 
	 * and they need cast the objects to embeddings in this method by themselves.
	 * @need Override
	 */
	private void CalculateGradient(int[] triplet,List<Object> gradientList)
	{}
	
	/**
	 * Upgrade all parameters in your model.
	 * @param embeddingList 
	 * @param gradientList
	 */
	private void UpgradeGradients(List<Object> embeddingList,List<Object> gradientList)
	{
		for(int i=0;i<embeddingList.size();i++)
		{
			if(embeddingList.get(i) instanceof double[][])
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
	private void UpgradeGradients(double[] embedding,double[] gradient)
	{
		for(int i=0;i<embedding.length;i++)
		{
			embedding[i]+=gamma*gradient[i]*(-1);
		}
	}
	private void UpgradeGradients(double[][] embedding,double[][] gradient)
	{
		for(int i=0;i<embedding.length;i++)
		{
			UpgradeGradients(embedding[i],gradient[i]);
		}
	}	
	private void UpgradeGradients(double[][][] embedding,double[][][] gradient)
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
	private void UpgradeGradients(List<Object> embeddingList,List<Object> gradientList,int index)
	{}
	

	/**
	 * After changing any parameter vector when training,it need 
	 * @param embeddingList
	 */
	private void BallProjecting(List<Object> embeddingList)
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
	private void L1BallProjecting(double[][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			L1BallProjecting(embeddings[i]);
		}
	}
	private void L2BallProjecting(double[][] embeddings)
	{
		for(int i=0;i<embeddings.length;i++)
		{
			L2BallProjecting(embeddings[i]);
		}
	}	
	private void L1BallProjecting(double[] embeddings)
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
	private void L2BallProjecting(double[] embeddings)
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
	private List<Object> ListingEmbedding()
	{
		return new ArrayList<Object>();
	}
	/**
	 * For UpgradeGradients(),
	 * create gradients matrixes for variables for entity, relation, and other weight matrixes.
	 * @return
	 * @need Override
	 */
	private List<Object> ListingGradient()
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
	private void OneBranchTraining(int[][] train_triplets,int sindex,int eindex)
	{
		List<Object> embeddingList=ListingEmbedding();
		List<Object> gradientList=ListingGradient();
		for(int i=sindex;i<=eindex;i++)
		{
			CalculateGradient(train_triplets[i],gradientList);
		}
		
		UpgradeGradients(embeddingList,gradientList);
		if(project)
			BallProjecting(embeddingList);
	}
	
	private boolean DiscriminateTripletMinium(double score)
	{
		return score<margin;
	}
	private boolean DiscriminateTripletMinium(double score,double falsescore)
	{
		return score+margin<falsescore;
	}	
	private boolean DiscriminateTripletMaxium(double score)
	{
		return score>margin;
	}
	private boolean DiscriminateTripletMaxium(double score,double falsescore)
	{
		return score-margin>falsescore;
	}		
	
	/**
	 * It is used to calculate errors for training, and to judge whether the learning process can stop.
	 * This function return the least similarity score for triplet.
	 * @param triplet
	 * @return 
	 */
	private double CalculatePointError(int[] triplet)
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
	private double CalculatePairError(int[] triplet)
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
	
	

	
	public void Training(int[][] train_triplets,int[][] validate_triplets)
	{
		int branch=train_triplets.length/minibranchsize;
		if(train_triplets.length%minibranchsize>0)
			branch++;
		if(trainprintable)
		{
			pointerr_buffer_list=new ArrayList<Double>();
			pointerr_buffer_list.add(Double.MAX_VALUE);
			pairerr_buffer_list=new ArrayList<Double>();
			pairerr_buffer_list.add(Double.MAX_VALUE);
		}
		double lasttrain_point_err=Double.MIN_VALUE;
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
				System.out.println("Epoch "+epoch+" is end at "+(end-start)/1000+"s");
				System.out.println("\t"+train_point_err+"\t"+train_pair_err+
						"\t"+valid_point_err+"\t"+valid_pair_err+
						"\t"+(lasttrain_point_err-train_point_err)+"\t"+(lasttrain_pair_err-train_pair_err)+
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
	private void PreTesting(int[][] test_triplets)
	{}
	
	

	/**
	 * 
	 */
	public void Testing(Set<TripletHash> filteringSet,int[][] test_triplets)
	{
		PreTesting(test_triplets);
		for(int i=0;i<test_triplets.length;i++)
		{
			int[] falsetriplet;
			int rawcount;
			int filtercount;
			double score=CalculateSimilarity(test_triplets[i]);
			//left testing
			falsetriplet=copyints(test_triplets[i]);
			rawcount=0;
			filtercount=0;
			for(int j=0;j<entityNum;j++)
			{
				if(j==test_triplets[i][0])
					continue;
				falsetriplet[2]=j;
				double tscore=CalculateSimilarity(falsetriplet);
				if(tscore<score)
				{
					rawcount++;
					TripletHash tri=new TripletHash();
					tri.setTriplet(falsetriplet);
					if(!filteringSet.contains(falsetriplet))
						filtercount++;
				}
			}
			System.out.println();
			//right testing
			
		}
	}
	private int[] copyints(int[] s)
	{
		int[] r=new int[s.length];
		for(int i=0;i<s.length;i++)
			r[i]=s[i];
		return r;
	}
	public Set<TripletHash> BuildTrainAndValidTripletSet(int[][] train_triplets,int[][] valid_triplets)
	{
		Set<TripletHash> trainAndValidSet=new HashSet<TripletHash>();
		int[][][] triplets=new int[2][][];
		triplets[0]=train_triplets;
		triplets[1]=valid_triplets;
		for(int m=0;m<2;m++)
		{
			for(int i=0;i<triplets[m].length;i++)
			{
				TripletHash tri=new TripletHash();
				tri.setTriplet(triplets[m][i]);
				trainAndValidSet.add(tri);
			}
		}
		return trainAndValidSet;
	}
	
}
