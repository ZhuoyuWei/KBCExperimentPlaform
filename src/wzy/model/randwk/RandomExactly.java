package wzy.model.randwk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import wzy.meta.FormulaTreeNode;
import wzy.meta.RPath;
import wzy.model.RandomWalkModel;

public class RandomExactly extends RandomWalkModel {
	
	public int max_round=10000;
	public double restart_rate=0.3;
	public double back_rate=0.5;
	public Random rand=new Random();
	
	
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
	
	@Override
	public int[] RandomWalk(int[] triplet)
	{
		//int[] fcounts=new int[pathWeights[triplet[1]].length];
		int[] fcounts=super.RandomWalk(triplet);
		
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
	
	public int[] RandomWalk(int[] triplet)
	{
		int[] fcounts=super.RandomWalk(triplet);
		
		int state=triplet[0];
		FormulaTreeNode ft_node=ff[triplet[1]].root;
		List<int[]> path_record=new ArrayList<int[]>();
		
		for(int i=0;i<max_round;i++)
		{
		
		}
	}
	
	@Override
	public double Logistic_F_wx(int r,int[] fcounts)
	{
		return super.Logistic_F_wx(r, fcounts);
	}

}
