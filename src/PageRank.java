
public class PageRank {
	private String docID;
	private double pageRank;
	
	public PageRank(String docID, double pageRank) {
		this.docID = docID;
		this.pageRank = pageRank;
	}
	
	public String getDocID() {
		return docID;
	}
	public void setDocID(String docID) {
		this.docID = docID;
	}
	public double getPageRank() {
		return pageRank;
	}
	public void setPageRank(double pageRank) {
		this.pageRank = pageRank;
	}
}
