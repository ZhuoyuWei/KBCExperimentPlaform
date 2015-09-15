package test.analy;
import java.io.*;



public class FormulaCount {

	public int length=5\;
	
	public void ReadFile(String filename) throws NumberFormatException, IOException
	{
		BufferedReader br=new BufferedReader(new FileReader(filename));
		String buffer=null;
		int count=0;
		int[] lengths=new int[length+1];
		while((buffer=br.readLine())!=null)
		{
			if(buffer.length()<2)
				continue;
			count++;
			String[] ss=buffer.split("\t");
			if(ss.length!=2)
			{
				System.out.println("Error");
			}
			int forsum=Integer.parseInt(ss[1]);
			int[] forlenghts=new int[length+1];
			for(int i=0;i<forsum;i++)
			{
				buffer=br.readLine();
				String[] sss=buffer.split("\t");
				forlenghts[sss.length]++;
				lengths[sss.length]++;
			}
		}
		int sum=0;
		for(int i=0;i<lengths.length;i++)
		{
			System.out.print(lengths[i]+"\t");
			sum+=lengths[i];
		}
		System.out.println();
		for(int i=0;i<lengths.length;i++)
		{
			System.out.print(String.format("%.2f", (double)lengths[i]/(double)sum)+"\t");
			sum+=lengths[i];
		}
		System.out.println();		
	}
	
	
	public static void main(String[] args) throws NumberFormatException, IOException
	{
		FormulaCount fc=new FormulaCount();
		fc.ReadFile("F:\\Workspace\\KBCworkspace\\groundformulas\\fb15k_4.for");
	}
}
