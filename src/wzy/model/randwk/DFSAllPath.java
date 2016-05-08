package wzy.model.randwk;

import java.util.ArrayList;
import java.util.List;

import wzy.meta.FormulaTreeNode;
import wzy.model.RandomWalkModel;

public class DFSAllPath extends RandomWalkModel{
	

	private int t;

	private int[] fcounts;
	
	@Override
	public int[] RandomWalk(int[] triplet)
	{
		fcounts=super.RandomWalk(triplet);
		
		int state=triplet[0];
		t=triplet[2];
		FormulaTreeNode ft_node=ff[triplet[1]].root;
		//List<int[]> path_record=new ArrayList<int[]>();
		DFS(state,ft_node);
		return fcounts;
		
	}
	protected void DFS(int s,FormulaTreeNode fnode)
	{
		for(int j=0;j<triplet_graph[s].length;j++)
		{
			if(fnode.next_map[triplet_graph[s][j][0]]!=null)
			{
				if(fnode.next_map[triplet_graph[s][j][0]].leaf)
				{
					fcounts[fnode.next_map[triplet_graph[s][j][0]].formula]++;
				}
				else if(fnode.next_map[triplet_graph[s][j][0]].next_map!=null)
				{
					DFS(triplet_graph[s][j][1],fnode.next_map[triplet_graph[s][j][0]]);
				}
			}
		}
	}

}
