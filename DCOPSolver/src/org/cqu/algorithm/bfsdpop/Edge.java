package org.cqu.algorithm.bfsdpop;

public class Edge {
	
	protected Integer nodeA;
	protected Integer nodeB;
		
	public Edge(Integer nodeA, Integer nodeB) {
		super();
		this.nodeA = nodeA;
		this.nodeB = nodeB;
	}

	public Integer getNodeA() {
		return nodeA;
	}

	public Integer getNodeB() {
		return nodeB;
	}

	public boolean equals(Edge e1) {
		return (this.nodeA.equals(e1.nodeA)&&this.nodeB.equals(e1.nodeB)||
				(this.nodeA.equals(e1.nodeB)&&this.nodeB.equals(e1.nodeA)));
	}
}
