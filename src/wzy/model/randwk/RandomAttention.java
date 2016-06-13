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

	//public int max_round=10;
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
	public int tentity;
	public int trel;
	private double[] fcounts;
	public double[][] randt_emb;
	public double randt_norm2;
	
	boolean needgate=false;
	
	//for embedding updating
	boolean updateemb=true;
	public static int dim=50;
	boolean record_entity=true;
	List<Integer[]>[] path_records; 
	//List<Integer> dfs_stack;
	boolean weighted=false;
	boolean punish_path=true;
	
	boolean normalization=true;
 	
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
			double[] true_paths_count=RandomWalk(train_triplets[i],true);
			if(!ShouldUpdate(true_paths_count))
				continue;
			global_update_count++;
			train_true_count+=CheckRandomRes(true_paths_count);
			double true_f_wx=Logistic_F_wx(train_triplets[i][1],true_paths_count);
			Logistic_Grident(train_triplets[i][1],true_paths_count,true_f_wx-1);
			//calculate path attention gradients
			/*if(updateemb)
				CalculatePathEmbeddinigGradients(true_paths_count,train_triplets[i][1]
						,train_triplets[i][2],1);*/
			
			//false
			for(int j=0;j<false_triplet_size;j++)
			{
				int[] false_triplet=GenerateFalseTriplet(train_triplets[i]);
				double[] false_paths_count=RandomWalk(false_triplet,true);
				if(!ShouldUpdate(false_paths_count))
					continue;
				
				train_false_count+=CheckRandomRes(false_paths_count);
				double false_f_wx=Logistic_F_wx(false_triplet[1],false_paths_count);
				Logistic_Grident(false_triplet[1],false_paths_count,false_f_wx);
				
				//calculate path attention gradients, false
				/*if(updateemb)
					CalculatePathEmbeddinigGradients(false_paths_count,false_triplet[1],
							false_triplet[2],-1);*/
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
	public double[] RandomWalk(int[] triplet,boolean training)
	{
		fcounts=super.RandomWalk(triplet,training);

		trel=triplet[1];
		tentity=triplet[2];
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
				List<Integer> relationlink=new ArrayList<Integer>();
				List<Double> rateList=new ArrayList<Double>();
				DFS(state,ft_node,statelink,relationlink,training,rateList);
			}
			else
			{
				DFS(state,ft_node,null,null,training,null);
			}
		}
		
		/*if(normalization)
		{
			for(int i=0;i<fcounts.length;i++)
				fcounts[i]+=0.1;			
			double sum=0.;
			for(int i=0;i<fcounts.length;i++)
				sum+=fcounts[i];
			for(int i=0;i<fcounts.length;i++)
				fcounts[i]/=sum;
		}*/
		
		return fcounts;
	}
	

	
	protected void DFS(int s,FormulaTreeNode fnode,List<Integer> statelink,List<Integer> relationlink
			,boolean training,List<Double> rateList)
	{	
		List<Integer> stateList=new ArrayList<Integer>();
		List<FormulaTreeNode> formulaList=new ArrayList<FormulaTreeNode>();
		List<int[]> nextrt=new ArrayList<int[]>();
		int pen_count=0;
		for(int j=0;j<triplet_graph[s].length;j++)
		{
			if(fnode.next_map[triplet_graph[s][j][0]]!=null)
			{
				if(fnode.next_map[triplet_graph[s][j][0]].leaf)
				{
					if(triplet_graph[s][j][1]==tentity)
					{
						fcounts[fnode.next_map[triplet_graph[s][j][0]].formula]++;
					
						if(statelink!=null)
						{
							statelink.add(tentity);
							
							//path_records[fnode.next_map[triplet_graph[s][j][0]].formula]
									//.add(statelink.toArray(new Integer[0]));
							//Calculate embedding gradient at here, change by wzy at 5.21
							relationlink.add(fnode.next_map[triplet_graph[s][j][0]].rel);
							if(training)
								OnePathGradient(statelink.toArray(new Integer[0])
										,relationlink.toArray(new Integer[0]),rateList.toArray(new Double[0]),1);
							relationlink.remove(relationlink.size()-1);
							
							
							statelink.remove(statelink.size()-1);
						}
						return;
					}
					else
					{
						if(punish_path)
						{
							//rpathLists[rel][fnode.next_map[triplet_graph[s][j][0]].formula].
							statelink.add(triplet_graph[s][j][1]);
							relationlink.add(fnode.next_map[triplet_graph[s][j][0]].rel);
							if(training)
								OnePathGradient(statelink.toArray(new Integer[0])
										,relationlink.toArray(new Integer[0]),rateList.toArray(new Double[0]),0);
							relationlink.remove(relationlink.size()-1);
							statelink.remove(statelink.size()-1);							
						}
					}
				}
				
				else if(fnode.next_map[triplet_graph[s][j][0]].next_map!=null)
				{
					//DFS(triplet_graph[s][j][1],fnode.next_map[triplet_graph[s][j][0]]);
					stateList.add(triplet_graph[s][j][1]);
					formulaList.add(fnode.next_map[triplet_graph[s][j][0]]);
					nextrt.add(triplet_graph[s][j]);
				}
				else
				{
					pen_count--;
				}
				pen_count++;
			}
		}
		if(stateList.size()>0)
		{
			double[] provector=CalAttentionSimilarity(nextrt);
			//train_standard_vars+=MatrixTool.Vector_StandardVar(provector);
			if(debug_print_once)
			{
				debug_print_once=false;
				System.out.println(provector[0]+"\t"+provector[1]);
			}
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
			if(relationlink!=null)
			{
				relationlink.add(formulaList.get(index).rel);
			}
			if(rateList!=null)
			{
				if(index==0)
					rateList.add(provector[0]);
				else
					rateList.add(provector[index]-provector[index-1]);
					
			}
			DFS(stateList.get(index),formulaList.get(index),statelink,relationlink,training,rateList);
			if(rateList!=null)
			{
				rateList.remove(rateList.size()-1);
			}
			if(relationlink!=null)
			{
				relationlink.remove(relationlink.size()-1);
			}
			if(statelink!=null)
			{
				statelink.remove(statelink.size()-1);
			}				
		}
		
		if(pen_count<=0)
		{
			if(punish_path)
			{
				if(training)
					OnePathGradient(statelink.toArray(new Integer[0])
							,relationlink.toArray(new Integer[0]),rateList.toArray(new Double[0]),0);
			}
		}
			
	}
	

	
