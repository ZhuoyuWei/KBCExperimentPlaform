package wzy.model.randwk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import wzy.meta.FormulaTreeNode;
import wzy.meta.RPath;
import wzy.meta.TripletHash;
import wzy.model.EmbeddingModel;
import wzy.model.RandomWalkModel;
import wzy.tool.MatrixTool;

public class RandomAttention extends RandomWalkModel{

	public int max_round=10;
	public Random rand=new Random();
	public double[][] entityEmbeddings;
	public double[][] relationEmbeddings;
	
	public double max_prior=0;
	public double lambda=4.5;
	
	public Map<TripletHash,Double> prior_map;
	
	//for learning
	public EmbeddingModel em_randwalk;
	public double[][] entityEmbGradients;
	public double[][] relationEmbGradients;
	
	//the target entity
	public int t;
	private double[] fcounts;
	public double[][] randt_emb;
	public double randt_norm2;
	
	boolean needgate=false;
	
	//for embedding updating
	boolean updateemb=true;
	int dim=50;
	boolean record_entity=true;
	List<Integer[]>[] path_records; 
	//List<Integer> dfs_stack;
	
	
 	
	@Override
	public void InitGradients()
	{
		super.InitGradients();
		entityEmbGradients=new double[entityEmbeddings.length][entityEmbeddings[0].length];
		relationEmbGradients=new double[relationEmbeddings.length][relationEmbeddings[0].length];
	}
	
	private boolean ShouldUpdate(double[] paths)
	{
		int count=0;
		for(int i=0;i<paths.length-1;i++)
			if(paths[i]>0)
				count++;
		return count>0;
	}
	
	@Override
	public void OneBranchTraining(int[][] train_triplets,int sindex,int eindex)
	{
		if(updateemb)
			InitGradients();
		else
			super.InitGradients();

		for(int i=sindex;i<=eindex;i++)
		{			
			double[] true_paths_count=RandomWalk(train_triplets[i]);
			if(!ShouldUpdate(true_paths_count))
				continue;
			train_true_count+=CheckRandomRes(true_paths_count);
			double true_f_wx=Logistic_F_wx(train_triplets[i][1],true_paths_count);
			Logistic_Grident(train_triplets[i][1],true_paths_count,true_f_wx-1);
			//calculate path attention gradients
			if(updateemb)
				CalculatePathEmbeddinigGradients(true_paths_count,train_triplets[i][1]
						,train_triplets[i][2],1);
			
			//false
			for(int j=0;j<false_triplet_size;j++)
			{
				int[] false_triplet=GenerateFalseTriplet(train_triplets[i]);
				double[] false_paths_count=RandomWalk(false_triplet);
				if(!ShouldUpdate(false_paths_count))
					continue;
				train_false_count+=CheckRandomRes(false_paths_count);
				double false_f_wx=Logistic_F_wx(false_triplet[1],false_paths_count);
				Logistic_Grident(false_triplet[1],false_paths_count,false_f_wx);
				
				//calculate path attention gradients, false
				if(updateemb)
					CalculatePathEmbeddinigGradients(false_paths_count,false_triplet[1],
							false_triplet[2],-1);
			}
		}
		if(updateemb)
			UpdateWeights();
		else
			super.UpdateWeights();
	}
	
