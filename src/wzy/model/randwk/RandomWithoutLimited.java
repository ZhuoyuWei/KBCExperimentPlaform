package wzy.model.randwk;

import java.util.ArrayList;
import java.util.List;

import wzy.meta.FormulaTreeNode;
import wzy.meta.RPath;
import wzy.model.RandomWalkModel;
import wzy.tool.MatrixTool;

public class RandomWithoutLimited extends RandomWalkModel{

	public int max_round=10;
	
	
	
	public void OneBranchTraining(int[][] train_triplets,int sindex,int eindex)
	{
		InitGradients();
		for(int i=sindex;i<=eindex;i++)
		{			
			double[] true_paths_count=RandomWalk(train_triplets[i]);
			train_true_count+=CheckRandomRes(true_paths_count);
			double true_f_wx=Logistic_F_wx(train_triplets[i][1],true_paths_count);
			Logistic_Grident(train_triplets[i][1],true_paths_count,true_f_wx-1);
			//false
			for(int j=0;j<false_triplet_size;j++)
			{
				int[] false_triplet=GenerateFalseTriplet(train_triplets[i]);
				double[] false_paths_count=RandomWalk(false_triplet);
				train_false_count+=CheckRandomRes(false_paths_count);
				double false_f_wx=Logistic_F_wx(false_triplet[1],false_paths_count);
				Logistic_Grident(false_triplet[1],false_paths_count,false_f_wx);
			}
			
		}
		//Add L2 norm
		if(l2_flag)
		{
			for(int i=0;i<pathWeightGradients.length;i++)
			{
				for(int j=0;j<pathWeightGradients[i].length;j++)
				{
					//pathWeightGradients[i][j]+=2*pathWeights[i][j];
					//L1
					if(Math.abs(pathWeights[i][j])<1e-6)
						continue;
					if(pathWeights[i][j]>0)
						pathWeightGradients[i][j]+=l2_C;
					else
						pathWeightGradients[i][j]-=l2_C;
				}
			}
		}
		
		UpdateWeights();
		
	}
	
	
	public RPath[] FreeWalk(int[] triplet)
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
		
		
	}
	
	
}
