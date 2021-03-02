package projects.cbrenet.nodes.tableEntry;

/**
 * Request
 */
public class Request implements Comparable<Request> {
	public int srcId;
	public int dstId;
	public double priority;
	
	public Request() {
		this.srcId = 0;
		this.dstId = 0;
		this.priority = 0.0;
	}
	
	public Request(int src, int dst) {
		this.srcId = src;
		this.dstId = dst;
		this.priority = 0.0;
	}
	
	public Request(int src, int dst, double pri) {
		this.srcId = src;
		this.dstId = dst;
		this.priority = pri;
	}

	@Override
	public int compareTo(Request o) {
		int value = Double.compare(this.priority, o.priority);
		if (value == 0) {
			return this.srcId - o.srcId;
		} else {
			return value;
		}
	}
}