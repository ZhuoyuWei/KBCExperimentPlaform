package wzy.main.cpe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;

import wzy.model.TransE;
import wzy.model.para.TransEParameter;
import wzy.model.para.TransFParameter;
import wzy.model.para.TransHParameter;
import wzy.model.para.TransRParameter;
import wzy.thread.KBCProcess;

public class ProduceTripletsFileForMLN {
	public static TransEParameter SetTransEParameter(int entitydim,int relationdim)
	{
		TransEParameter ptranse=new TransEParameter();
		ptranse.setEntityDim(entitydim);
		ptranse.setRelationDim(relationdim);
		return ptranse;
	}
	
	public static TransHParameter SetTransHParameter(int entitydim,int relationdim)
	{
		TransHParameter ptranse=new TransHParameter();
		ptranse.setEntityDim(entitydim);
		ptranse.setRelationDim(relationdim);
		return ptranse;
	}	
	public static TransRParameter SetTransRParameter(int entitydim,int relationdim)
	{
		TransRParameter ptranse=new TransRParameter();
		ptranse.setEntityDim(entitydim);
		ptranse.setRelationDim(relationdim);
		return ptranse;
	}	
	
	public static TransFParameter SetTransFParameter(int entitydim,int relationdim)
	{
		TransFParameter ptranse=new TransFParameter();
		ptranse.setEntityDim(entitydim);
		ptranse.setRelationDim(relationdim);
		return ptranse;
	}		
	
	public static void main(String[] args)
	{
/*		//String dir="F:\\emnlp2015\\fb15k\\FB15k\\";
		//String dir="F:\\Workspace\\KBCworkspace\\dataset\\" +
			//	"wordnet-mlj12\\wordnet-mlj12\\";		
		String dir=args[0];
		
		//Read Three DB Files
		KBCProcess kbc_raw_tester=new KBCProcess();
		
		kbc_raw_tester.SetThreeTriplets(dir+"liuzhiyuan.train.txt"
				, dir+"liuzhiyuan.valid.txt"
				, dir+"liuzhiyuan.test.txt"
				, "\t");
		
		kbc_raw_tester.SetThreeTriplets(dir+"exp_train.txt"
				, dir+"exp_valid.txt"
				, dir+"exp_test.txt"
				, "\t");
		
		
		kbc_raw_tester.setEm(new TransE());
		//kbc_raw_tester.setPrint_model_file(dir+args[1]);
		kbc_raw_tester.setEmbedding_init_file(dir+args[1]);
		//kbc_raw_tester.setPath_structure_file(dir+args[2]);
		kbc_raw_tester.SetEmbeddingModelSpecificParameter(SetTransEParameter(50,50));
		
	
		kbc_raw_tester.getEm().SetBestParameter();
		kbc_raw_tester.getEm().setTrainprintable(false);
		
		kbc_raw_tester.Processing_produceForMLN();
		
		try {
			ProduceDataFromMap("/dev/shm/wm18candidateright");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		String[] files={"F:\\Workspace\\KBCworkspace\\wm18Andfb15k\\wm18\\exp_train.txt"
				,"F:\\Workspace\\KBCworkspace\\wm18Andfb15k\\wm18\\exp_valid.txt"};
		try {
			ProduceMLNTrainData(files,"F:\\Workspace\\KBCworkspace\\wm18Andfb15k\\wm18\\wm18mln.train");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void ProduceDataFromMap(String dirfile) throws IOException
	{
		File dir=new File(dirfile);
		File[] subdirs=dir.listFiles();
		for(int i=0;i<subdirs.length;i++)
		{
			BufferedReader br=new BufferedReader(new FileReader(subdirs[i].listFiles()[0].getPath()));
			PrintStream ps=new PrintStream(subdirs[i].getPath()+"/test.query");
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[\\s]+");
				String newline=ss[0]+"\t"+ss[1];
				ps.println(newline);
			}
			ps.flush();
			ps.close();
			br.close();
		}
	}
	
	public static void ProduceMLNTrainData(String filenames[],String outputfile) throws IOException
	{
		PrintStream ps=new PrintStream(outputfile);
		for(int i=0;i<filenames.length;i++)
		{
			BufferedReader br=new BufferedReader(new FileReader(filenames[i]));
			String buffer=null;
			while((buffer=br.readLine())!=null)
			{
				if(buffer.length()<2)
					continue;
				String[] ss=buffer.split("[\\s]+");
				ps.println(ss[1]+"("+ss[0]+","+ss[2]+")");
			}
			br.close();
		}
		ps.flush();
		ps.close();
	}
	
	//public static void SplitData()
	
}