	/**
	 * Calculate embedding gradients for relation and entity embeddings
	 * just simply use randt*next_state as the objective function to calculate gradients
	 * @param fcount
	 * @param rel
	 * @param tentity
	 * @param label
	 */
/*	public void CalculatePathEmbeddinigGradients(double[] fcount,int rel,int tentity,int label)
	{
		if(rpathLists==null||rpathLists[rel]==null)
			return;
		for(int i=0;i<rpathLists[rel].length;i++)
		{
			if(fcount[i]>1e-4)
			{
				//System.out.println(rpathLists.length+"\t"+rel+"\t"+rpathLists[rel].length+"\t"+i);
				RPath path=rpathLists[rel][i];
				for(int j=0;j<path.length();j++)
				{
					for(int k=0;k<dim;k++)
					{
						relationEmbGradients[path.GetElement(j)][k]-=relationEmbeddings[rel][k]*label;
						relationEmbGradients[rel][k]-=relationEmbeddings[path.GetElement(j)][k]*label;	
					}
					if(record_entity&&path_records!=null&&path_records[i]!=null)
					{
						for(int h=0;h<path_records[i].size();h++)
						{
							int entityIndex=path_records[i].get(h)[j];
							for(int k=0;k<dim;k++)
							{
								entityEmbGradients[entityIndex][k]-=entityEmbeddings[tentity][k]*label;
								entityEmbGradients[tentity][k]-=entityEmbeddings[entityIndex][k]*label;								
							}
						}
					}
				}
				
			}
			//false sampling
			else
			{
				RPath path=rpathLists[rel][i];
				for(int j=0;j<path.length();j++)
				{
					for(int k=0;k<dim;k++)
					{
						relationEmbGradients[path.GetElement(j)][k]+=relationEmbeddings[rel][k]*label;
						relationEmbGradients[rel][k]+=relationEmbeddings[path.GetElement(j)][k]*label;	
					}
					if(record_entity&&path_records!=null&&path_records[i]!=null)
					{
						for(int h=0;h<path_records[i].size();h++)
						{
							int entityIndex=path_records[i].get(h)[j];
							for(int k=0;k<dim;k++)
							{
								entityEmbGradients[entityIndex][k]+=entityEmbeddings[tentity][k]*label;
								entityEmbGradients[tentity][k]+=entityEmbeddings[entityIndex][k]*label;								
							}
						}
					}
				}
			}
		}
		
		
	}*/
	
