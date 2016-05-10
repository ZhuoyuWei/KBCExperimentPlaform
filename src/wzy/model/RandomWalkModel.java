package wzy.model;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import wzy.io.FileTools;
import wzy.io.busi.ReadTriplets;
import wzy.meta.FormulaForest;
import wzy.meta.RPath;
import wzy.meta.TripletHash;

public class RandomWalkModel {

	public int[][] relation_trans_matrix=null;
	public int entityNum=0;
	public int relationNum=0;
	
	public int[][] train_triplets;
	public int[][] validate_triplets;
	public int[][] test_triplets;
	
	
	public RPath[][] rpathLists=null;
	public double[][] pathWeights;
	public double[][] pathWeightGradients;
	
	protected int Epoch=600;
	protected int minibranchsize=4800;
	protected double gamma=0.01;
	protected double margin=1.;
	protected int random_data_each_epoch=100000;
	protected Random rand=new Random();
	protected boolean trainprintable=false;
	protected boolean bern=false;
	protected Set<TripletHash> filteringSet; 
	
	public FormulaForest[] ff;
	
	public boolean l2_flag=true;
	public double l2_C=1;
	public double learning_rate=0.01;
	public int false_triplet_size=1;
	
	public int[][][] triplet_graph;
	public boolean nocount=true;
	
	//TransE embedding
	public EmbeddingModel em;
	
	//debug
	public int train_true_count=0;
	public int train_false_count=0;
	
	
	public void InitPathWeights()
	{
		pathWeights=new double[relationNum][];
		for(int i=0;i<relationNum;i++)
		{
			pathWeights[i]=new double[rpathLists[i].length+1];
			//random init
			for(int j=0;j<pathWeights[i].length;j++)
			{
				//pathWeights[i][j]=rand.nextDouble();
				
				//init for transE
				pathWeights[i][j]=0.;
				if(j==pathWeights[i].length-1)
					pathWeights[i][j]=1.;
			}
		}
	}
	public void InitPathWeightsByDef()
	{
		InitPathWeights();
		for(int i=0;i<rpathLists.length;i++)
		{
			for(int j=0;j<rpathLists[i].length;j++)
			{
				pathWeights[i][j]=rpathLists[i][j].getWeight();
			}
		}
	}
	
	public void ReadFormulas(String filename)
	{
		if(relationNum>0)
		{
			relation_trans_matrix=FileTools.ReadFormulas_RelationMatrix(filename, relationNum);
			rpathLists=FileTools.ReadFormulasForRelations(filename, relationNum);
			ff=new FormulaForest[relationNum];
			for(int i=0;i<relationNum;i++)
			{
				ff[i]=new FormulaForest();
				ff[i].BuildForest(rpathLists[i], relationNum);
			}
		}
	}
	
	public void Training(int[][] train_triplets,int[][] validate_triplets)
	{
		if(this.filteringSet==null)
			BuildTrainAndValidTripletSet(train_triplets,test_triplets);
		int branch=train_triplets.length/minibranchsize;
		
		//if(train_triplets.length%minibranchsize>0) //if the size of minibranch didn't touch minibranch size
			//branch++;
		double lasttrain_point_err=Double.MAX_VALUE;
		double lasttrain_pair_err=Double.MAX_VALUE;				
		double lastvalid_point_err=Double.MAX_VALUE;
		double lastvalid_pair_err=Double.MAX_VALUE;	
		for(int epoch=0;epoch<Epoch;epoch++)
		{
			//Disrupt the order of training data set
			long start=System.currentTimeMillis();
			for(int i=0;i<random_data_each_epoch;i++)
			{
				int a=Math.abs(rand.nextInt())%train_triplets.length;
				int b=Math.abs(rand.nextInt())%train_triplets.length;	
				int[] t=train_triplets[a];
				train_triplets[a]=train_triplets[b];
				train_triplets[b]=t;
			}
			
			//wzy debug 5.6
			train_true_count=0;
			train_false_count=0;
			
			for(int i=0;i<branch;i++)
			{
				int sindex=i*minibranchsize;
				int eindex=(i+1)*minibranchsize-1;
				if(eindex>=train_triplets.length)
					eindex=train_triplets.length-1;
				//InitGradients();
				OneBranchTraining(train_triplets,sindex,eindex);	
			}
			long end=System.currentTimeMillis();
			if(trainprintable)
			{
				
			}
			else
			{
				System.err.println("Epoch "+epoch+" is end at "+(end-start)/1000+"s");
			}
			
		}
	}
	
