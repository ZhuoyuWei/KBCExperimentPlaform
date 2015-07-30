package wzy.meta;

public class EntityInfo {

	private int id;
	private int[][] neighbour_triplet;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int[][] getNeighbour_triplet() {
		return neighbour_triplet;
	}
	public void setNeighbour_triplet(int[][] neighbour_triplet) {
		this.neighbour_triplet = neighbour_triplet;
	}
	
	
}
