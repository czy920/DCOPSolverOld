package com.cqu.tree;

/**
 * 遍历状态
 * @author hz
 *
 */
public enum IteratingStatus {
	
	/**
	 * 未开始
	 */
	INIT, 
	
	/**
	 * 遍历中
	 */
	ON, 
	
	/**
	 * 提前终止
	 */
	ENDEDAHEAD, 
	
	/**
	 * 正常结束
	 */
	FINISHED
}
