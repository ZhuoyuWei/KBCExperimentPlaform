package test;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;

public class ProduceRanomForTransR {

	
	public static void main(String[] args) throws FileNotFoundException
	{

		int entityNum=14951;
		int relationNum=1345;
		int dim=100;
		Random rand=new Random();
		
		double[][] entityEmbedding=new double[entityNum][dim];
		double[][] relationEmbedding=new double[relationNum][dim];
		
		for(int i=0;i<entityNum;i++)
		{
			double x=0.;
			for(int j=0;j<dim;j++)
			{
				entityEmbedding[i][j]=rand.nextDouble();
				x+=entityEmbedding[i][j];
				if(rand.nextDouble()>0.5)
					entityEmbedding[i][j]=-entityEmbedding[i][j];
			}
			if(x>1)
			{
				for(int j=0;j<dim;j++)
				{
					entityEmbedding[i][j]/=x;
				}
			}
			
		}
		for(int i=0;i<relationNum;i++)
		{
			double x=0.;
			for(int j=0;j<dim;j++)
			{
				relationEmbedding[i][j]=rand.nextDouble();
				x+=relationEmbedding[i][j];
				if(rand.nextDouble()>0.5)
					relationEmbedding[i][j]=-relationEmbedding[i][j];
			}
			if(x>1)
			{
				for(int j=0;j<dim;j++)
				{
					relationEmbedding[i][j]/=x;
				}
			}
		}
		
		PrintStream ps=new PrintStream("entity2vector.rand");
		for(int i=0;i<entityEmbedding.length;i++)
		{
			for(int j=0;j<dim;j++)
			{
				ps.print(entityEmbedding[i][j]+"\t");
			}
			ps.println();
		}
		ps.close();
		ps=new PrintStream("relation2vector.rand");
		for(int i=0;i<relationEmbedding.length;i++)
		{
			for(int j=0;j<dim;j++)
			{
				ps.print(relationEmbedding[i][j]+"\t");
			}
			ps.println();
		}
		ps.close();
	}
	
	
	
	
}