	public void PrintPathsWeight(String filename)
	{
		List<Object> weightlist=new ArrayList<Object>();
		weightlist.add(pathWeights);
		FileTools.PrintEmbeddingList(filename, weightlist);
	}
	
	
	protected int[] copyints(int[] s)
	{
		int[] r=new int[s.length];
		for(int i=0;i<s.length;i++)
			r[i]=s[i];
		return r;
	}
	/**
	 * Generate a false triplet from the true one, it can be bern or unif, which is depend on 'bern' variable.
	 * @param triplet
	 * @return
	 */
	protected int[] GenerateFalseTriplet(int[] triplet)
	{
		//double pr=0.5;
		double pr=1.;
		
		TripletHash falseTri=new TripletHash();
		falseTri.setTriplet(copyints(triplet));
		if(rand.nextDouble()<pr)
		{
			while(filteringSet.contains(falseTri)||falseTri.getTriplet()[2]<0)
				falseTri.getTriplet()[2]=(Math.abs(rand.nextInt()))%entityNum;
		}
		else
		{
			while(filteringSet.contains(falseTri)||falseTri.getTriplet()[0]<0)
				falseTri.getTriplet()[0]=(Math.abs(rand.nextInt()))%entityNum;			
		}
		return falseTri.getTriplet();
	}
	
	
	/**
	 * Form a set of all true triplets in train and validate set.
	 * It is useful in testing or other functions.
	 * @param train_triplets
	 * @param valid_triplets
	 * @return
	 */
	public void BuildTrainAndValidTripletSet(int[][] train_triplets,int[][] valid_triplets)
	{
		filteringSet=new HashSet<TripletHash>();
		int[][][] triplets=new int[2][][];
		triplets[0]=train_triplets;
		triplets[1]=valid_triplets;
		for(int m=0;m<2;m++)
		{
			for(int i=0;i<triplets[m].length;i++)
			{
				TripletHash tri=new TripletHash();
				tri.setTriplet(triplets[m][i]);
				filteringSet.add(tri);
			}
		}
	}
	public void TestCandidatesForRel(String dir,int candidate_size,String separator)
	{
		//debug
		this.train_true_count=0;		
		this.train_false_count=0;
		for(int i=0;i<relationNum;i++)
		{
			this.test_triplets=ReadTriplets.ReadTripletsFromFile(dir+"/0/"+i,separator);
			System.out.println("Relation "+i);
			TestAllCandidates(dir+"/1/"+i,candidate_size);
		}
	}
	
