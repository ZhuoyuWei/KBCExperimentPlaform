package wzy.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class FileTools {

	
	/**
	 * Read lines as the format: 'h r t'
	 * @param filename
	 * @param separator is a regular expression for separate one line
	 * @return
	 */
	public static int[][] ReadIntegralTriplets(String filename,String separator)
	{
		List<int[]> triplets=new ArrayList<int[]>();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split(separator);
				if(ss.length!=3)
				{
					System.err.println("Input Data Format is wrong");
					System.err.println(buffer);
					System.exit(-1);
				}
				int[] triplet=new int[3];
				for(int i=0;i<3;i++)
				{
					triplet[i]=Integer.parseInt(ss[i]);
				}
				triplets.add(triplet);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return triplets.toArray(new int[0][0]);
	}
	
	/**
	 * Read lines as the format: 'h r t'
	 * h,r,t are all string
	 * @param filename
	 * @param separator is a regular expression for separate one line
	 * @return
	 */
	public static String[][] ReadStringTriplets(String filename,String separator)
	{
		List<String[]> triplets=new ArrayList<String[]>();
		try {
			BufferedReader br=new BufferedReader(new FileReader(filename));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split(separator);
				if(ss.length!=3)
				{
					System.err.println("Input Data Format is wrong");
					System.err.println(buffer);
					System.exit(-1);
				}
				triplets.add(ss);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return triplets.toArray(new String[0][0]);
	}	
	
	public static void PrintIntegralTriplets(int[][] triplets,String filename,String separator)
	{
		try {
			PrintStream ps=new PrintStream(filename);
			for(int i=0;i<triplets.length;i++)
			{
				ps.println(triplets[i][0]+separator+triplets[i][1]+separator+triplets[i][2]);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
