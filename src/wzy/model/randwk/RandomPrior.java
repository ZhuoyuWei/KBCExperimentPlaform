package wzy.model.randwk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import wzy.meta.FormulaTreeNode;
import wzy.meta.TripletHash;
import wzy.model.EmbeddingModel;
import wzy.model.RandomWalkModel;

public class RandomPrior extends RandomWalkModel{

	public int max_round=100;
	public double restart_rate=0.3;
	public double back_rate=0.5;
	public Random rand=new Random();
	public double[][] entityEmbeddings;
	public double[][] relationEmbeddings;
	public EmbeddingModel em;
	public double max_prior=0;
	public double lambda=4.5;
	
	public Map<TripletHash,Double> prior_map;
	
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
	public double[] RandomWalk(int[] triplet)
	{
		double[] fcounts=new double[pathWeights[triplet[1]].length];
		
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
			if(path_record.size()>2&&rand.nextDouble()<back_rate)
			{
				state=path_record.get(path_record.size()-2)[1];
				ft_node=ft_node.parent;
				path_record.remove(path_record.size()-1);
			}			
			List<Integer> indexList=new ArrayList<Integer>();
			List<Double> valList=new ArrayList<Double>();
			
			double pre=0.;
			for(int j=0;j<triplet_graph[state].length;j++)
			{
				if(ft_node.next_map[triplet_graph[state][j][0]]!=null)
				{
					indexList.add(j);
					TripletHash tri=new TripletHash();
					int[] one_triplet=new int[3];
					one_triplet[0]=state;
					one_triplet[1]=triplet_graph[state][j][0];
					one_triplet[2]=triplet_graph[state][j][1];
					tri.setTriplet(one_triplet);
					Double val=prior_map.get(tri);
					if(val==null)
						valList.add(1.);
					else
						valList.add(val+pre);
					pre=valList.get(valList.size()-1);
				}
			}
			
			
			
			if(indexList.size()<=0)
			{
				//restart
				state=triplet[0];
				ft_node=ff[triplet[1]].root;
				path_record=new ArrayList<int[]>();
			}
			
			//random by prior
			
			for(int j=0;j<valList.size();j++)
			{
				valList.set(j, valList.get(j)/pre);
			}
			double rand_val=rand.nextDouble();
			int rand_ind=indexList.get(0);
			for(int j=1;j<indexList.size();j++)
			{
				if(valList.get(j-1)>=rand_val)
					break;
				rand_ind=indexList.get(j);
			}
			//forward
			state=triplet_graph[state][rand_ind][1];
			ft_node=ft_node.next_map[triplet_graph[state][rand_ind][0]];
			path_record.add(triplet_graph[state][rand_ind]);
			if(ft_node.leaf)
			{
				fcounts[ft_node.formula]++;
			}
		}

		return fcounts;
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
			double val=4.5*(2-tmp_score_list.get(i)/max_prior);
			prior_map.put(tmp_tri_list.get(i), val);
		}
		
	}
	
	
}
