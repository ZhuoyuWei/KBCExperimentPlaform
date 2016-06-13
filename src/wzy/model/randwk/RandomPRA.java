package wzy.model.randwk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import wzy.meta.FormulaTreeNode;
import wzy.meta.RPath;
import wzy.model.RandomWalkModel;
import wzy.tool.MatrixTool;

public class RandomPRA extends RandomWalkModel {
	
	//public int max_round=10;
	public double restart_rate=0.3;
	public double back_rate=0.5;
	public Random rand=new Random();
	
	public static Map<Integer,Integer>[] graphcounts;
	
	public static void StaticGraphForRelations(int[][][] graph)
	{
		graphcounts=new Map[graph.length];
		for(int i=0;i<graph.length;i++)
		{
			graphcounts[i]=new HashMap<Integer,Integer>();
			for(int j=0;j<graph[i].length;j++)
			{
				Integer count=graphcounts[i].get(graph[i][j][0]);
				if(count==null)
				{
					graphcounts[i].put(graph[i][j][0], 1);
				}
				else
				{
					count++;
					graphcounts[i].remove(graph[i][j][0]);
					graphcounts[i].put(graph[i][j][0], count);
				}
			}
		}
		
	}
	
	double[] fcounts;
	int t;
	
	@Override
	public void InitGradients()
	{
		super.InitGradients();
	}
	
	@Override
	public void OneBranchTraining(int[][] train_triplets,int sindex,int eindex)
	{
		super.OneBranchTraining(train_triplets, sindex, eindex);
	}
	
/*	@Override
	public double[] RandomWalk(int[] triplet)
	{
		//int[] fcounts=new int[pathWeights[triplet[1]].length];
		fcounts=super.RandomWalk(triplet);
		
		int state=triplet[0];
		FormulaTreeNode ft_node=ff[triplet[1]].root;
		List<int[]> path_record=new ArrayList<int[]>();
		
		for(int i=0;i<max_round;i++)
		{
			//restart
			if(rand.nextDouble()<restart_rate)
			{
				state=triplet[0];
				ft_node=ff[triplet[1]].root;
				path_record=new ArrayList<int[]>();
			}
			//back
			if(ft_node.next_map==null||(path_record.size()>2&&rand.nextDouble()<back_rate))
			{
				if(path_record.size()<2)
				{
					//restart
					state=triplet[0];
					ft_node=ff[triplet[1]].root;
					path_record=new ArrayList<int[]>();
					continue;
				}
				state=path_record.get(path_record.size()-2)[1];
				ft_node=ft_node.parent;
				path_record.remove(path_record.size()-1);
			}			
			List<Integer> indexList=new ArrayList<Integer>();
			
			for(int j=0;j<triplet_graph[state].length;j++)
			{
				if(ft_node.next_map[triplet_graph[state][j][0]]!=null)
				{
					indexList.add(j);
				}
			}
			
			if(indexList.size()<=0||ft_node.next_map==null)
			{
				//restart
				state=triplet[0];
				ft_node=ff[triplet[1]].root;
				path_record=new ArrayList<int[]>();
				continue;
			}
			
			//random exactly
			int rand_ind=Math.abs(rand.nextInt())%indexList.size();
			while(rand_ind<0)
				rand_ind=Math.abs(rand.nextInt())%indexList.size();
			//forward
			//System.out.println(triplet_graph[state][indexList.get(rand_ind)][0]);
			ft_node=ft_node.next_map[triplet_graph[state][indexList.get(rand_ind)][0]];
			path_record.add(triplet_graph[state][indexList.get(rand_ind)]);
			state=triplet_graph[state][indexList.get(rand_ind)][1];
			if(ft_node.leaf)
			{
				fcounts[ft_node.formula]++;
			}
		}
		
		if(nocount)
			super.NoCount(fcounts);

		return fcounts;
	}
	*/
	
	@Override
	public double[] RandomWalk(int[] triplet,boolean training)
	{
		fcounts=super.RandomWalk(triplet,training);
		
		
		t=triplet[2];
		
		//List<int[]> path_record=new ArrayList<int[]>();
		for(int r=0;r<max_round;r++)
		{
			int state=triplet[0];
			FormulaTreeNode ft_node=ff[triplet[1]].root;
			DFS(state,ft_node);
		}
		
		return fcounts;
		
	}
	protected void DFS(int s,FormulaTreeNode fnode)
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
					return;
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
			double[] provector=CalAttentionSimilarity(s,nextrt);
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

			DFS(stateList.get(index),formulaList.get(index));
			
		}
			
	}
	
	protected double[] CalAttentionSimilarity(int head, List<int[]> nextrt)
	{
		double[] res=new double[nextrt.size()];
		double sum=0;
		for(int i=0;i<res.length;i++)
		{
			res[i]=1./graphcounts[head].get(nextrt.get(i)[0]);
		}
		
		for(int i=0;i<res.length;i++)
		{
			/*if(res[i]<0)
				res[i]=0;*/
			res[i]=Math.exp(res[i]);
			//res[i]=Math.pow(1000, res[i]);
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

}
