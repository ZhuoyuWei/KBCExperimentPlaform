package split_candidate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import wzy.io.FileTools;
import wzy.thread.RandomWalkProcess;

public class CheckCand {

	
	public String[] triplets;
	public String[] cands;
	public int relNum;
	
	public String[] ReadFile(String filename) throws IOException
	{
		List<String> lineList=new ArrayList<String>();
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			lineList.add(buffer);
			
		}
		br.close();
		
		return lineList.toArray(new String[0]);
	}
	
	public void Evaluation()
	{
		int[] count=new int[4];
		for(int i=0;i<triplets.length;i++)
		{
			String[] triplet=triplets[i].split("\t");
			String[] ssleft=cands[i*2].split("\t");
			int j=1;
			for(;j<=ssleft.length;j++)
			{
				if(Integer.parseInt(ssleft[j-1])==Integer.parseInt(triplet[2]))
				{
					break;
				}
			}
			if(j<=10)
				count[0]++;
			count[1]+=j;
			
			ssleft=cands[i*2+1].split("\t");
			j=1;
			for(;j<=ssleft.length;j++)
			{
				if(Integer.parseInt(ssleft[j-1])==Integer.parseInt(triplet[0]))
				{
					break;
				}
			}
			if(j<=10)
				count[2]++;
			count[3]+=j;			
			
		}
		for(int i=0;i<4;i++)
		{
			System.out.println(count[i]/(double)(triplets.length));
		}
	}
	
	public void SplitData(String dir,int state) throws FileNotFoundException
	{
		List<String>[][] list=new List[relNum][2];
		for(int i=0;i<relNum;i++)
		{
			for(int j=0;j<2;j++)
			{
				list[i][j]=new ArrayList<String>();
			}
		}
		for(int i=0;i<triplets.length;i++)
		{
			String[] ss=triplets[i].split("\t");
			int rel=Integer.parseInt(ss[1]);
			list[rel][0].add(triplets[i]);
			for(int j=0;j<state;j++)
			{
				list[rel][1].add(cands[i*state+j]);				
			}
		}
		PrintFiles(dir,list);
		
	}
	
	public void PrintFiles(String dir,List<String>[][] data) throws FileNotFoundException
	{
		FileTools.makeDir(dir);
		FileTools.makeDir(dir+"/0");
		FileTools.makeDir(dir+"/1");
		for(int i=0;i<relNum;i++)
		{
			for(int j=0;j<2;j++)
			{
				PrintStream ps=new PrintStream(dir+"/"+j+"/"+i);
				for(int k=0;k<data[i][j].size();k++)
				{
					ps.println(data[i][j].get(k));
				}
				ps.close();
			}
		}
	}

	
	public static void main(String[] args) throws IOException
	{
		CheckCand cc=new CheckCand();
		String dir=args[0];
		//String dir="C:\\Users\\Administrator\\Documents\\data\\wn18_doubledirect\\";
		cc.triplets=cc.ReadFile(dir+"exp_test.txt");
		cc.cands=cc.ReadFile(dir+args[1]);
		cc.relNum=Integer.parseInt(args[2]);
		System.out.println(cc.triplets.length+"\t"+cc.cands.length);
		//cc.Evaluation();
		cc.SplitData(dir+"split_candidates",RandomWalkProcess.teststate);
		
	}
}
