package pre_data;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import wzy.model.TransE;
import wzy.thread.KBCProcess;

public class ProduceReverseKB {

	
	public int entityNum;
	public int relationNum;
	
	public int[][] ReverseKB(int[][] triplets)
	{
		int[][] reversetriplets=new int [triplets.length][3];
		for(int i=0;i<triplets.length;i++)
		{
			for(int j=0;j<3;j++)
			{
				reversetriplets[i][j]=triplets[i][2-j];
			}
			reversetriplets[i][1]+=relationNum;
		}
		return reversetriplets;
	}
	
	public void PrintDataSet(String filename,int[][] triplets,int[][] reversetriplets) throws FileNotFoundException
	{
		int[][][] data=new int[2][][];
		data[0]=triplets;
		data[1]=reversetriplets;
		PrintStream ps=new PrintStream(filename);
		for(int i=0;i<data.length;i++)
		{
			for(int j=0;j<data[i].length;j++)
			{
				for(int k=0;k<data[i][j].length;k++)
				{
					ps.print(data[i][j][k]+"\t");
				}
				ps.println();
			}
		}
		ps.close();
	}
	
	public static void main(String[] args) throws FileNotFoundException
	{
		String dir="C:\\Users\\Administrator\\Documents\\data\\fb15k_doubledirect\\";
		KBCProcess kbc=new KBCProcess();
		kbc.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		kbc.setEm(new TransE());
		kbc.StatisticTrainingSet();
		
		
		
		ProduceReverseKB prKB=new ProduceReverseKB();
		prKB.entityNum=kbc.getEm().getEntityNum();
		prKB.relationNum=kbc.getEm().getRelationNum();
		
		
		int[][] rtrain=prKB.ReverseKB(kbc.getTrain_triplets());
		int[][] rvalid=prKB.ReverseKB(kbc.getValidate_triplets());
		int[][] rtest=prKB.ReverseKB(kbc.getTest_triplets());
		
		prKB.PrintDataSet(dir+"new_train.txt", kbc.getTrain_triplets(),rtrain);
		prKB.PrintDataSet(dir+"new_valid.txt", kbc.getValidate_triplets(),rvalid);		
		prKB.PrintDataSet(dir+"new_test.txt", kbc.getTest_triplets(),rtest);		
	}
}
