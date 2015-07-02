package wzy.thread.cons4rel;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import wzy.meta.RPath;

public class BFSearch extends ConstrForRel{

	
	
	@Override
	protected void Mining(int[] triplet)
	{
		Set<Integer> visited=new HashSet<Integer>();
		visited.add(triplet[0]);
		
		Queue<Integer> queue=new LinkedList<Integer>();
		queue.add(triplet[0]);	
		Queue<RPath> rpathList=new LinkedList<RPath>();
		rpathList.add(new RPath());
			
		
		for(int i=1;i<=maxLength;i++)
		{
			Queue<Integer> preList=queue;
			queue=new LinkedList<Integer>();
			
			Queue<RPath> prepathList=rpathList;
			rpathList=new LinkedList<RPath>();
			
			Integer s;
			while((s=preList.poll())!=null)
			{
				RPath path=prepathList.poll();
				if(path==null)
					break;
				int[][] neighbors=triplet_graph[s];
				for(int j=0;j<neighbors.length;j++)
				{
					//step 1: judge whether end point is triplet[2], and decide to save or count the path.
					if(neighbors[j][1]==triplet[2])
					{
						if(path.length()<minLength)
							continue;
						
						RPath savep=path.CopySelf();
						savep.Add(neighbors[j][0]);
						Integer count=path2Count.get(savep);
						if(count==null)
						{
							path2Count.put(savep, 1);
						}
						else
						{
							count++;
							path2Count.put(savep, count);							
						}
					}
					//step 2: judge whether the neighbor is visited, and decide to push it to the queue.
					else
					{
						if(i==maxLength||visited.contains(neighbors[j][1]))
							continue;
						queue.add(neighbors[j][1]);
						visited.add(neighbors[j][1]);
						RPath savep=path.CopySelf();
						savep.Add(neighbors[j][0]);	
						rpathList.add(savep);
					}
				}
			}

		}
		
	}
	
}
