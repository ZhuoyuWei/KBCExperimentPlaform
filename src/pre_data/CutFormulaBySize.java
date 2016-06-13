package pre_data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import wzy.meta.RPath;
import wzy.model.RandomWalkModel;

public class CutFormulaBySize {

	public static int MaxSize=100;
	
	public static void reprint(RPath[][] rpathLists,String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<rpathLists.length;i++)
		{
			int maxsize=MaxSize<rpathLists[i].length?MaxSize:rpathLists[i].length;
			ps.println(i+"\t"+maxsize);
			for(int j=0;j<maxsize;j++)
			{
				for(int k=0;k<rpathLists[i][j].length()-1;k++)
				{
					ps.print(rpathLists[i][j].GetElement(k)+"\t");
				}
				ps.println(rpathLists[i][j].GetElement(rpathLists[i][j].length()-1));
			}
		}
		ps.close();
		
	}
	
	
	public static void ReadSimply(String filename,String output) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		int line=0;
		PrintStream ps=new PrintStream(output);
		while((buffer=br.readLine())!=null)
		{
			String[] ss=buffer.split("\t");
			int rel=Integer.parseInt(ss[0]);
			int count=Integer.parseInt(ss[1]);
			List<String> list=new ArrayList<String>();
			for(int i=0;i<count;i++)
			{
				buffer=br.readLine();
				list.add(buffer);
			}
			
			int maxsize=MaxSize<list.size()?MaxSize:list.size();
			ps.println(rel+"\t"+maxsize);
			for(int j=0;j<maxsize;j++)
			{
				ps.println(list.get(j));
			}
			
		}
		
		ps.close();
		br.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		String dir="C:\\Users\\Administrator\\Documents\\data\\fb15k_doubledirect\\";
		//RandomWalkModel.relationNum=2690;
		//RandomWalkModel.ReadFormulas(dir+"paths.txt");
		//reprint(RandomWalkModel.rpathLists,dir+"paths.txt.100");
		ReadSimply(dir+"paths.txt",dir+"paths.txt.100");
	}
}
