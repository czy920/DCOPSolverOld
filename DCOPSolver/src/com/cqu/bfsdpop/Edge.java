package com.cqu.bfsdpop;

public class Edge {
	
	private Integer nodeA;
	private Integer nodeB;
	private Integer mutualAncestor;
	private Integer branchA;
	private Integer branchB;
		
	public Edge(Integer nodeA, Integer nodeB) {
		super();
		this.nodeA = nodeA;
		this.nodeB = nodeB;
	}

	public Edge(Integer nodeA, Integer nodeB, Integer mutualAncestor,
			Integer branchA, Integer branchB) {
		super();
		this.nodeA = nodeA;
		this.nodeB = nodeB;
		this.mutualAncestor = mutualAncestor;
		this.branchA = branchA;
		this.branchB = branchB;
	}

	public Integer getMutualAncestor() {
		return mutualAncestor;
	}

	public void setMutualAncestorAndBranches(Integer mutualAncestor, Integer branchA, Integer branchB) {
		this.mutualAncestor = mutualAncestor;
		this.branchA=branchA;
		this.branchB=branchB;
	}

	public Integer getBranchA() {
		return branchA;
	}

	public Integer getBranchB() {
		return branchB;
	}

	public Integer getNodeA() {
		return nodeA;
	}

	public Integer getNodeB() {
		return nodeB;
	}

	public boolean equals(Edge e1) {
		return (this.nodeA==e1.nodeA&&this.nodeB==e1.nodeB)||
				(this.nodeA==e1.nodeB&&this.nodeB==e1.nodeA);
	}
}