	public void CalculatePathEmbeddinigGradients(double[] fcount,int rel,int tentity,int label)
	{
		
		if(rpathLists==null||rpathLists[rel]==null)
			return;
		global_update_count++;
		for(int i=0;i<rpathLists[rel].length;i++)
		{
			if(fcount[i]>1e-4)
			{
				//System.out.println(rpathLists.length+"\t"+rel+"\t"+rpathLists[rel].length+"\t"+i);
				RPath path=rpathLists[rel][i];
				for(int j=0;j<path.length();j++)
				{
					if(record_entity&&path_records!=null&&path_records[i]!=null)
					{
						for(int h=0;h<path_records[i].size();h++)
						{
							int entityIndex=path_records[i].get(h)[j];
							
							double[][] tmp_next_state_emb=new double[2][];
							tmp_next_state_emb[0]=relationEmbeddings[path.GetElement(j)];
							tmp_next_state_emb[1]=entityEmbeddings[entityIndex];
							
							
							double next_state_norm2=MatrixTool.MatrixNorm2(tmp_next_state_emb);
							double sim_rt_nextstate=MatrixTool.MatrixCosSim(
									MatrixTool.DotMulti(randt_emb, tmp_next_state_emb), randt_norm2
									, next_state_norm2);
							
							for(int k=0;k<dim;k++)
							{								
								relationEmbGradients[path.GetElement(j)][k]+=
										CosSimGradients(relationEmbeddings[path.GetElement(j)][k],randt_emb[0][k]
										,next_state_norm2,randt_norm2,sim_rt_nextstate)*label;
								entityEmbGradients[entityIndex][k]+=
										CosSimGradients(entityEmbeddings[entityIndex][k],randt_emb[1][k]
										,next_state_norm2,randt_norm2,sim_rt_nextstate)*label;
								
								relationEmbGradients[rel][k]+=
										CosSimGradients(randt_emb[0][k],relationEmbeddings[path.GetElement(j)][k]
										,randt_norm2,next_state_norm2,sim_rt_nextstate)*label;
								entityEmbGradients[tentity][k]+=
										CosSimGradients(randt_emb[1][k],entityEmbeddings[entityIndex][k]
										,randt_norm2,next_state_norm2,sim_rt_nextstate)*label;															
							}
						}
					}
					else
					{
					}
				}
				
			}
			//false sampling
			else
			{
				RPath path=rpathLists[rel][i];
				for(int j=0;j<path.length();j++)
				{
					if(record_entity&&path_records!=null&&path_records[i]!=null)
					{
						for(int h=0;h<path_records[i].size();h++)
						{
							int entityIndex=path_records[i].get(h)[j];
							
							double[][] tmp_next_state_emb=new double[2][];
							tmp_next_state_emb[0]=relationEmbeddings[path.GetElement(j)];
							tmp_next_state_emb[1]=entityEmbeddings[entityIndex];
							
							
							double next_state_norm2=MatrixTool.MatrixNorm2(tmp_next_state_emb);
							double sim_rt_nextstate=MatrixTool.MatrixCosSim(
									MatrixTool.DotMulti(randt_emb, tmp_next_state_emb), randt_norm2
									, next_state_norm2);
							
							for(int k=0;k<dim;k++)
							{								
								relationEmbGradients[path.GetElement(j)][k]-=
										CosSimGradients(relationEmbeddings[path.GetElement(j)][k],randt_emb[0][k]
										,next_state_norm2,randt_norm2,sim_rt_nextstate)*label;
								entityEmbGradients[entityIndex][k]-=
										CosSimGradients(entityEmbeddings[entityIndex][k],randt_emb[1][k]
										,next_state_norm2,randt_norm2,sim_rt_nextstate)*label;
								
								relationEmbGradients[rel][k]-=
										CosSimGradients(randt_emb[0][k],relationEmbeddings[path.GetElement(j)][k]
										,randt_norm2,next_state_norm2,sim_rt_nextstate)*label;
								entityEmbGradients[tentity][k]-=
										CosSimGradients(randt_emb[1][k],entityEmbeddings[entityIndex][k]
										,randt_norm2,next_state_norm2,sim_rt_nextstate)*label;															
							}
						}
					}
					else
					{
					}
				}				
			}
		}
		
	}
	public double CosSimGradients(double x,double y,double xnorm,double ynorm,double sim)
	{
		
		double t_x_norm2=1./xnorm;
		double t_y_norm2=1./ynorm;		
		
		double res=t_x_norm2*t_x_norm2;
		res*=x;
		res*=sim;
		
		res-=(t_x_norm2*t_y_norm2*y);
		
		return res;
	}
	
