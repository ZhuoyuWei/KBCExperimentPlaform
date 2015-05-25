package wzy.model.para;

public class TransHParameter extends SpecificParameter {
	
	private int entityDim;
	private int relationDim;
	
	public int getEntityDim() {
		return entityDim;
	}
	public void setEntityDim(int entityDim) {
		this.entityDim = entityDim;
	}
	public int getRelationDim() {
		return relationDim;
	}
	public void setRelationDim(int relationDim) {
		this.relationDim = relationDim;
	}
}
