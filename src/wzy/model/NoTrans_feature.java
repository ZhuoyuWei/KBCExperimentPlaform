package wzy.model;

import java.util.ArrayList;
import java.util.List;

import wzy.model.para.SpecificParameter;
import wzy.model.para.TransEParameter;
import wzy.tool.MatrixTool;

public class NoTrans_feature extends EmbeddingModel{
	
	private double[][] entityEmbedding;
	private double[][] relationEmbedding;
	private double[][] entityGradient;
	private double[][] relationGradient;
	private int entity_dim;
	private int relation_dim;
	
	private int[][][] tripletGraph;
	private boolean trainOrTest=true;
	
	public void InsertIntoGraph(int[][] triplets)
	{
		List<int[]>[] graph=new List[entityNum];
		for(int i=0;i<graph.length;i++)
		{
			graph[i]=new ArrayList<int[]>();
		}
		for(int i=0;i<triplets.length;i++)
		{
			graph[triplets[i][0]].add(triplets[i]);
			graph[triplets[i][2]].add(triplets[i]);
		}
		tripletGraph=new int[entityNum][][];
		for(int i=0;i<tripletGraph.length;i++)
		{
			tripletGraph[i]=graph[i].toArray(new int[0][]);
		}
	}
	
	
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
		
		InsertIntoGraph(triplets);
		
	}
	@Override
	protected void InitGradients()
	{
		entityGradient=new double[entityNum][entity_dim];
		relationGradient=new double[relationNum][relation_dim];
	}
	/**
	 * L1 similarity
	 */
	@Override
	public double CalculateSimilarity(int[] triplet)
	{

		double sum=0.;
		
		double[] hm;
		double[] tm;
		
		if(trainOrTest)
		{
			hm=this.FeatureRepresentEntity(triplet[0]);
			tm=this.FeatureRepresentEntity(triplet[2]);
		}
		else
		{
			hm=MatrixTool.CopyVector(entityEmbedding[triplet[0]]);
			tm=MatrixTool.CopyVector(entityEmbedding[triplet[1]]);
		}
		

		for(int i=0;i<entity_dim;i++)
		{
			sum+=(hm[i]+relationEmbedding[triplet[1]][i])
					*(tm[i]-relationEmbedding[triplet[1]][i]);
		}
		
		
		return -sum;
	}
	
	public double CalculateSimilarity(double[] hm,double[] tm,double[] rm)
	{

		double sum=0.;


		for(int i=0;i<entity_dim;i++)
		{
			sum+=(hm[i]+rm[i])*(tm[i]-rm[i]);
		}
		
		
		return -sum;
	}
	
	/**
	 * Specific for TransF, entities in a triplet are transform by its triplets List from train data set.
	 * It is only suitable for train process, because we can use feature list to reconstruct entityEmbedding
	 * after obtain the original embedding.
	 * @param entity
	 * @return
	 */
	private double[] FeatureRepresentEntity(int entity)
	{
		double[] res=new double[entity_dim];
		int[][] featureList=tripletGraph[entity];
		if(featureList.length==0)
			return res;
		for(int i=0;i<featureList.length;i++)
		{
			if(featureList[i][0]==entity)
			{
				for(int j=0;j<entity_dim;j++)
				{
					res[j]+=(entityEmbedding[featureList[i][2]][j]-relationEmbedding[featureList[i][1]][j]);
				}
			}
			else
			{
				for(int j=0;j<entity_dim;j++)
				{
					res[j]+=(entityEmbedding[featureList[i][0]][j]+relationEmbedding[featureList[i][1]][j]);
				}				
			}
		}
		for(int i=0;i<entity_dim;i++)
		{
			res[i]/=featureList.length;
		}
		return res;
	}
	
	/**
	 * L1 similarity
	 */
	@Override
	protected void CalculateGradient(int[] triplet)
	{
		int[] ftriplet=GenerateFalseTriplet(triplet);

		
		double[] true_hm=this.FeatureRepresentEntity(triplet[0]);
		double[] true_tm=this.FeatureRepresentEntity(triplet[2]);
		double[] false_hm=this.FeatureRepresentEntity(ftriplet[0]);
		double[] false_tm=this.FeatureRepresentEntity(ftriplet[2]);
		
		
		double truesimi=CalculateSimilarity(true_hm,true_tm,relationEmbedding[triplet[1]]);
		double falsesimi=CalculateSimilarity(false_hm,false_tm,relationEmbedding[ftriplet[1]]);
		

		if(truesimi+margin-falsesimi>0)
		{
			
			int[][] true_h_List=tripletGraph[triplet[0]];
			double rtrue_h_sum=true_h_List.length>0?1./true_h_List.length:0;	
			for(int i=0;i<true_h_List.length;i++)
			{
				if(true_h_List[i][0]==triplet[0])
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[true_h_List[i][2]][j]-=rtrue_h_sum*(true_tm[j]-relationEmbedding[triplet[1]][j]);
						relationGradient[true_h_List[i][1]][j]+=rtrue_h_sum*(true_tm[j]-relationEmbedding[triplet[1]][j]);
					}
				}
				else
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[true_h_List[i][0]][j]-=rtrue_h_sum*(true_tm[j]-relationEmbedding[triplet[1]][j]);
						relationGradient[true_h_List[i][1]][j]-=rtrue_h_sum*(true_tm[j]-relationEmbedding[triplet[1]][j]);
					}					
				}
			}
			for(int i=0;i<relation_dim;i++)
				relationGradient[triplet[1]][i]-=true_tm[i]-true_hm[i]-2*relationEmbedding[triplet[1]][i];
			
			int[][] true_t_List=tripletGraph[triplet[2]];
			double rtrue_t_sum=true_t_List.length>0?1./true_t_List.length:0;
			for(int i=0;i<true_t_List.length;i++)
			{
				if(true_t_List[i][0]==triplet[2])
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[true_t_List[i][2]][j]-=rtrue_t_sum*(true_hm[j]+relationEmbedding[triplet[1]][j]);
						relationGradient[true_t_List[i][1]][j]+=rtrue_t_sum*(true_hm[j]+relationEmbedding[triplet[1]][j]);
					}
				}
				else
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[true_t_List[i][0]][j]-=rtrue_t_sum*(true_hm[j]+relationEmbedding[triplet[1]][j]);
						relationGradient[true_t_List[i][1]][j]-=rtrue_t_sum*(true_hm[j]+relationEmbedding[triplet[1]][j]);
					}					
				}
			}			
			

			int[][] false_h_List=tripletGraph[ftriplet[0]];
			double rfalse_h_sum=false_h_List.length>0?1./false_h_List.length:0;	
			for(int i=0;i<false_h_List.length;i++)
			{
				if(false_h_List[i][0]==ftriplet[0])
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[false_h_List[i][2]][j]-=rfalse_h_sum*(false_tm[j]-relationEmbedding[ftriplet[1]][j]);
						relationGradient[false_h_List[i][1]][j]+=rfalse_h_sum*(false_tm[j]-relationEmbedding[ftriplet[1]][j]);
					}
				}
				else
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[false_h_List[i][0]][j]-=rfalse_h_sum*(false_tm[j]-relationEmbedding[ftriplet[1]][j]);
						relationGradient[false_h_List[i][1]][j]-=rfalse_h_sum*(false_tm[j]-relationEmbedding[ftriplet[1]][j]);
					}					
				}
			}
			for(int i=0;i<relation_dim;i++)
				relationGradient[ftriplet[1]][i]-=false_tm[i]-false_hm[i]-2*relationEmbedding[ftriplet[1]][i];
			
			int[][] false_t_List=tripletGraph[ftriplet[2]];
			double rfalse_t_sum=false_t_List.length>0?1./false_t_List.length:0;
			for(int i=0;i<false_t_List.length;i++)
			{
				if(false_t_List[i][0]==ftriplet[2])
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[false_t_List[i][2]][j]-=rfalse_t_sum*(false_hm[j]+relationEmbedding[ftriplet[1]][j]);
						relationGradient[false_t_List[i][1]][j]+=rfalse_t_sum*(false_hm[j]+relationEmbedding[ftriplet[1]][j]);
					}
				}
				else
				{
					for(int j=0;j<entity_dim;j++)
					{
						entityGradient[false_t_List[i][0]][j]-=rfalse_t_sum*(false_hm[j]+relationEmbedding[ftriplet[1]][j]);
						relationGradient[false_t_List[i][1]][j]-=rfalse_t_sum*(false_hm[j]+relationEmbedding[ftriplet[1]][j]);
					}					
				}
			}	
		
		}	

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
		project=true;//false;//true;//
		trainprintable=true;	

		Epoch=1000;
		minibranchsize=4800;
		gamma=0.001;
		margin=0.25;
		random_data_each_epoch=100000;
		bern=false;
		
		lammadaL1=0.;
		lammadaL2=5;
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
				for(int k=0;k<embeddings[i][j].length;k++)
				{
					//embeddings[i][j][k]-=lammadaL2*gamma*2*embeddings[i][j][k];
					embeddings[i][j][k]/=x;
				}
			}
		}
		
		
		
	}
	
}