	@Override
	public double[] RandomWalk(int[] triplet)
	{
		fcounts=super.RandomWalk(triplet);

		t=triplet[2];
		randt_emb=new double[2][];
		randt_emb[0]=relationEmbeddings[triplet[1]];
		randt_emb[1]=entityEmbeddings[triplet[2]];
		
		randt_norm2=MatrixTool.MatrixNorm2(randt_emb);
		
		
		if(record_entity)
		{
			path_records=new List[fcounts.length];
			for(int i=0;i<path_records.length;i++)
				path_records[i]=new ArrayList<Integer[]>();
		}
		
		//List<int[]> path_record=new ArrayList<int[]>();
		for(int r=0;r<max_round;r++)
		{
			int state=triplet[0];
			
			FormulaTreeNode ft_node=ff[triplet[1]].root;
			if(record_entity)
			{
				List<Integer> statelink=new ArrayList<Integer>();
				statelink.add(triplet[0]);
				DFS(state,ft_node,statelink);
			}
			else
			{
				DFS(state,ft_node,null);
			}
		}
		
		
		
		return fcounts;
	}
	

	
	protected void DFS(int s,FormulaTreeNode fnode,List<Integer> statelink)
	{	
		List<Integer> stateList=new ArrayList<Integer>();
		List<FormulaTreeNode> formulaList=new ArrayList<FormulaTreeNode>();
		List<int[]> nextrt=new ArrayList<int[]>();
		for(int j=0;j<triplet_graph[s].length;j++)
		{
			if(fnode.next_map[triplet_graph[s][j][0]]!=null)
			{
				if(fnode.next_map[triplet_graph[s][j][0]].leaf&&triplet_graph[s][j][1]==t)
				{
					fcounts[fnode.next_map[triplet_graph[s][j][0]].formula]++;
					if(statelink!=null)
					{
						statelink.add(t);
						path_records[fnode.next_map[triplet_graph[s][j][0]].formula]
								.add(statelink.toArray(new Integer[0]));
						statelink.remove(statelink.size()-1);
					}
				}
				else if(fnode.next_map[triplet_graph[s][j][0]].next_map!=null)
				{
					//DFS(triplet_graph[s][j][1],fnode.next_map[triplet_graph[s][j][0]]);
					stateList.add(triplet_graph[s][j][1]);
					formulaList.add(fnode.next_map[triplet_graph[s][j][0]]);
					nextrt.add(triplet_graph[s][j]);
				}
			}
		}
		if(stateList.size()>0)
		{
			double[] provector=CalAttentionSimilarity(nextrt);
			int index=0;
			double rate=rand.nextDouble();
			for(int i=0;i<provector.length;i++)
			{
				if(rate<provector[i])
				{
					index=i;
					break;
				}
			}
			if(statelink!=null)
			{
				statelink.add(stateList.get(index));
			}	
			DFS(stateList.get(index),formulaList.get(index),statelink);
			if(statelink!=null)
			{
				statelink.remove(statelink.size()-1);
			}				
		}
			
	}
	
	protected double[] CalAttentionSimilarity(List<int[]> nextrt)
	{
		double[] res=new double[nextrt.size()];
		double sum=0;
		for(int i=0;i<res.length;i++)
		{
			if(needgate)
			{}
			else
			{
				double[][] candemb=new double[2][];
				candemb[0]=relationEmbeddings[nextrt.get(i)[0]];
				candemb[1]=entityEmbeddings[nextrt.get(i)[1]];
				//res[i]=MatrixTool.DotMulti(randtemb, candemb);
				//res[i]=MatrixTool.VectorDot(randtemb[0], candemb[0]);
				if(record_entity)
					res[i]=MatrixTool.MatrixCosSim(randt_emb, candemb);
				else
					res[i]=MatrixTool.VectorCosSim(randt_emb[0], candemb[0]);
				//sum+=res[i];
			}
		}
		
		for(int i=0;i<res.length;i++)
		{
			/*if(res[i]<0)
				res[i]=0;*/
			res[i]=Math.exp(res[i]);
			sum+=res[i];
		}
		
		for(int i=0;i<res.length;i++)
		{
			res[i]/=sum;
		}
		
		for(int i=1;i<res.length;i++)
		{
			res[i]+=res[i-1];
		}
		
		
		return res;
	}
	

	
	
	
	@Override
	public double Logistic_F_wx(int r,double[] fcounts)
	{
		return super.Logistic_F_wx(r, fcounts);
	}
	
