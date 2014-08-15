package com.cqu.dpop;

public class ReductDimensionResult {
	
	public static final int REDUCT_DIMENSION_WITH_MAX=0;
	public static final int REDUCT_DIMENSION_WITH_MIN=1;
	
	private MultiDimensionDataB mdData;
	private int[] resultIndex;
	
	public ReductDimensionResult(MultiDimensionDataB mdData, int[] resultIndex) {
		super();
		this.mdData = mdData;
		this.resultIndex = resultIndex;
	}
	
	public MultiDimensionDataB getMdData() {
		return mdData;
	}
	
	public void setMdData(MultiDimensionDataB mdData) {
		this.mdData = mdData;
	}
	
	public int[] getResultIndex() {
		return resultIndex;
	}
	
	public void setResultIndex(int[] resultIndex) {
		this.resultIndex = resultIndex;
	}
}
