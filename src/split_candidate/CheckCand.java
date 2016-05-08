package split_candidate;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import wzy.io.FileTools;

public class CheckCand {

	
	public String[] triplets;
	public String[] cands;
	
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
	
	public void SplitData(String dir) throws FileNotFoundException
	{
		List<String>[][] list=new List[18][2];
		for(int i=0;i<18;i++)
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
			for(int j=0;j<2;j++)
			{
				list[rel][1].add(cands[i*2+j]);				
			}
		}
		PrintFiles(dir,list);
		
	}
	
	public void PrintFiles(String dir,List<String>[][] data) throws FileNotFoundException
	{
		FileTools.makeDir(dir);
		FileTools.makeDir(dir+"/0");
		FileTools.makeDir(dir+"/1");
		for(int i=0;i<18;i++)
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
		cc.triplets=cc.ReadFile("C:\\Users\\Administrator\\Documents\\data\\wn18\\exp_test.txt");
		cc.cands=cc.ReadFile("C:\\Users\\Administrator\\Documents\\data\\wn18\\cand.txt");
		System.out.println(cc.triplets.length+"\t"+cc.cands.length);
		cc.Evaluation();
		cc.SplitData("C:\\Users\\Administrator\\Documents\\data\\wn18\\split_candidatesas");
		
	}
}
