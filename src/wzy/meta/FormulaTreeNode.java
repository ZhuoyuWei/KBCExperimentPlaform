package wzy.meta;

import java.util.List;
import java.util.Map;

public class FormulaTreeNode {

	public boolean leaf=false;
	public int rel;
	public int formula;
	public FormulaTreeNode[] next_map;
	public FormulaTreeNode parent=null;
	public List<Integer> next_list;
	
}
