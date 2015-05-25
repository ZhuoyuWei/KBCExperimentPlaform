package wzy.tool;

public class MatrixTool {

	public static double[][] MatrixMulti(double[][] a,double[][] b)
	{
		if(a[0].length!=b.length)
			return null;
		
		double[][] result=new double[a.length][b[0].length];
		
		for(int i=0;i<a.length;i++)
		{
			for(int j=0;j<b[0].length;j++)
			{
				for(int k=0;k<b.length;k++)
				{
					result[i][j]+=a[i][k]*b[k][j];
				}
			}
		}
		
		return result;
	}
	
	public static double[] VectorRightDotMatrix(double[] v,double [][] m)
	{
		if(v.length!=m.length)
			return null;
		double[] results=new double[m[0].length];
		for(int i=0;i<results.length;i++)
		{
			for(int j=0;j<v.length;j++)
			{
				results[i]+=v[j]*m[j][i];
			}
		}
		return results;
	}
	
	public static double DotMulti(double[][] a,double[][] b)
	{
		if(a.length!=b.length||a[0].length!=b[0].length)
			return -1.;
		double sum=0;
		for(int i=0;i<a.length;i++)
		{
			for(int j=0;j<a[0].length;j++)
			{
				sum+=a[i][j]*b[i][j];
			}
		}
		return sum;
	}
	
	public static double VectorDot(double[] a,double[] b)
	{
		double score=0.;
		for(int i=0;i<a.length;i++)
		{
			score+=a[i]*b[i];
		}
		return score;
	}
	
	public static double VectorNorm2(double[] vec)
	{
		double norm=0;
		for(int i=0;i<vec.length;i++)
		{
			norm+=Math.pow(vec[i],2.);
		}
		return norm;
	}
	
	public static double VectorNorm1(double[] vec)
	{
		double norm=0;
		for(int i=0;i<vec.length;i++)
		{
			norm+=Math.abs(vec[i]);
		}
		return norm;
	}	
	public static double MatrixNorm2(double[][] mat)
	{
		double norm=0;
		for(int i=0;i<mat.length;i++)
		{
			for(int j=0;j<mat[i].length;j++)
			{
				norm+=mat[i][j]*mat[i][j];
			}
		}
		return norm;
	}
	public static double MatrixNorm1(double[][] mat)
	{
		double norm=0;
		for(int i=0;i<mat.length;i++)
		{
			for(int j=0;j<mat[i].length;j++)
			{
				norm+=Math.abs(mat[i][j]);
			}
		}
		return norm;		
	}
	
	public static double[] CopyVector(double[] s)
	{
		double[] d=new double[s.length];
		for(int i=0;i<s.length;i++)
		{
			d[i]=s[i];
		}
		return d;
	}
	public static double[][] CopyMatrix(double[][] s)
	{
		double[][] d=new double[s.length][];
		for(int i=0;i<s.length;i++)
		{
			d[i]=CopyVector(s[i]);
		}
		return d;
	}
}
