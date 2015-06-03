package proc;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MergeTransRModel {

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
	}
	
	public static void main(String[] args)
	{}
	
}
