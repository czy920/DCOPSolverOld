package com.cqu.dpop;

public class ReductDimensionResult {
	
	public static final int REDUCT_DIMENSION_WITH_MAX=0;
	public static final int REDUCT_DIMENSION_WITH_MIN=1;
	
	private MultiDimensionData mdData;
	private int[] resultIndex;
	
	public ReductDimensionResult(MultiDimensionData mdData, int[] resultIndex) {
		super();
		this.mdData = mdData;
		this.resultIndex = resultIndex;
	}
	
	public MultiDimensionData getMdData() {
		return mdData;
	}
	
	public void setMdData(MultiDimensionData mdData) {
		this.mdData = mdData;
	}
	
	public int[] getResultIndex() {
		return resultIndex;
	}
	
	public void setResultIndex(int[] resultIndex) {
		this.resultIndex = resultIndex;
	}
}
