package wzy.model;

import java.util.ArrayList;
import java.util.List;

import wzy.meta.TripletHash;
import wzy.model.para.SpecificParameter;
import wzy.model.para.TransEParameter;
import wzy.model.para.TransHParameter;
import wzy.tool.MatrixTool;

public class TransH extends EmbeddingModel{


	private double[][] entityEmbedding;
	private double[][] relationEmbedding;
	private double[][] relationweight; //has the same dimensionality with relationEmbedding or entity.
	private double[][] entityGradient;
	private double[][] relationGradient;
	private double[][] relationweightGradient;
	private int entity_dim;
	private int relation_dim;
	
	
	@Override
	public void InitEmbeddingsRandomly()
	{
		entityEmbedding=new double[entityNum][entity_dim];
		relationEmbedding=new double[relationNum][relation_dim];
		relationweight=new double[relationNum][relation_dim];
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
		for(int i=0;i<relationNum;i++)
		{
			for(int j=0;j<relation_dim;j++)
			{
				relationweight[i][j]=rand.nextDouble();
				if(rand.nextDouble()<0.5)
					relationweight[i][j]=-relationweight[i][j];
			}
			double x=MatrixTool.VectorNorm1(relationweight[i]);
			if(x>1)
			{
				for(int j=0;j<relation_dim;j++)
				{
					relationweight[i][j]/=x;
				}
			}
		}			
	}
	
	@Override
	protected void InitGradients()
	{
		entityGradient=new double[entityNum][entity_dim];
		relationGradient=new double[relationNum][relation_dim];
		relationweightGradient=new double[relationNum][relation_dim];
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
		for(int i=0;i<relationweightGradient.length;i++)
		{
			for(int j=0;j<relationweightGradient[i].length;j++)
			{
				relationweightGradient[i][j]=0.;
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
		
		if(truesimi+margin-falsesimi>0)
		{
			for(int i=0;i<truevector.length;i++)
			{
				if(truevector[i]>0)
				{
					entityGradient[triplet[0]][i]+=1-relationweight[triplet[1]][i];
					relationGradient[triplet[1]][i]+=1;
					entityGradient[triplet[2]][i]-=relationweight[triplet[1]][i]-1;
					relationweightGradient[triplet[1]][i]+=entityEmbedding[triplet[2]][i]-entityEmbedding[triplet[0]][i];
				}
				else
				{
					entityGradient[triplet[0]][i]-=1-relationweight[triplet[1]][i];
					relationGradient[triplet[1]][i]-=1;
					entityGradient[triplet[2]][i]+=relationweight[triplet[1]][i]-1;
					relationweightGradient[triplet[1]][i]-=entityEmbedding[triplet[2]][i]-entityEmbedding[triplet[0]][i];				
				}
			}
			for(int i=0;i<falsevector.length;i++)
			{
				if(falsevector[i]<0)
				{
					entityGradient[ftriplet[0]][i]+=1-relationweight[ftriplet[1]][i];
					relationGradient[ftriplet[1]][i]+=1;
					entityGradient[ftriplet[2]][i]-=relationweight[ftriplet[1]][i]-1;
					relationweightGradient[ftriplet[1]][i]+=entityEmbedding[ftriplet[2]][i]-entityEmbedding[ftriplet[0]][i];
				}
				else
				{
					entityGradient[ftriplet[0]][i]-=1-relationweight[ftriplet[1]][i];
					relationGradient[ftriplet[1]][i]-=1;
					entityGradient[ftriplet[2]][i]+=relationweight[ftriplet[1]][i]-1;
					relationweightGradient[ftriplet[1]][i]-=entityEmbedding[ftriplet[2]][i]-entityEmbedding[ftriplet[0]][i];				
				}
			}			
		}	
	}
	private double[] CalculateTripletVector(int[] triplet)
	{
		double[] resvector=new double[entity_dim];		
		double[] htran=new double[entity_dim];
		double[] ttran=new double[entity_dim];
		for(int i=0;i<entity_dim;i++)
		{
			htran[i]=entityEmbedding[triplet[0]][i]*relationweight[triplet[1]][i];
			ttran[i]=entityEmbedding[triplet[2]][i]*relationweight[triplet[1]][i];
		}
		for(int i=0;i<entity_dim;i++)
		{
			resvector[i]=htran[i]+relationEmbedding[triplet[1]][i]-ttran[i];
		}
		return resvector;
	}
	
	protected List<Object> ListingEmbedding()
	{
		List<Object> embListing=new ArrayList<Object>();
		embListing.add(entityEmbedding);
		embListing.add(relationEmbedding);
		embListing.add(relationweight);
		return embListing;
	}
	protected List<Object> ListingGradient()
	{
		List<Object> graListing=new ArrayList<Object>();
		graListing.add(entityGradient);
		graListing.add(relationGradient);
		graListing.add(relationweightGradient);
		return graListing;
	}

	@Override
	public void SetSpecificParameterStream(SpecificParameter para)
	{
		TransHParameter ptransH=(TransHParameter)para;
		entity_dim=ptransH.getEntityDim();
		relation_dim=ptransH.getRelationDim();
	}
	

}

