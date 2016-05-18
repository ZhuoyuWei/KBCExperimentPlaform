package wzy.main;

import java.text.SimpleDateFormat;
import java.util.Date;

import wzy.io.busi.ReadTriplets;
import wzy.thread.ConstructFormulas;
import wzy.thread.cons4rel.ConstrForRel;

public class MiningFomrulaStructures {

	
	
	
	public static void main(String[] args)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		System.err.println("Mining formulas process.\t"+df.format(new Date()));
		
		//String dir="C:\\Users\\Administrator\\Documents\\data\\wn18\\";
		String dir=args[0];
		
		ConstructFormulas cf=new ConstructFormulas();
		cf.setTrain_triplets(ReadTriplets.ReadTripletsFromFile(dir+"exp_train.txt", "\t"));
		int[] entityAndRelationSizes=ReadTriplets.StaticTrainSet(cf.getTrain_triplets());
		cf.setEntityNum(entityAndRelationSizes[0]);
		cf.setRelNum(entityAndRelationSizes[1]);
		cf.setMaxLength(Integer.parseInt(args[1])); //max path length:2 can run over, 3 is hard.
		//cf.setMaxLength(4);
		cf.setSearch_method(1); //DFS
		cf.setThreNum(128);
		cf.setPrintsupport(true);
		cf.setFormulaPrintFile(dir+"formulas");
		ConstrForRel.setFalse_triplet(Integer.parseInt(args[2]));
		ConstrForRel.setSupport_threthold(Integer.parseInt(args[3]));
		ConstrForRel.setConfidence_threthold(Double.parseDouble(args[4]));
		System.err.println("Mining is starting.");
		long start=System.currentTimeMillis();
		cf.Processing();
		long end=System.currentTimeMillis();
		System.err.println("The time of mining is "+(end-start)/1000+"s");
		
	}
}
