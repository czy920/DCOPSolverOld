package com.cqu.bfsdpop;

public class Edge {
	
	private Integer idNodeA;
	private Integer idNodeB;
		
	public Edge(Integer idNodeA, Integer idNodeB) {
		super();
		this.idNodeA = idNodeA;
		this.idNodeB = idNodeB;
	}
	
	public Integer getIdNodeA() {
		return idNodeA;
	}
	
	public Integer getIdNodeB() {
		return idNodeB;
	}
	
	public boolean equals(Edge e1) {
		return (this.idNodeA==e1.idNodeA&&this.idNodeB==e1.idNodeB)||
				(this.idNodeA==e1.idNodeB&&this.idNodeB==e1.idNodeA);
	}
}