	public void TestAllCandidates(String filename,int candidate_size)
	{
		if(this.filteringSet==null)
			BuildTrainAndValidTripletSet(train_triplets,validate_triplets);
		//|test_triplets|*|2(left,right)|*|Top100|
		int[][][] test_cans=FileTools.ReadTestAllCandidates(filename,
				test_triplets.length, candidate_size);
		if(test_cans.length!=test_triplets.length)
		{
			System.err.println("different triplets for test error");
			System.exit(-1);
		}
		
		int[] hits1_r=new int[2];
		int[] hits10_r=new int[2];
		int[] mean_r=new int[2];			
		int[] hits1_f=new int[2];
		int[] hits10_f=new int[2];		
		int[] mean_f=new int[2];
		int[] noin=new int[2];
	
		
		for(int i=0;i<test_cans.length;i++)
		{
			int[] triplet=test_triplets[i];
			double[] fcounts=RandomWalk(triplet);
			train_true_count+=CheckRandomRes(fcounts);
			/*if(train_true_count>0)
			{
				System.out.println("debug > 0 ");
			}*/
			double ans_score=Logistic_F_wx(triplet[1],fcounts);
			for(int j=0;j<2;j++)
			{
				int ans_index=-1;
				for(int k=0;k<candidate_size;k++)
				{
					if((j==0&&test_cans[i][j][k]==triplet[2])||test_cans[i][j][k]==triplet[0])
					{
						ans_index=k;
						break;
					}
				}
				if(ans_index<0)
				{
					mean_r[j]+=candidate_size;
					mean_f[j]+=candidate_size;
					noin[j]++;
					continue;
				}
				
				int[] can_tri=new int[3];
				for(int k=0;k<3;k++)
					can_tri[k]=triplet[k];
				
				
				int rcount=1;
				int fcount=1;
				for(int k=0;k<candidate_size;k++)
				{			
					if(k==ans_index)
						continue;
					if(j==0)
					{
						can_tri[2]=test_cans[i][j][k];
					}
					else
					{
						can_tri[0]=test_cans[i][j][k];						
					}
					
					fcounts=RandomWalk(can_tri);
					train_false_count+=CheckRandomRes(fcounts);
					double score=Logistic_F_wx(can_tri[1],fcounts);
					if(ans_score<=score)
					{
						rcount++;
						TripletHash tri=new TripletHash();
						tri.setTriplet(can_tri);
						if(!filteringSet.contains(tri))
							fcount++;
					}
				}	
				
				if(rcount<=10)
					hits10_r[j]++;
				if(rcount<=1)
					hits1_r[j]++;				
				mean_r[j]+=rcount;

				if(fcount<=10)
					hits10_f[j]++;
				if(fcount<=1)
					hits1_f[j]++;				
				mean_f[j]+=fcount;					
			}
		}
		
		int[][] tri_sum=new int[2][2];
		tri_sum[0][0]=test_triplets.length;
		tri_sum[0][1]=test_triplets.length;	
		tri_sum[1][0]=test_triplets.length-noin[0];
		tri_sum[1][1]=test_triplets.length-noin[1];			
		
		for(int m=0;m<2;m++)
		{
			for(int i=0;i<2;i++)
				System.out.println(hits10_r[i]/(double)tri_sum[m][i]+
					"\t"+hits1_r[i]/(double)tri_sum[m][i]+
					"\t"+mean_r[i]/(double)tri_sum[m][i]+
					"\t"+hits10_f[i]/(double)tri_sum[m][i]+
					"\t"+hits1_f[i]/(double)tri_sum[m][i]+
					"\t"+mean_f[i]/(double)tri_sum[m][i]);
			System.out.println((hits10_r[0]+hits10_r[1])/(double)(tri_sum[m][0]+tri_sum[m][1])+
					"\t"+(hits1_r[0]+hits1_r[1])/(double)(tri_sum[m][0]+tri_sum[m][1])+
					"\t"+(mean_r[0]+mean_r[1])/(double)(tri_sum[m][0]+tri_sum[m][1])+
					"\t"+(hits10_f[0]+hits10_f[1])/(double)(tri_sum[m][0]+tri_sum[m][1])+
					"\t"+(hits1_f[0]+hits1_f[1])/(double)(tri_sum[m][0]+tri_sum[m][1])+
					"\t"+(mean_f[0]+mean_f[1])/(double)(tri_sum[m][0]+tri_sum[m][1]));
		}
		
		
	}
	
