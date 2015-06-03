package proc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import java.util.*;

public class MergeDataSet {

	public List<String> lineList=new ArrayList<String>();
	
	public void ReadFile(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			lineList.add(buffer);
		}
		br.close();
	}
	
	public void PrintFile(String filename) throws FileNotFoundException
	{
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<lineList.size();i++)
		{
			ps.println(lineList.get(i));
		}
		ps.close();
	}
	
	public static void main(String[] args) throws IOException
	{
		MergeDataSet md=new MergeDataSet();
		md.ReadFile("F:\\emnlp2015\\fb15k\\FB15k\\liuzhiyuan.train.txt");
		md.ReadFile("F:\\emnlp2015\\fb15k\\FB15k\\liuzhiyuan.valid.txt");
		md.PrintFile("F:\\emnlp2015\\fb15k\\FB15k\\liuzhiyuan.trainAndvalid.txt");
	}
	
}
