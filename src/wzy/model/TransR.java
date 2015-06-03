package wzy.model;

import java.util.ArrayList;
import java.util.List;

import wzy.io.FileTools;
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
	public void InitEmbeddingsMemory()
	{
		entityEmbedding=new double[entityNum][entity_dim];
		relationEmbedding=new double[relationNum][relation_dim];
		relationweight=new double[relationNum][entity_dim][relation_dim];		
	}
	
	@Override
	public void InitEmbeddingFromFile(String filename)
	{
		InitEmbeddingsMemory();
		List<Object> embeddingList=new ArrayList<Object>();
		embeddingList.add(entityEmbedding);
		embeddingList.add(relationEmbedding);
		FileTools.ReadEmbeddingsFromFile(filename, embeddingList);
		for(int i=0;i<relationweight.length;i++)
		{
			int mindim=entity_dim<relation_dim?entity_dim:relation_dim;
			for(int j=0;j<mindim;j++)
			{
				relationweight[i][j][j]=1;
			}
		}
	}
	
	@Override
	public void InitEmbeddingsRandomly()
	{
		InitEmbeddingsMemory();
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
			int mindim=entity_dim<relation_dim?entity_dim:relation_dim;
			for(int j=0;j<mindim;j++)
			{
				relationweight[i][j][j]=1;
			}
		}
		
		/*
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
		}*/
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
						relationweightGradient[triplet[1]][j][i]+=entityEmbedding[triplet[0]][j]-entityEmbedding[triplet[2]][j];
					}			
					relationGradient[triplet[1]][i]+=1;
				}
				else
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[triplet[0]][j]-=relationweight[triplet[1]][j][i];
						entityGradient[triplet[2]][j]+=relationweight[triplet[1]][j][i];	
						relationweightGradient[triplet[1]][j][i]-=entityEmbedding[triplet[0]][j]-entityEmbedding[triplet[2]][j];
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
						relationweightGradient[ftriplet[1]][j][i]+=entityEmbedding[ftriplet[0]][j]-entityEmbedding[ftriplet[2]][j];
					}			
					relationGradient[ftriplet[1]][i]+=1;
				}
				else
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[ftriplet[0]][j]-=relationweight[ftriplet[1]][j][i];
						entityGradient[ftriplet[2]][j]+=relationweight[ftriplet[1]][j][i];	
						relationweightGradient[ftriplet[1]][j][i]-=entityEmbedding[ftriplet[0]][j]-entityEmbedding[ftriplet[2]][j];
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
	}
	
	@Override
	protected void RegularEmbedding(int[][] train_triplets,int sindex,int eindex)
	{
		List<Object> embList=new ArrayList<Object>();
		embList.add(entityEmbedding);
		embList.add(relationEmbedding);
		BallProjecting(embList);
		
		for(int i=0;i<this.relationweight.length;i++)
		{
			for(int j=0;j<relation_dim;j++)
			{
				double x=0.;
				for(int k=0;k<entity_dim;k++)
				{
					x+=Math.pow(relationweight[i][k][j], 2.);
				}
				x=Math.sqrt(x);
				if(x>1.)
				{
					for(int k=0;k<entity_dim;k++)
						relationweight[i][k][j]/=x;
				}
			}
		}
		//CutRelationWeigh(train_triplets,sindex,eindex);
	}
	
	private void CutRelationWeigh(int[][] train_triplets,int sindex,int eindex)
	{
		while(true)
		{
			int count=0;
			for(int i=sindex;i<=eindex;i++)
			{
				int[] entity=new int[2];
				entity[0]=train_triplets[i][0];
				entity[1]=train_triplets[i][2];		
				for(int j=0;j<2;j++)
				{
					double x=0.;
					for(int k=0;k<relation_dim;k++)
					{
						double t=0;
						for(int h=0;h<entity_dim;h++)
						{
							t+=entityEmbedding[entity[j]][h]*relationweight[train_triplets[i][1]][h][k];
						}
						x+=t*t;
					}
					x=Math.sqrt(x);
					if(x>1)
					{
						count++;
						
						for(int k=0;k<relation_dim;k++)
						{
							double t=0;
							for(int h=0;h<entity_dim;h++)
							{
								t+=entityEmbedding[entity[j]][h]*relationweight[train_triplets[i][1]][h][k];
							}
							
							for(int h=0;h<entity_dim;h++)
							{
								relationweight[train_triplets[i][1]][h][k]-=gamma*t*entityEmbedding[entity[j]][h];
								entityEmbedding[entity[j]][h]-=gamma*t*relationweight[train_triplets[i][1]][h][k];
							}							
						}
					}
				}
			}
			if(count<=0)
				break;
		}
	}
	
}