	public void PrintTopWeightFormula(String filename,int TOPN,String dictfile)
	{
		String[] relationdict=null;
		if(dictfile!=null)
		{
			relationdict=new String[relationNum];
			try {
				BufferedReader br=new BufferedReader(new FileReader(dictfile));
				String buffer=null;
				int i=0;
				while((buffer=br.readLine())!=null)
				{
					String[] ss=buffer.split("[\\s]+");
					if(ss.length!=2)
						continue;
					relationdict[i++]=ss[0];
				}
				br.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i=0;i<relationNum;i++)
				System.out.println(i+"\t"+relationdict[i]);
			
		}
			
		
		class FormulaIndexWithWeight implements Comparator
		{
			public int index;
			public double weight;
			@Override
			public int compare(Object o1, Object o2) {
				// TODO Auto-generated method stub
				FormulaIndexWithWeight f1=(FormulaIndexWithWeight)o1;
				FormulaIndexWithWeight f2=(FormulaIndexWithWeight)o2;	
				if(Math.abs(f1.weight-f2.weight)<1e-10)
					return 0;
				else if(f1.weight>f2.weight)
					return -1;
				else 
					return 1;
			}
		}
		try {
			PrintStream ps=new PrintStream(filename);
			for(int i=0;i<rpathLists.length;i++)
			{
				List<FormulaIndexWithWeight> list=new ArrayList<FormulaIndexWithWeight>();
				for(int j=0;j<rpathLists[i].length;j++)
				{
					FormulaIndexWithWeight f=new FormulaIndexWithWeight();
					f.index=j;
					f.weight=pathWeights[i][j];
					list.add(f);
				}
				Collections.sort(list,new FormulaIndexWithWeight());
				ps.println("Relation: "+relationdict!=null?relationdict[i]:i);
				int topn=TOPN<list.size()?TOPN:list.size();
				for(int j=0;j<topn;j++)
				{
					RPath path=rpathLists[i][list.get(j).index];
					for(int k=0;k<path.length();k++)
					{
						ps.print((relationdict!=null?relationdict[path.GetElement(k)]:path.GetElement(k))+"\t");
					}
					ps.println(list.get(j).weight);
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	//*****************all below are need overwritten****************************
	public void InitGradients()
	{
		this.pathWeightGradients
			=new double[pathWeights.length][];
		for(int i=0;i<relationNum;i++)
			this.pathWeightGradients[i]=new double[this.pathWeights[i].length];
		
	}
	
	public void OneBranchTraining(int[][] train_triplets,int sindex,int eindex)
	{
		InitGradients();
		for(int i=sindex;i<=eindex;i++)
		{			
			double[] true_paths_count=RandomWalk(train_triplets[i]);
			train_true_count+=CheckRandomRes(true_paths_count);
			double true_f_wx=Logistic_F_wx(train_triplets[i][1],true_paths_count);
			Logistic_Grident(train_triplets[i][1],true_paths_count,true_f_wx-1);
			//false
			for(int j=0;j<false_triplet_size;j++)
			{
				int[] false_triplet=GenerateFalseTriplet(train_triplets[i]);
				double[] false_paths_count=RandomWalk(false_triplet);
				train_false_count+=CheckRandomRes(false_paths_count);
				double false_f_wx=Logistic_F_wx(false_triplet[1],false_paths_count);
				Logistic_Grident(false_triplet[1],false_paths_count,false_f_wx);
			}
			
		}
		//Add L2 norm
		if(l2_flag)
		{
			for(int i=0;i<pathWeightGradients.length;i++)
			{
				for(int j=0;j<pathWeightGradients[i].length;j++)
				{
					//pathWeightGradients[i][j]+=2*pathWeights[i][j];
					//L1
					if(Math.abs(pathWeights[i][j])<1e-6)
						continue;
					if(pathWeights[i][j]>0)
						pathWeightGradients[i][j]+=l2_C;
					else
						pathWeightGradients[i][j]-=l2_C;
				}
			}
		}
		
		UpdateWeights();
		
	}
	
	public void UpdateWeights()
	{
		for(int i=0;i<pathWeightGradients.length;i++)
		{
			for(int j=0;j<pathWeightGradients[i].length;j++)
			{
				pathWeights[i][j]-=learning_rate*pathWeightGradients[i][j];
			}
		}
		
		//l1-norm
		if(true)
		{
			for(int i=0;i<pathWeightGradients.length;i++)
			{
				for(int j=0;j<pathWeightGradients[i].length;j++)
				{
					if(Math.abs(pathWeights[i][j])<1e-6)
						pathWeights[i][j]=0;
				}
			}			
		}
		
	}
	
	public double Logistic_F_wx(int r,double[] fcounts)
	{

		double wx=F_wx(r,fcounts);
		double exp_wx=Math.exp(-wx);
		//return exp_wx/(1+exp_wx);
		return 1/(1+exp_wx);
	}
	
	public double F_wx(int r,double[] fcounts)
	{
		double wx=0;
		for(int i=0;i<fcounts.length;i++)
		{
			if(fcounts[i]==0)
				continue;
			wx+=pathWeights[r][i]*fcounts[i];
		}
		return wx;
	}
	
	public void Logistic_Grident(int r,double[] fcounts,double f_wx_y)
	{
		for(int i=0;i<fcounts.length;i++)
		{
			if(fcounts[i]==0)
				continue;
			pathWeightGradients[r][i]+=(f_wx_y)*fcounts[i];
		}
	}
	
	public double[] RandomWalk(int[] triplet)
	{
		double[] fcounts=new double[pathWeights[triplet[1]].length];
		if(em!=null)
			fcounts[fcounts.length-1]=-em.CalculateSimilarity(triplet);
		else
			fcounts[fcounts.length-1]=1;			
		return fcounts;
	}
	
	public void NoCount(double[] scr)
	{
		for(int i=0;i<scr.length;i++)
			if(scr[i]>1e-06)
				scr[i]=1;
	}

	public int CheckRandomRes(double[] res)
	{
		int sum=0;
		for(int i=0;i<res.length-1;i++)
		{
			if(res[i]>1e-6)
				sum+=1;
		}
		//return sum;
		if(sum>0)
			return 1;
		else
			return 0;
	}
	

	 
	
}
