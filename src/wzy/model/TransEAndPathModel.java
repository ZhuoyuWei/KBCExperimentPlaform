package wzy.model;

import java.util.ArrayList;
import java.util.List;

import wzy.io.FileTools;
import wzy.meta.RPath;
import wzy.model.para.SpecificParameter;
import wzy.model.para.TransEParameter;
import wzy.tool.MatrixTool;

public class TransEAndPathModel extends EmbeddingModel{
	
	private double[][] entityEmbedding;
	private double[][] relationEmbedding;
	private double[][] entityGradient;
	private double[][] relationGradient;
	
	private int entity_dim;
	private int relation_dim;
	
	private RPath[][] rpathLists;
	private double[][][] pathEmbedding;
	private double[][] pathWeights;
	private double[][] pathWeightGradients;
	
	@Override
	protected void InitEmbeddingsMemory()
	{
		entityEmbedding=new double[entityNum][entity_dim];
		relationEmbedding=new double[relationNum][relation_dim];
		pathEmbedding=new double[relationNum][][];
		pathWeights=new double[relationNum][];
		for(int i=0;i<relationNum;i++)
		{
			pathEmbedding[i]=new double[rpathLists[i].length][relation_dim];
			pathWeights[i]=new double[rpathLists[i].length];
		}
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
	public void InitEmbeddingFromFile(String filename)
	{
		InitEmbeddingsMemory();
		List<Object> embeddingList=new ArrayList<Object>();
		embeddingList.add(entityEmbedding);
		embeddingList.add(relationEmbedding);
		FileTools.ReadEmbeddingsFromFile(filename, embeddingList);
		CalculatePathEmbedding();
		//Init path weights
		/*for(int i=0;i<pathWeights.length;i++)
		{
			for(int j=0;j<pathWeights[i].length;j++)
			{
				pathWeights[i][j]=rand.nextDouble();
			}
		}*/
	}
	
	/**
	 * We need read path file which is produced during constructing formulas. 
	 */
	@Override
	public void InitPathFromFile(String filename)
	{
		rpathLists=FileTools.ReadFormulasForRelations(filename, relationNum);
	}
	
	private void CalculatePathEmbedding()
	{
		for(int i=0;i<rpathLists.length;i++)
		{
			for(int j=0;j<rpathLists[i].length;j++)
			{
				for(int k=0;k<rpathLists[i][j].length();k++)
				{
					for(int h=0;h<relation_dim;h++)
					{
						pathEmbedding[i][j][h]+=relationEmbedding[rpathLists[i][j].getRelationList().get(k)][h];
					}
				}
			}
		}
	}
	
	@Override
	protected void InitGradients()
	{
		/*entityGradient=new double[entityNum][entity_dim];
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
		}*/
		
		
		pathWeightGradients=new double[pathWeights.length][];
		for(int i=0;i<pathWeights.length;i++)
		{
			pathWeightGradients[i]=new double[pathWeights[i].length];
		}
		
	}
	/**
	 * L1 similarity
	 */
	@Override
	public double CalculateSimilarity(int[] triplet)
	{
		double[] resvector=CalculateTripletVector(triplet);
		double sim=MatrixTool.VectorNorm1(resvector);
		
		double[][] pathE=pathEmbedding[triplet[1]];
		double[] pathW=pathWeights[triplet[1]];
		
		for(int i=0;i<pathE.length;i++)
		{
			double[] res=new double[entity_dim];
			for(int j=0;j<entity_dim;j++)
			{
				res[j]=entityEmbedding[triplet[0]][j]+pathE[i][j]-entityEmbedding[triplet[2]][j];
			}
			sim+=pathW[i]*MatrixTool.VectorNorm1(res);
		}
		
		return sim;
	}
	public double CalculateSimilarity(double[] tripletResVec,double[][] pathResVec,double[] weights)
	{
		double sim=MatrixTool.VectorNorm1(tripletResVec);
		for(int i=0;i<pathResVec.length;i++)
		{
			sim+=weights[i]*MatrixTool.VectorNorm1(pathResVec[i]);
		}
		return sim;
	}	
	public double CalculateSimilarity(double[] tripletResVec,double[] pathRes,double[] weights)
	{
		double sim=MatrixTool.VectorNorm1(tripletResVec);
		for(int i=0;i<pathRes.length;i++)
		{
			sim+=weights[i]*pathRes[i];
		}
		return sim;
	}		
	/**
	 * L1 similarity
	 */
	@Override
	protected void CalculateGradient(int[] triplet)
	{
		int[] ftriplet=GenerateFalseTriplet(triplet);

		double[] trueResVec=CalculateTripletVector(triplet);
		double[][] truePathResVecs=CalculatePathResVector(triplet);
		double[] trueNorm1=CalculatePathsSimilarity(truePathResVecs);
		
		double[] falseResVec=CalculateTripletVector(ftriplet);	
		double[][] falsePathResVecs=CalculatePathResVector(ftriplet);
		double[] falseNorm1=CalculatePathsSimilarity(falsePathResVecs);	
		
		double truesimi=CalculateSimilarity(trueResVec,trueNorm1,pathWeights[triplet[1]]);
		double falsesimi=CalculateSimilarity(falseResVec,falseNorm1,pathWeights[ftriplet[1]]);
		
		if(truesimi+margin-falsesimi>0)
		{
			for(int i=0;i<pathWeights[triplet[1]].length;i++)
			{
				//L2 regular
				pathWeightGradients[triplet[1]][i]+=trueNorm1[i]-falseNorm1[i]+2*lammadaL2*pathWeightGradients[triplet[1]][i];
			}
		}	
	}
	
	private double[][] CalculatePathResVector(int[] triplet)
	{
		
		double[][] pathE=pathEmbedding[triplet[1]];
		double[][] pathres=new double[pathE.length][relation_dim];
		
		for(int i=0;i<pathE.length;i++)
		{
			for(int j=0;j<entity_dim;j++)
			{
				pathres[i][j]=entityEmbedding[triplet[0]][j]+pathE[i][j]-entityEmbedding[triplet[2]][j];
			}
		}
		
		return pathres;
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
	
	private double[] CalculatePathsSimilarity(double[][] pathResVec)
	{
		double[] res=new double[pathResVec.length];
		for(int i=0;i<pathResVec.length;i++)
		{
			res[i]=MatrixTool.VectorNorm1(pathResVec[i]);
		}
		return res;
	}
	private double[] CalculateTripletVector(double[][] tri_emb)
	{
		double[] resvector=new double[entity_dim];
		for(int i=0;i<entity_dim;i++)
		{
			resvector[i]=tri_emb[0][i]+tri_emb[1][i]-tri_emb[2][i];
		}	
		return resvector;
	}
	
	protected List<Object> ListingEmbedding()
	{
		List<Object> embListing=new ArrayList<Object>();
		//embListing.add(entityEmbedding);
		//embListing.add(relationEmbedding);
		embListing.add(pathWeights);
		return embListing;
	}
	protected List<Object> ListingGradient()
	{
		List<Object> graListing=new ArrayList<Object>();
		//graListing.add(entityGradient);
		//graListing.add(relationGradient);
		graListing.add(pathWeightGradients);
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
		project=false;
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
		//none
	}



	
	//get methods
	public double[][][] getPathEmbedding() {
		return pathEmbedding;
	}

	public RPath[][] getRpathLists() {
		return rpathLists;
	}

	public double[][] getEntityEmbedding() {
		return entityEmbedding;
	}

	
}
