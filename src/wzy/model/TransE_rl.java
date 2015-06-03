package wzy.model;

import java.util.ArrayList;
import java.util.List;

import wzy.io.FileTools;
import wzy.model.EmbeddingModel;
import wzy.model.para.SpecificParameter;
import wzy.model.para.TransEParameter;
import wzy.tool.MatrixTool;

public class TransE_rl extends EmbeddingModel{
	
	private double[][] entityEmbedding_l;
	private double[][] entityEmbedding_r;	
	private double[][] relationEmbedding;
	private double[][] entityGradient_l;
	private double[][] entityGradient_r;	
	private double[][] relationGradient;
	private int entity_dim;
	private int relation_dim;
	
	
	@Override
	protected void InitEmbeddingsMemory()
	{
		entityEmbedding_l=new double[entityNum][entity_dim];
		entityEmbedding_r=new double[entityNum][entity_dim];		
		relationEmbedding=new double[relationNum][relation_dim];
	}
	
	@Override
	public void InitEmbeddingsRandomly()
	{
		//cannot be initialized randomly.
	}
	
	@Override
	public void InitEmbeddingFromFile(String filename)
	{
		InitEmbeddingsMemory();
		List<Object> embeddingList=new ArrayList<Object>();
		embeddingList.add(entityEmbedding_l);
		embeddingList.add(relationEmbedding);
		FileTools.ReadEmbeddingsFromFile(filename, embeddingList);
		entityEmbedding_r=MatrixTool.CopyMatrix(entityEmbedding_l);
	}
	
	@Override
	protected void InitGradients()
	{
		entityGradient_l=new double[entityNum][entity_dim];
		entityGradient_r=new double[entityNum][entity_dim];		
		relationGradient=new double[relationNum][relation_dim];
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
		
		if(truesimi+margin-falsesimi>0)
		{
			for(int i=0;i<truevector.length;i++)
			{
				if(truevector[i]>0)
				{
					entityGradient_l[triplet[0]][i]+=1;
					relationGradient[triplet[1]][i]+=1;
					entityGradient_r[triplet[2]][i]-=1;
				}
				else
				{
					entityGradient_l[triplet[0]][i]-=1;
					relationGradient[triplet[1]][i]-=1;
					entityGradient_r[triplet[2]][i]+=1;					
				}
			}
			for(int i=0;i<falsevector.length;i++)
			{
				if(falsevector[i]<0)
				{
					entityGradient_l[ftriplet[0]][i]+=1;
					relationGradient[ftriplet[1]][i]+=1;
					entityGradient_r[ftriplet[2]][i]-=1;
				}
				else
				{
					entityGradient_l[ftriplet[0]][i]-=1;
					relationGradient[ftriplet[1]][i]-=1;
					entityGradient_r[ftriplet[2]][i]+=1;					
				}
			}			
		}	
	}
	private double[] CalculateTripletVector(int[] triplet)
	{
		
		double[] resvector=new double[entity_dim];
		for(int i=0;i<entity_dim;i++)
		{
			resvector[i]=entityEmbedding_l[triplet[0]][i]+relationEmbedding[triplet[1]][i]-entityEmbedding_r[triplet[2]][i];
		}	
		return resvector;
	}
	
	protected List<Object> ListingEmbedding()
	{
		List<Object> embListing=new ArrayList<Object>();
		embListing.add(entityEmbedding_l);
		embListing.add(entityEmbedding_r);		
		embListing.add(relationEmbedding);
		return embListing;
	}
	protected List<Object> ListingGradient()
	{
		List<Object> graListing=new ArrayList<Object>();
		graListing.add(entityGradient_l);
		graListing.add(entityGradient_r);		
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
		gamma=0.001;
		margin=1.;
		random_data_each_epoch=100000;
		bern=false;
		
		lammadaL1=0.;
		lammadaL2=0.;
	}
	
	@Override
	protected void RegularEmbedding(int[][] train_triplets,int sindex,int eindex)
	{
		double[][][] embeddings=new double[3][][];
		embeddings[0]=entityEmbedding_l;
		embeddings[1]=entityEmbedding_r;		
		embeddings[2]=relationEmbedding;
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