	public void BuildPriorMap(int[][] train_triplets,int[][] valid_triplets,Class<?> Emclass)
	{
		prior_map=new HashMap<TripletHash,Double>();
		int[][][] triplets=new int[2][][];
		triplets[0]=train_triplets;
		triplets[1]=valid_triplets;
		
		
		
		try {
			em=(EmbeddingModel)Emclass.newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//double[] tmp_score=new double[triplets[0].length+triplets[1].length];
		List<TripletHash> tmp_tri_list=new ArrayList<TripletHash>(triplets[0].length+triplets[1].length);
		List<Double> tmp_score_list=new ArrayList<Double>(triplets[0].length+triplets[1].length);
		for(int i=0;i<2;i++)
		{
			for(int j=0;j<triplets[i].length;j++)
			{
				double val=em.CalculateSimilarity(triplets[i][j]);
				TripletHash tri=new TripletHash();
				tri.setTriplet(triplets[i][j]);
				//prior_map.put(tri, val);
				tmp_tri_list.add(tri);
				tmp_score_list.add(val);
				if(val<max_prior)
				{
					max_prior=val;
				}
			}
		}
		
		for(int i=0;i<tmp_tri_list.size();i++)
		{
			double val=4.5*tmp_score_list.get(i)/max_prior+1;
			prior_map.put(tmp_tri_list.get(i), val);
		}
		
	}
	
	public void DeepCopyEmbedding(List<Object> listembedding)
	{
		entityEmbeddings=MatrixTool.CopyMatrix((double[][])listembedding.get(0));
		relationEmbeddings=MatrixTool.CopyMatrix((double[][])listembedding.get(1));
	}
	
	public void ShallowCopyEmbedding(List<Object> listembedding)
	{
		entityEmbeddings=(double[][])listembedding.get(0);
		relationEmbeddings=(double[][])listembedding.get(1);
	}
	
	public void DeepCopyEM(EmbeddingModel source)
	{
		em_randwalk=source.CopySeft();
	}
	public void ShallowCopyEm(EmbeddingModel source)
	{
		em_randwalk=source;
	}
	
	private int CheckGradientsNot0(double[][] gradients)
	{
		int count=0;
		for(int i=0;i<gradients.length;i++)
		{
			if(Math.abs(gradients[i][0])>1e-4)
				count++;
		}
		return count;
	}
	@Override
	public void UpdateWeights()
	{
		//path gradients
		for(int i=0;i<pathWeightGradients.length;i++)
		{
			for(int j=0;j<pathWeightGradients[i].length;j++)
			{
				pathWeights[i][j]-=learning_rate*pathWeightGradients[i][j];
			}
		}
		
		//relation gradients
		
		//use embedding
		List<Object> tmpGradientList=new ArrayList<Object>();
		
		tmpGradientList.add(entityEmbGradients);
		//if(isdebuging)
			//System.out.println(CheckGradientsNot0(entityEmbGradients));
		tmpGradientList.add(relationEmbGradients);
		//if(isdebuging)		
			//System.out.println(CheckGradientsNot0(relationEmbGradients));

		/*List<Object> tmp=new ArrayList<Object>();
		tmp.add(em_randwalk.ListingEmbedding_public().get(0));
		tmp.add(em_randwalk.ListingEmbedding_public().get(1));	
		tmp.add(em_randwalk.ListingEmbedding_public().get(0));		*/
		
		em_randwalk.UpgradeGradients_public(em_randwalk.ListingEmbedding_public(), tmpGradientList);
		
		
		//this.CompareDiff(em.ListingEmbedding_public(), em_randwalk.ListingEmbedding_public());
		
		//norm l1 ball projecting
		if(project)
		{
			for(int i=0;i<pathWeights.length;i++)
			{
				double norm=MatrixTool.VectorNorm1(pathWeights[i]);
				if(norm>1)
				{
					for(int j=0;j<pathWeights[i].length;j++)
					{
						pathWeights[i][j]/=norm;
					}
				}
				if(emb_force_1) // 
				{
					pathWeights[i][pathWeights[i].length-1]=1.;
				}
			}	
			em_randwalk.BallProjecting_public(em_randwalk.ListingEmbedding_public());
		}
		
		//l1-norm
		if(true)
		{
			for(int i=0;i<pathWeights.length;i++)
			{
				for(int j=0;j<pathWeights[i].length;j++)
				{
					if(Math.abs(pathWeights[i][j])<1e-4)
						pathWeights[i][j]=0;
				}
			}			
		}
		
	}	
	

	
}
