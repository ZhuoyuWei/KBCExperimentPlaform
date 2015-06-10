package wzy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import wzy.meta.TripletHash;
import wzy.model.para.SpecificParameter;
import wzy.model.para.TransEParameter;
import wzy.model.para.TransFParameter;
import wzy.tool.MatrixTool;

public class TransF extends EmbeddingModel {

	private double[][] entityEmbedding;
	private double[][] relationEmbedding;
	private double[][] entityGradient;
	private double[][] relationGradient;
	private int entity_dim;
	private int relation_dim;
	
	private List<int[]>[] tripletGraph;
	private boolean trainOrTest=true;
	
	private void InsertIntoGraph(int[][] triplets)
	{
		for(int i=0;i<triplets.length;i++)
		{
			tripletGraph[triplets[i][0]].add(triplets[i]);
			tripletGraph[triplets[i][2]].add(triplets[i]);
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
		
		tripletGraph=new List[entityNum];
		for(int i=0;i<entityNum;i++)
		{
			tripletGraph[i]=new ArrayList<int[]>();
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
		double[] resvector=CalculateTripletVector(triplet);
		return MatrixTool.VectorNorm1(resvector);
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
		List<int[]> featureList=tripletGraph[entity];
		if(featureList.size()==0)
			return res;
		for(int i=0;i<featureList.size();i++)
		{
			if(featureList.get(i)[0]==entity)
			{
				for(int j=0;j<entity_dim;j++)
				{
					res[j]+=(entityEmbedding[featureList.get(i)[2]][j]-relationEmbedding[featureList.get(i)[1]][j]);
				}
			}
			else
			{
				for(int j=0;j<entity_dim;j++)
				{
					res[j]+=(entityEmbedding[featureList.get(i)[0]][j]+relationEmbedding[featureList.get(i)[1]][j]);
				}				
			}
		}
		for(int i=0;i<entity_dim;i++)
		{
			res[i]/=featureList.size();
		}
		return res;
	}
	
	/**
	 * After train process, we need transform entity embedding, reconstruct them by their features in train data.
	 * After calling this function, we can use TransE's similarity function to calculate similarity for each tripelt in testing set.
	 */
	private void TransformEntityEmbedding()
	{
		double[][] embeddings=new double[entityNum][];
		for(int i=0;i<entityNum;i++)
		{
			embeddings[i]=FeatureRepresentEntity(i);
		}
		entityEmbedding=embeddings;
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
			List<int[]> hList=tripletGraph[triplet[0]];
			List<int[]> tList=tripletGraph[triplet[2]];
			for(int i=0;i<truevector.length;i++)
			{
				int sign=truevector[i]>0?1:-1;
				
				//for h(triplet[0]) in true triplet
				double hsize_1=hList.size()>0?1./hList.size():0;
				for(int j=0;j<hList.size();j++)
				{
					if(hList.get(j)[0]==triplet[0])
					{
						entityGradient[hList.get(j)[2]][i]+=hsize_1*sign;
						relationGradient[hList.get(j)[1]][i]-=hsize_1*sign;
					}
					else
					{
						entityGradient[hList.get(j)[0]][i]+=hsize_1*sign;
						relationGradient[hList.get(j)[1]][i]+=hsize_1*sign;						
					}
				}
				
				//for r(triplet[1])
				relationGradient[triplet[1]][i]+=sign;
				
				//for t(triplet[2]) in true triplet
				double tsize_1=tList.size()>0?1./tList.size():0;
				for(int j=0;j<tList.size();j++)
				{
					if(tList.get(j)[0]==triplet[2])
					{
						entityGradient[tList.get(j)[2]][i]-=tsize_1*sign;
						relationGradient[tList.get(j)[1]][i]+=tsize_1*sign;
					}
					else
					{
						entityGradient[tList.get(j)[0]][i]-=tsize_1*sign;
						relationGradient[tList.get(j)[1]][i]-=tsize_1*sign;						
					}
				}
			}
			
			hList=tripletGraph[ftriplet[0]];
			tList=tripletGraph[ftriplet[2]];
			for(int i=0;i<falsevector.length;i++)
			{
				int sign=falsevector[i]<0?1:-1;
				
				//for h(ftriplet[0]) in false ftriplet
				double hsize_1=hList.size()>0?1./hList.size():0;
				for(int j=0;j<hList.size();j++)
				{
					if(hList.get(j)[0]==ftriplet[0])
					{
						entityGradient[hList.get(j)[2]][i]+=hsize_1*sign;
						relationGradient[hList.get(j)[1]][i]-=hsize_1*sign;
					}
					else
					{
						entityGradient[hList.get(j)[0]][i]+=hsize_1*sign;
						relationGradient[hList.get(j)[1]][i]+=hsize_1*sign;						
					}
				}
				
				//for r(ftriplet[1])
				relationGradient[ftriplet[1]][i]+=sign;
				
				//for t(ftriplet[2]) in false ftriplet
				double tsize_1=tList.size()>0?1./tList.size():0;
				for(int j=0;j<tList.size();j++)
				{
					if(tList.get(j)[0]==ftriplet[2])
					{
						entityGradient[tList.get(j)[2]][i]-=tsize_1*sign;
						relationGradient[tList.get(j)[1]][i]+=tsize_1*sign;
					}
					else
					{
						entityGradient[tList.get(j)[0]][i]-=tsize_1*sign;
						relationGradient[tList.get(j)[1]][i]-=tsize_1*sign;						
					}
				}
			}			
		}	
	}
	/**
	 * Calculate the result vector, i.e., TransE like methods use this function to calculate h+r-t vector.
	 * Be careful of that an entity has no features, and its feature vector would be full of zero. 
	 * @param triplet
	 * @return
	 */
	private double[] CalculateTripletVector(int[] triplet)
	{
		
		double[] hm;
		double[] tm;
		if(trainOrTest)
		{
			hm=FeatureRepresentEntity(triplet[0]);
			tm=FeatureRepresentEntity(triplet[2]);
		}
		else
		{
			hm=MatrixTool.CopyVector(entityEmbedding[triplet[0]]);
			tm=MatrixTool.CopyVector(entityEmbedding[triplet[2]]);
		}
		double[] resvector=new double[entity_dim];
		for(int i=0;i<entity_dim;i++)
		{
			resvector[i]=hm[i]+relationEmbedding[triplet[1]][i]-tm[i];
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
		TransFParameter ptransF=(TransFParameter)para;
		entity_dim=ptransF.getEntityDim();
		relation_dim=ptransF.getRelationDim();
	}
	

	///////////////////
	@Override
	public void SetBestParameter()
	{
		L1regular=false;
		project=true;
		trainprintable=false;//true;	

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
	protected void PreTesting(int[][] test_triplets)
	{
		TransformEntityEmbedding();
		this.trainOrTest=false;
	}
	
	
}
