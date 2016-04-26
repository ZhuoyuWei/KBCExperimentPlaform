package wzy.meta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class FormulaForest {

	public FormulaTreeNode root;
	
	public void BuildForest(int relNum)
	{	
		root=new FormulaTreeNode();
		root.next_map=new FormulaTreeNode[relNum];
	}
	public void BuildForest(RPath[] paths,int relNum)
	{
		BuildForest(relNum);
		for(int i=0;i<paths.length;i++)
		{
			if(paths[i].length()<=0)
				continue;
			if(root.next_map[paths[i].GetElement(0)]==null)
			{
				root.next_map[paths[i].GetElement(0)]=new FormulaTreeNode();
				root.next_map[paths[i].GetElement(0)].rel=paths[i].GetElement(0);
				root.next_map[paths[i].GetElement(0)].parent=root;
				if(paths[i].length()==1)
				{
					root.next_map[paths[i].GetElement(0)].leaf=true;
					root.next_map[paths[i].GetElement(0)].formula=i;
				}
			}
			FormulaTreeNode pnode=root.next_map[paths[i].GetElement(0)];
			for(int j=1;j<paths[i].length();j++)
			{
				//pnode.leaf=false;
				if(pnode.next_map==null)
					pnode.next_map=new FormulaTreeNode[relNum];
				if(pnode.next_map[paths[i].GetElement(j)]==null)
				{
					pnode.next_map[paths[i].GetElement(j)]=new FormulaTreeNode();
					pnode.next_map[paths[i].GetElement(j)].rel=paths[i].GetElement(j);
					pnode.next_map[paths[i].GetElement(j)].parent=root;
				}
				pnode=pnode.next_map[paths[i].GetElement(j)];
				if(j==paths[i].length()-1)
				{
					pnode.leaf=true;
					pnode.formula=i;
				}
			}
		}
		Map2List(root);

	}
	
	public void Map2List(FormulaTreeNode node)
	{
		if(node.next_map==null)
			return;
		node.next_list=new ArrayList<Integer>();
		for(int i=0;i<node.next_map.length;i++)
		{
			if(node.next_map[i]!=null)
			{
				Map2List(node.next_map[i]);
				node.next_list.add(i);
			}
		}
	}

}
