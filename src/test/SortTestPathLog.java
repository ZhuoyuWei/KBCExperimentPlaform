package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortTestPathLog {
	
	public List<PathCount> pcList=new ArrayList<PathCount>();
	

	public void ReadPathFile(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		int line=0;
		PathCount pc=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			if(line%2==0)
			{
				pc=new PathCount();
				String[] ss=buffer.split("\t");
				pc.rel=Integer.parseInt(ss[0]);
				pc.count=Integer.parseInt(ss[1]);
				pc.pathid=line/2;
				if(pc.count==0)
					line--;
			}
			else
			{
				String[] ss=buffer.split("\t");
				for(int i=0;i<ss.length;i++)
				{
					PathPairCount ppc=new PathPairCount();
					String[] sss=ss[i].split("/");
					//System.out.println(ss[i]);
					ppc.score=Double.parseDouble(sss[0]);
					ppc.count=Integer.parseInt(sss[1]);
					pc.ppcList.add(ppc);
				}
				Collections.sort(pc.ppcList,new PathPairCount());
				pcList.add(pc);
			}
			line++;
		}
	}
	
	public void RePrint(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<pcList.size();i++)
		{
			PathCount pc=pcList.get(i);
			
			int hascount=0;
			for(int j=0;j<pc.ppcList.size();j++)
			{
				if(pc.ppcList.get(j).count>0)
					hascount++;
			}
			
			ps.println(pc.rel+"\t"+pc.count+"\t"+hascount);
			for(int j=0;j<pc.ppcList.size();j++)
			{
				ps.print(pc.ppcList.get(j).score+"/"+pc.ppcList.get(j).count+"\t");
			}
			ps.println();
		}
	}
	
	
	public static void main(String[] args) throws IOException
	{
		SortTestPathLog stpl=new SortTestPathLog();
		stpl.ReadPathFile("F:\\Workspace\\KBCworkspace\\result_analyze\\tester_path.log");
		stpl.RePrint("F:\\Workspace\\KBCworkspace\\result_analyze\\tester_path.sort");
	}
	
}

class PathCount
{
	public int rel;
	public int pathid;
	public int count;
	public List<PathPairCount> ppcList=new ArrayList<PathPairCount>();
}

class PathPairCount implements Comparator
{
	public double score;
	public int count;
	@Override
	public int compare(Object o1, Object o2) {
		// TODO Auto-generated method stub
		PathPairCount ppc1=(PathPairCount)o1;
		PathPairCount ppc2=(PathPairCount)o2;
		
		if(Math.abs(ppc1.score-ppc2.score)<1e-10)
			return 0;
		else if(ppc1.score<ppc2.score)
			return -1;
		else
			return 1;
	}
}
