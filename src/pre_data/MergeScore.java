package pre_data;


import java.io.*;
import java.util.*;

public class MergeScore {

	
	public static String[] readfile(String filename) throws IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		List<String> list=new ArrayList<String>();
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			list.add(buffer);
		}
		return list.toArray(new String[0]);
	}
	
	public static void main(String[] args) throws IOException
	{
		int relnum=Integer.parseInt(args[0]);
		int[][][] readdata=new int[relnum][6][7];
		String dir=args[1];
		for(int i=0;i<relnum;i++)
		{
			String[] line=readfile(dir+i+"log");
			for(int j=1;i<7;i++)
			{
				
			}
		}
	}
}
