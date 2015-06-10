package wzy.model;

import java.util.ArrayList;
import java.util.List;

import wzy.model.para.SpecificParameter;
import wzy.model.para.TransEParameter;
import wzy.tool.MatrixTool;

public class TransS extends EmbeddingModel{

	private double[][] entityEmbedding;
	private double[][] relationEmbedding;
	private double[][] entityGradient;
	private double[][] relationGradient;
	private int entity_dim;
	private int relation_dim;
	
	
	@Override
	protected void InitEmbeddingsMemory()
	{
		entityEmbedding=new double[entityNum][entity_dim];
		relationEmbedding=new double[relationNum][relation_dim];
	}
	
	@Override
	public void InitEmbeddingsRandomly(int[][] triplets)
	{
		InitEmbeddingsMemory();
		for(int i=0;i<entityNum;i++)
		{
			for(int j=0;j<entity_dim;j++)
			{
				entityEmbedding[i][j]=rand.nextDouble();
				if(rand.nextDouble()<0.5)
					entityEmbedding[i][j]=-entityEmbedding[i][j];
			}
			double x=MatrixTool.VectorNorm1(entityEmbedding[i]);
			if(x>1)
			{
				for(int j=0;j<entity_dim;j++)
				{
					entityEmbedding[i][j]/=x;
				}
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
			double x=MatrixTool.VectorNorm1(relationEmbedding[i]);
			if(x>1)
			{
				for(int j=0;j<relation_dim;j++)
				{
					relationEmbedding[i][j]/=x;
				}
			}
		}		
	}
	@Override
	protected void InitGradients()
	{
		entityGradient=new double[entityNum][entity_dim];
		relationGradient=new double[relationNum][relation_dim];
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
		double[] resvector=CalculateTripletVector(triplet);
		return MatrixTool.VectorNorm1(resvector);
	}
	
	
	/**
	 * L1 similarity
	 */
	@Override
	protected void CalculateGradient(int[] triplet)
	{
		int[] ftriplet=GenerateFalseTriplet(triplet);
		double[] truevector=CalculateTripletVector(triplet);
		double[] falsevector=CalculateTripletVector(ftriplet);
		double truesimi=MatrixTool.VectorNorm1(truevector);
		double falsesimi=MatrixTool.VectorNorm1(falsevector);
		
		if(falsesimi+margin-truesimi>0)
		{
			for(int i=0;i<truevector.length;i++)
			{
				if(truevector[i]<0)
				{
					entityGradient[triplet[0]][i]+=relationEmbedding[triplet[1]][i];
					relationGradient[triplet[1]][i]+=entityEmbedding[triplet[0]][i]-entityEmbedding[triplet[2]][i];
					entityGradient[triplet[2]][i]-=relationEmbedding[triplet[1]][i];
				}
				else
				{
					entityGradient[triplet[0]][i]-=relationEmbedding[triplet[1]][i];
					relationGradient[triplet[1]][i]-=entityEmbedding[triplet[0]][i]-entityEmbedding[triplet[2]][i];
					entityGradient[triplet[2]][i]+=relationEmbedding[triplet[1]][i];			
				}
			}
			for(int i=0;i<falsevector.length;i++)
			{
				if(falsevector[i]>0)
				{
					entityGradient[ftriplet[0]][i]+=relationEmbedding[ftriplet[1]][i];
					relationGradient[ftriplet[1]][i]+=entityEmbedding[ftriplet[0]][i]-entityEmbedding[ftriplet[2]][i];
					entityGradient[ftriplet[2]][i]-=relationEmbedding[ftriplet[1]][i];
				}
				else
				{
					entityGradient[ftriplet[0]][i]-=relationEmbedding[ftriplet[1]][i];
					relationGradient[ftriplet[1]][i]-=entityEmbedding[ftriplet[0]][i]-entityEmbedding[ftriplet[2]][i];
					entityGradient[ftriplet[2]][i]+=relationEmbedding[ftriplet[1]][i];			
				}
			}			
		}	
	}
	private double[] CalculateTripletVector(int[] triplet)
	{
		
		double[] resvector=new double[entity_dim];
		double[] hm=new double[entity_dim];
		double[] tm=new double[entity_dim];
		
		for(int i=0;i<entity_dim;i++)
		{
			hm[i]=entityEmbedding[triplet[0]][i]*relationEmbedding[triplet[1]][i];
			tm[i]=entityEmbedding[triplet[2]][i]*relationEmbedding[triplet[1]][i];
		}
		
		for(int i=0;i<entity_dim;i++)
		{
			resvector[i]=hm[i]-tm[i];
		}	
		return resvector;
	}
	
	protected List<Object> ListingEmbedding()
	{
		List<Object> embListing=new ArrayList<Object>();
		embListing.add(entityEmbedding);
		embListing.add(relationEmbedding);
		return embListing;
	}
	protected List<Object> ListingGradient()
	{
		List<Object> graListing=new ArrayList<Object>();
		graListing.add(entityGradient);
		graListing.add(relationGradient);
		return graListing;
	}

	@Override
	public void SetSpecificParameterStream(SpecificParameter para)
	{
		TransEParameter ptransE=(TransEParameter)para;
		entity_dim=ptransE.getEntityDim();
		relation_dim=ptransE.getRelationDim();
	}
	

	///////////////////
	@Override
	public void SetBestParameter()
	{
		L1regular=false;
		project=true;
		trainprintable=true;	

		Epoch=1000;
		minibranchsize=4800;
		gamma=0.01;
		margin=1.;
		random_data_each_epoch=100000;
		bern=false;
		
		lammadaL1=0.;
		lammadaL2=0.;
	}
	
	@Override
	protected void RegularEmbedding(int[][] train_triplets,int sindex,int eindex)
	{
		double[][][] embeddings=new double[2][][];
		embeddings[0]=entityEmbedding;
		embeddings[1]=relationEmbedding;
		for(int i=0;i<embeddings.length;i++)
		{
			for(int j=0;j<embeddings[i].length;j++)
			{
				double x=MatrixTool.VectorNorm2(embeddings[i][j]);
				if(x>0.1)
				{
					for(int k=0;k<embeddings[i][j].length;k++)
					{
						embeddings[i][j][k]/=x;
					}
				}
			}
		}
	}
}
