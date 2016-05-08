package wzy.thread.cons4rel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import wzy.meta.RPath;

public class DFSearch extends ConstrForRel{

	private int o;
	private boolean debug=false;
	
	@Override
	protected void Mining(int[] triplet)
	{
		o=triplet[2];
		int[] stack=new int[100];
		int stacksize=0;
		stack[stacksize++]=triplet[0];
		int[] relList=new int[100];
		DFS(stack,relList,stacksize);		
	}
	
	protected void Mining(int[] triplet,int flag)
	{
		o=triplet[2];
		int[] stack=new int[100];
		int stacksize=0;
		stack[stacksize++]=triplet[0];
		int[] relList=new int[100];
		DFS(stack,relList,stacksize,flag);
	}
	
	public void DFS(int[] stack,int[] relList, int stacksize)
	{
		if(stacksize>maxLength)
			return;
		Integer s=stack[stacksize-1];
		int[][] neighbors=triplet_graph[s];
		
		//1. if o appears in neighbors, we save it as the path. 
		//Tips: spliting the two steps: a. find and save object paths; and b. DFS to neighbors, is to random sampling paths
		//for long paths or super-popular nodes.
		for(int i=0;i<neighbors.length;i++)
		{
			if(neighbors[i][1]==o)
			{
				//save path
				relList[stacksize-1]=neighbors[i][0];
				stack[stacksize++]=o;
				
				
				RPath path=new RPath();
				path.CopyFromStack(relList, stacksize-1);
				
				if(path.length()<minLength)
					continue;
				
				
				Integer count=path2Count.get(path);
				if(count==null)
				{
					path2Count.put(path, 1);
				}
				else
				{
					count++;
					path2Count.put(path, count);							
				}
				//debug
				if(debug)
				{
					for(int t=0;t<stacksize;t++)
					{
						System.out.print(stack[t]+" ");
					}
					System.out.print("----");
					for(int t=0;t<stacksize-1;t++)
					{
						System.out.print(relList[t]+" ");
					}					
					System.out.println();
				}
				
				stacksize--;
			}	
		}
		
		
		if(stacksize>=maxLength)
			return;
		
		//2. DFS to neightbors: for all next nodes
		for(int i=0;i<neighbors.length;i++)
		{
			if(neighbors[i][1]==o)
				continue;
			
			int j;
			for(j=0;j<stacksize;j++)
			{
				if(stack[j]==neighbors[i][1])
					break;
			}
			if(j<stacksize)
				continue;
			
			//details
			relList[stacksize-1]=neighbors[i][0];
			stack[stacksize++]=neighbors[i][1];
			DFS(stack,relList,stacksize);
			stacksize--;
		}		
		
	}
	
	public void DFS(int[] stack,int[] relList, int stacksize,int flag)
	{
		if(stacksize>maxLength)
			return;
		Integer s=stack[stacksize-1];
		int[][] neighbors=triplet_graph[s];
		
		//1. if o appears in neighbors, we save it as the path. 
		//Tips: spliting the two steps: a. find and save object paths; and b. DFS to neighbors, is to random sampling paths
		//for long paths or super-popular nodes.
		for(int i=0;i<neighbors.length;i++)
		{
			if(neighbors[i][1]==o)
			{
				//save path
				relList[stacksize-1]=neighbors[i][0];
				stack[stacksize++]=o;
				
				
				RPath path=new RPath();
				path.CopyFromStack(relList, stacksize-1);
				
				if(path.length()<minLength)
					continue;
				
				
				int[] counts=path2Conf.get(path);
				if(counts==null)
				{
					counts=new int[2];
					path2Conf.put(path, counts);
				}
				counts[flag]++;
				//debug
				if(debug)
				{
					for(int t=0;t<stacksize;t++)
					{
						System.out.print(stack[t]+" ");
					}
					System.out.print("----");
					for(int t=0;t<stacksize-1;t++)
					{
						System.out.print(relList[t]+" ");
					}					
					System.out.println();
				}
				
				stacksize--;
			}	
		}
		
		
		if(stacksize>=maxLength)
			return;
		
		//2. DFS to neightbors: for all next nodes
		for(int i=0;i<neighbors.length;i++)
		{
			if(neighbors[i][1]==o)
				continue;
			
			int j;
			for(j=0;j<stacksize;j++)
			{
				if(stack[j]==neighbors[i][1])
					break;
			}
			if(j<stacksize)
				continue;
			
			//details
			relList[stacksize-1]=neighbors[i][0];
			stack[stacksize++]=neighbors[i][1];
			DFS(stack,relList,stacksize,flag);
			stacksize--;
		}		
		
	}
	
}
