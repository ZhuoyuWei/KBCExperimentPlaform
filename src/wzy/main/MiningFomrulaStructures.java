package wzy.main;

import java.text.SimpleDateFormat;
import java.util.Date;

import wzy.io.busi.ReadTriplets;
import wzy.thread.ConstructFormulas;

public class MiningFomrulaStructures {

	
	
	
	public static void main(String[] args)
	{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		System.err.println("Mining formulas process.\t"+df.format(new Date()));
		
		//String dir="F:\\emnlp2015\\fb15k\\FB15k\\";
		String dir=args[0];
		
		ConstructFormulas cf=new ConstructFormulas();
		cf.setTrain_triplets(ReadTriplets.ReadTripletsFromFile(dir+"liuzhiyuan.train.txt", "\t"));
		int[] entityAndRelationSizes=ReadTriplets.StaticTrainSet(cf.getTrain_triplets());
		cf.setEntityNum(entityAndRelationSizes[0]);
		cf.setRelNum(entityAndRelationSizes[1]);
		cf.setMaxLength(Integer.parseInt(args[1])); //max path length:2 can run over, 3 is hard.
		cf.setSearch_method(0); //BFS
		cf.setThreNum(24);
		cf.setPrintsupport(true);
		cf.setFormulaPrintFile(dir+"formulas");
		System.err.println("Mining is starting.");
		cf.Processing();
		
	}
}