/*	protected void OnePathGradient(Integer[] entityList,Integer[] relationList,int label)
	{
		if(entityList.length!=relationList.length+1)
		{
			System.err.println("entity list and relation list have different sizes "
					+entityList.length+"\t"+relationList.length);
			System.exit(-1);
		}
		for(int i=0;i<relationList.length;i++)
		{
			double[][] tmp_next_state_emb=new double[2][];
			tmp_next_state_emb[0]=relationEmbeddings[relationList[i]];
			tmp_next_state_emb[1]=entityEmbeddings[entityList[i+1]];

			double next_state_norm2=MatrixTool.MatrixNorm2(tmp_next_state_emb);
			double sim_rt_nextstate=MatrixTool.MatrixCosSim(
					MatrixTool.DotMulti(randt_emb, tmp_next_state_emb), randt_norm2
					, next_state_norm2);
			
			for(int k=0;k<dim;k++)
			{								
				try{
				relationEmbGradients[relationList[i]][k]+=
						CosSimGradients(relationEmbeddings[relationList[i]][k],randt_emb[0][k]
						,next_state_norm2,randt_norm2,sim_rt_nextstate)*label;
				}catch(Exception e)
				{
					System.out.println("debug");
					System.out.println(relationEmbGradients==null);
					System.out.println(relationList==null);			
					System.out.println(randt_emb==null);	
					System.out.println(relationEmbeddings==null);			
					e.printStackTrace();
					System.exit(-1);
				}
				entityEmbGradients[entityList[i+1]][k]+=
						CosSimGradients(entityEmbeddings[entityList[i+1]][k],randt_emb[1][k]
						,next_state_norm2,randt_norm2,sim_rt_nextstate)*label;
				
				relationEmbGradients[trel][k]+=
						CosSimGradients(randt_emb[0][k],relationEmbeddings[relationList[i]][k]
						,randt_norm2,next_state_norm2,sim_rt_nextstate)*label;
				entityEmbGradients[tentity][k]+=
						CosSimGradients(randt_emb[1][k],entityEmbeddings[entityList[i+1]][k]
						,randt_norm2,next_state_norm2,sim_rt_nextstate)*label;															
			}
		}
	}*/
	
	//paper objective
	/*protected void OnePathGradient(Integer[] entityList,Integer[] relationList,Double[] rateList,int label)
	{
		if(entityList.length!=relationList.length+1)
		{
			System.err.println("entity list and relation list have different sizes "
					+entityList.length+"\t"+relationList.length);
			System.exit(-1);
		}
		
		double prate=1.;
		for(int i=0;i<rateList.length;i++)
			prate*=rateList[i];
		
		prate=(prate-label+1e-6)/(1-prate+1e-6);
		
		for(int i=0;i<relationList.length-1;i++)
		{
			double[][] tmp_next_state_emb=new double[2][];
			tmp_next_state_emb[0]=relationEmbeddings[relationList[i]];
			tmp_next_state_emb[1]=entityEmbeddings[entityList[i+1]];

			double next_state_norm2=MatrixTool.MatrixNorm2(tmp_next_state_emb);
			double sim_rt_nextstate=MatrixTool.MatrixCosSim(
					MatrixTool.DotMulti(randt_emb, tmp_next_state_emb), randt_norm2
					, next_state_norm2);
			double theta_ij=MatrixTool.DotMulti(randt_emb, tmp_next_state_emb);
			double prij=rateList[i];
			
			double dtrue=prate*(1-prij)/theta_ij;
			double dfalse=-prate*prij/theta_ij;
			
			//true
			for(int k=0;k<dim;k++)
			{								
				relationEmbGradients[relationList[i]][k]+=randt_emb[0][k]*dtrue;
						

				entityEmbGradients[entityList[i+1]][k]+=randt_emb[1][k]*dtrue;
				
				relationEmbGradients[trel][k]+=relationEmbeddings[relationList[i]][k]*dtrue;
						
				entityEmbGradients[tentity][k]+=entityEmbeddings[entityList[i+1]][k]*dtrue;														
			}
			
			//false
			for(int j=0;j<triplet_graph[entityList[i]].length;j++)
			{
				for(int k=0;k<dim;k++)
				{								
					relationEmbGradients[triplet_graph[entityList[i]][j][0]][k]+=randt_emb[0][k]*dfalse;
					entityEmbGradients[triplet_graph[entityList[i]][j][1]][k]+=randt_emb[1][k]*dfalse;
					relationEmbGradients[trel][k]+=relationEmbeddings[triplet_graph[entityList[i]][j][0]][k]*dfalse;	
					entityEmbGradients[tentity][k]+=entityEmbeddings[triplet_graph[entityList[i]][j][1]][k]*dfalse;														
				}
			}
		}
	}*/
	
	//pairwise
	/*protected void OnePathGradient(Integer[] entityList,Integer[] relationList,Double[] rateList,int label)
	{
		if(label<0.5)
			return;
		if(entityList.length!=relationList.length+1)
		{
			System.err.println("entity list and relation list have different sizes "
					+entityList.length+"\t"+relationList.length);
			System.exit(-1);
		}
		
		double prate=1.;
		for(int i=0;i<rateList.length;i++)
			prate*=rateList[i];
		
		prate=(prate-label+1e-6)/(1-prate+1e-6);
		
		for(int i=0;i<relationList.length-1;i++)
		{
			double[][] tmp_next_state_emb=new double[2][];
			tmp_next_state_emb[0]=relationEmbeddings[relationList[i]];
			tmp_next_state_emb[1]=entityEmbeddings[entityList[i+1]];

			double next_state_norm2=MatrixTool.MatrixNorm2(tmp_next_state_emb);
			double sim_rt_nextstate=MatrixTool.MatrixCosSim(
					MatrixTool.DotMulti(randt_emb, tmp_next_state_emb), randt_norm2
					, next_state_norm2);
			double theta_ij=MatrixTool.DotMulti(randt_emb, tmp_next_state_emb);
			if(triplet_graph[entityList[i]].length<1)
				continue;
			for(int j=0;j<triplet_graph[entityList[i]].length;j++){
			int findex=rand.nextInt(triplet_graph[entityList[i]].length);
			findex=j;
			
			double[][] false_next_state_emb=new double[2][];
			false_next_state_emb[0]=relationEmbeddings[triplet_graph[entityList[i]][findex][0]];
			false_next_state_emb[1]=entityEmbeddings[triplet_graph[entityList[i]][findex][1]];
			double ftheta_ij=MatrixTool.DotMulti(randt_emb, false_next_state_emb);
			
			//if(-theta_ij+ftheta_ij>1)
				//continue;
			
			for(int k=0;k<dim;k++)
			{
				//true
				relationEmbGradients[relationList[i]][k]-=randt_emb[0][k];
				entityEmbGradients[entityList[i+1]][k]-=randt_emb[1][k];
				
				//false
				relationEmbGradients[triplet_graph[entityList[i]][findex][0]][k]+=randt_emb[0][k];
				entityEmbGradients[triplet_graph[entityList[i]][findex][1]][k]+=randt_emb[1][k];				
			}}
		}
	}*/
	
	protected double TriletAndRHOSim(double[][] a,double[][] b)
	{
		double[][] tmp=MatrixTool.MatrixSubtraction(a, b);
		return MatrixTool.MatrixNorm1(tmp);
	}
	//norm1
	protected void OnePathGradient(Integer[] entityList,Integer[] relationList,Double[] rateList,int label)
	{
		if(label<0.5)
			return;
		if(entityList.length!=relationList.length+1)
		{
			System.err.println("entity list and relation list have different sizes "
					+entityList.length+"\t"+relationList.length);
			System.exit(-1);
		}
		
		double prate=1.;
		for(int i=0;i<rateList.length;i++)
			prate*=rateList[i];
		
		prate=(prate-label+1e-6)/(1-prate+1e-6);
		
		for(int i=0;i<relationList.length-1;i++)
		{
			double[][] tmp_next_state_emb=new double[2][];
			tmp_next_state_emb[0]=relationEmbeddings[relationList[i]];
			tmp_next_state_emb[1]=entityEmbeddings[entityList[i+1]];

			double next_state_norm2=MatrixTool.MatrixNorm2(tmp_next_state_emb);
			/*double sim_rt_nextstate=MatrixTool.MatrixCosSim(
					MatrixTool.DotMulti(randt_emb, tmp_next_state_emb), randt_norm2
					, next_state_norm2);*/
			double theta_ij=TriletAndRHOSim(randt_emb, tmp_next_state_emb);
			if(triplet_graph[entityList[i]].length<1)
				continue;
			for(int j=0;j<triplet_graph[entityList[i]].length;j++){
			int findex=rand.nextInt(triplet_graph[entityList[i]].length);
			findex=j;
			
			double[][] false_next_state_emb=new double[2][];
			false_next_state_emb[0]=relationEmbeddings[triplet_graph[entityList[i]][findex][0]];
			false_next_state_emb[1]=entityEmbeddings[triplet_graph[entityList[i]][findex][1]];
			double ftheta_ij=TriletAndRHOSim(randt_emb, false_next_state_emb);
			
			if(theta_ij+1<ftheta_ij)
				continue;
			
			for(int k=0;k<dim;k++)
			{
				//true
				if(relationEmbeddings[relationList[i]][k]-randt_emb[0][k]>0)
					relationEmbGradients[relationList[i]][k]-=1;
				else
					relationEmbGradients[relationList[i]][k]+=1;	
				
				if(entityEmbeddings[entityList[i+1]][k]-randt_emb[1][k]>0)				
					entityEmbGradients[entityList[i+1]][k]-=1;
				else
					entityEmbGradients[entityList[i+1]][k]+=1;					
				
				//false
				if(relationEmbeddings[triplet_graph[entityList[i]][findex][0]][k]-randt_emb[0][k]>0)
					relationEmbGradients[triplet_graph[entityList[i]][findex][0]][k]+=1;
				else
					relationEmbGradients[triplet_graph[entityList[i]][findex][0]][k]-=1;
				if(entityEmbeddings[triplet_graph[entityList[i]][findex][1]][k]-randt_emb[1][k]>0)
					entityEmbGradients[triplet_graph[entityList[i]][findex][0]][k]+=1;
				else
					entityEmbGradients[triplet_graph[entityList[i]][findex][0]][k]-=1;					
			}}
		}
	}
	
	
	/*protected void OnePathGradient(Integer[] entityList,Integer[] relationList,int label)
	{
		if(entityList.length!=relationList.length+1)
		{
			System.err.println("entity list and relation list have different sizes "
					+entityList.length+"\t"+relationList.length);
			System.exit(-1);
		}
		for(int i=0;i<relationList.length;i++)
		{
			double[][] tmp_next_state_emb=new double[2][];
			tmp_next_state_emb[0]=relationEmbeddings[relationList[i]];
			tmp_next_state_emb[1]=entityEmbeddings[entityList[i+1]];

			double next_state_norm2=MatrixTool.MatrixNorm2(tmp_next_state_emb);
			double sim_rt_nextstate=MatrixTool.MatrixCosSim(
					MatrixTool.DotMulti(randt_emb, tmp_next_state_emb), randt_norm2
					, next_state_norm2);
			
			for(int k=0;k<dim;k++)
			{								

				relationEmbGradients[relationList[i]][k]+=-1*relationEmbeddings[trel][k]*label;

				entityEmbGradients[entityList[i+1]][k]+=-1*entityEmbeddings[tentity][k]*label;
				
				relationEmbGradients[trel][k]+=-1*relationEmbeddings[relationList[i]][k]*label;
				entityEmbGradients[tentity][k]+=-1*entityEmbeddings[entityList[i+1]][k]*label;															
			}
		}
	}*/
	
	
	
	protected double[] CalPathWeightByAttention(double[] fcounts,int rel)
	{
		double[] scores=new double[fcounts.length];
		for(int i=0;i<fcounts.length-1;i++)
		{
			if(fcounts[i]>1e-4)
				scores[i]=CalOnePathWeightByAttention(i,rel);
		}
		scores[scores.length-1]=fcounts[scores.length-1];
		return scores;
	}
	protected double CalOnePathWeightByAttention(int index,int rel)
	{
		double sum=0;
		RPath rpath=rpathLists[rel][index];
		for(int i=0;i<rpath.length();i++)
		{
			sum+=MatrixTool.VectorCosSim(relationEmbeddings[rpath.GetElement(i)], 
					relationEmbeddings[rel]);
			
		}
		sum/=rpath.length();
		return sum;
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
				//candemb[1]=new double[dim];
				//res[i]=MatrixTool.DotMulti(randtemb, candemb);
				//res[i]=MatrixTool.VectorDot(randtemb[0], candemb[0]);
				if(record_entity)
					//res[i]=MatrixTool.MatrixCosSim(randt_emb, candemb);
					res[i]=TriletAndRHOSim(randt_emb, candemb);
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
			//res[i]=Math.pow(10, res[i]);
			
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
		if(!weighted)
			return super.Logistic_F_wx(r, fcounts);

		fcounts=CalPathWeightByAttention(fcounts,r);
		return super.Logistic_F_wx(r, fcounts);
		/*double wx=0;
		for(int i=0;i<fcounts.length;i++)
			wx+=fcounts[i];
		double exp_wx=Math.exp(-wx);
		//return exp_wx/(1+exp_wx);
		return 1/(1+exp_wx);*/
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
				if(pathWeights[i][pathWeights[i].length-1]<0&&emb_force_1) // 
				{
					//pathWeights[i][pathWeights[i].length-1]=0.5;
					pathWeights[i][pathWeights[i].length-1]=-pathWeights[i][pathWeights[i].length-1];
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
