package wzy.model;

import java.util.ArrayList;
import java.util.List;

import wzy.meta.TripletHash;
import wzy.model.para.SpecificParameter;
import wzy.model.para.TransRParameter;
import wzy.tool.MatrixTool;

public class TransR extends EmbeddingModel{
	private double[][] entityEmbedding;
	private double[][] relationEmbedding;
	private double[][][] relationweight;
	private double[][] entityGradient;
	private double[][] relationGradient;
	private double[][][] relationweightGradient;
	private int entity_dim;
	private int relation_dim;
	
	
	@Override
	public void InitEmbeddingsRandomly()
	{
		entityEmbedding=new double[entityNum][entity_dim];
		relationEmbedding=new double[relationNum][relation_dim];
		relationweight=new double[relationNum][entity_dim][relation_dim];
		for(int i=0;i<entityNum;i++)
		{
			for(int j=0;j<entity_dim;j++)
			{
				entityEmbedding[i][j]=rand.nextDouble();
				//if(rand.nextDouble()<0.5)
					//entityEmbedding[i][j]=-entityEmbedding[i][j];
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
				//if(rand.nextDouble()<0.5)
					//relationEmbedding[i][j]=-relationEmbedding[i][j];
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
		for(int i=0;i<relationweight.length;i++)
		{
			double x=0;
			for(int j=0;j<relationweight[i].length;j++)
			{
				for(int k=0;k<relationweight[i][j].length;k++)
				{
					relationweight[i][j][k]=rand.nextDouble();
					//if(rand.nextDouble()<0.5)
						//relationweight[i][j][k]=-relationweight[i][j][k];
					x+=Math.abs(relationweight[i][j][k]);
				}
			}
			if(x>1)
			{
				for(int j=0;j<relationweight[i].length;j++)
				{
					for(int k=0;k<relationweight[i][j].length;k++)
					{
						relationweight[i][j][k]/=x;
					}
				}				
			}
		}
	}
	@Override
	protected void InitGradients()
	{
		entityGradient=new double[entityNum][entity_dim];
		relationGradient=new double[relationNum][relation_dim];
		relationweightGradient=new double[relationNum][entity_dim][relation_dim];
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
	
	private double[] CalculateTripletVector(int[] triplet)
	{
		
		double[] resvector=new double[relation_dim];
		double[] hm=new double[relation_dim];
		double[] tm=new double[relation_dim];
		
		for(int i=0;i<relation_dim;i++)
		{
			for(int j=0;j<entity_dim;j++)
			{
				hm[i]+=entityEmbedding[triplet[0]][j]*relationweight[triplet[1]][j][i];
				tm[i]+=entityEmbedding[triplet[2]][j]*relationweight[triplet[1]][j][i];
			}
		}
		for(int i=0;i<entity_dim;i++)
		{
			resvector[i]=hm[i]+relationEmbedding[triplet[1]][i]-tm[i];
		}
		return resvector;
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
			for(int i=0;i<truevector.length;i++)  //vector has the same dimensionality of relation_dim
			{
				if(truevector[i]>0)
				{
					//entityGradient[triplet[0]][i]+=1;
					
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[triplet[0]][j]+=relationweight[triplet[1]][j][i];
						entityGradient[triplet[2]][j]-=relationweight[triplet[1]][j][i];	
						relationweightGradient[triplet[1]][j][i]+=entityGradient[triplet[0]][j]-entityGradient[triplet[2]][j];
					}			
					relationGradient[triplet[1]][i]+=1;
				}
				else
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[triplet[0]][j]-=relationweight[triplet[1]][j][i];
						entityGradient[triplet[2]][j]+=relationweight[triplet[1]][j][i];	
						relationweightGradient[triplet[1]][j][i]-=entityGradient[triplet[0]][j]-entityGradient[triplet[2]][j];
					}			
					relationGradient[triplet[1]][i]-=1;				
				}
			}
			for(int i=0;i<falsevector.length;i++)
			{
				if(falsevector[i]<0)
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[ftriplet[0]][j]+=relationweight[ftriplet[1]][j][i];
						entityGradient[ftriplet[2]][j]-=relationweight[ftriplet[1]][j][i];	
						relationweightGradient[ftriplet[1]][j][i]+=entityGradient[ftriplet[0]][j]-entityGradient[ftriplet[2]][j];
					}			
					relationGradient[ftriplet[1]][i]+=1;
				}
				else
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[ftriplet[0]][j]-=relationweight[ftriplet[1]][j][i];
						entityGradient[ftriplet[2]][j]+=relationweight[ftriplet[1]][j][i];	
						relationweightGradient[ftriplet[1]][j][i]-=entityGradient[ftriplet[0]][j]-entityGradient[ftriplet[2]][j];
					}			
					relationGradient[ftriplet[1]][i]-=1;						
				}
			}			
		}	
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
		TransRParameter ptransR=(TransRParameter)para;
		entity_dim=ptransR.getEntityDim();
		relation_dim=ptransR.getRelationDim();
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
}
