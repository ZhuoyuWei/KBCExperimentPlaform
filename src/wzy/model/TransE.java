package wzy.model;

import java.util.List;

import wzy.meta.TripletHash;
import wzy.tool.MatrixTool;

public class TransE extends EmbeddingModel {

	private double[][] entityEmbedding;
	private double[][] relationEmbedding;
	private double[][] entityGradient;
	private double[][] relationGradient;
	private int entity_dim;
	private int relation_dim;
	
	
	@Override
	public void InitEmbeddingsRandomly()
	{
		entityEmbedding=new double[entityNum][entity_dim];
		relationEmbedding=new double[relationNum][relation_dim];
		for(int i=0;i<entityNum;i++)
		{
			for(int j=0;j<entity_dim;j++)
			{
				entityEmbedding[i][j]=rand.nextDouble();
				if(rand.nextDouble()<0.5)
					entityEmbedding[i][j]=-entityEmbedding[i][j];
			}
		}
		for(int i=0;i<relationNum;i++)
		{
			for(int j=0;j<relation_dim;j++)
			{
				relationEmbedding[i][j]=rand.nextDouble();
				if(rand.nextDouble()<0.5)
					relationEmbedding[i][j]=-relationEmbedding[i][j];
			}
		}		
	}
	protected void InitGradients()
	{
		for(int i=0;i<entityGradient.length;i++)
		{
			for(int j=0;j<entityGradient[i].length;j++)
			{
				entityGradient[i][j]=0.;
			}
		}
		for(int i=0;i<relationGradient.length;i++)
		{
			for(int j=0;j<relationGradient[i].length;j++)
			{
				relationGradient[i][j]=0.;
			}
		}
	}
	/**
	 * L1 similarity
	 */
	@Override
	public double CalculateSimilarity(int[] triplet)
	{
		double[] resvector=new double[entity_dim];
		for(int i=0;i<entity_dim;i++)
		{
			resvector[i]=entityEmbedding[triplet[0]][i]+relationEmbedding[triplet[1]][i]-entityEmbedding[triplet[2]][i];
		}
		return MatrixTool.VectorNorm1(resvector);
	}
	
	
	/**
	 * L1 similarity
	 */
	@Override
	protected void CalculateGradient(int[] triplet,List<Object> gradientList)
	{
		TripletHash falseTri=new TripletHash();
		falseTri.setTriplet(copyints(triplet));
		 
		double pr=0.5;
		if(bern)
			pr=relation_entity_counts[triplet[1]][1]
					/(relation_entity_counts[triplet[1]][0]+relation_entity_counts[triplet[1]][1]);

		if(rand.nextDouble()<pr)
		{
			while(!filteringSet.contains(falseTri))
				falseTri.getTriplet()[2]=Math.abs(rand.nextInt())%entityNum;
		}
		else
		{
			while(!filteringSet.contains(falseTri))
				falseTri.getTriplet()[0]=Math.abs(rand.nextInt())%entityNum;			
		}
		int[] ftriplet=falseTri.getTriplet();
		
		double[] truevector=CalculateTripletVector(triplet);
		double[] falsevector=CalculateTripletVector(ftriplet);
		double truesimi=MatrixTool.VectorNorm1(truevector);
		double falsesimi=MatrixTool.VectorNorm1(falsevector);
		
		if(truesimi+margin-falsesimi>0)
		{
			for(int i=0;i<truevector.length;i++)
			{
				if(truevector[i]>0)
				{
					entityGradient[triplet[0]][i]+=1;
					relationGradient[triplet[1]][i]+=1;
					entityGradient[triplet[2]][i]-=1;
				}
				else
				{
					entityGradient[triplet[0]][i]-=1;
					relationGradient[triplet[1]][i]-=1;
					entityGradient[triplet[2]][i]+=1;					
				}
			}
			for(int i=0;i<falsevector.length;i++)
			{
				if(falsevector[i]<0)
				{
					entityGradient[ftriplet[0]][i]+=1;
					relationGradient[ftriplet[1]][i]+=1;
					entityGradient[ftriplet[2]][i]-=1;
				}
				else
				{
					entityGradient[ftriplet[0]][i]-=1;
					relationGradient[ftriplet[1]][i]-=1;
					entityGradient[ftriplet[2]][i]+=1;					
				}
			}			
		}
		
		
	}
	private double[] CalculateTripletVector(int[] triplet)
	{
		double[] resvector=new double[entity_dim];
		for(int i=0;i<entity_dim;i++)
		{
			resvector[i]=entityEmbedding[triplet[0]][i]+relationEmbedding[triplet[1]][i]-entityEmbedding[triplet[2]][i];
		}	
		return resvector;
	}
	
	protected List<Object> ListingEmbedding()
	{
		List<Object> 
		return null;
	}
	protected List<Object> ListingGradient()
	{}

	
	protected void PreTesting(int[][] test_triplets)
	{}
	
	
}
