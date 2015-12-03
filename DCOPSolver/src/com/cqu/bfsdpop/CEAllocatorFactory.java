package com.cqu.bfsdpop;

import com.cqu.parser.Problem;

/**
 * 交叉边分配器构造工厂
 * @author CQU
 *
 */
public class CEAllocatorFactory {
	
	/**
	 * 返回cross edge allocator
	 * @param allocatorName
	 * @return 对应的分配器或null
	 */
	public static CrossEdgeAllocator getCrossEdgeAllocator(String allocatorName, Problem problem)
	{
		if(allocatorName.equals("CEAllocatorA"))
		{
			return new CEAllocatorA(problem);
		}else if(allocatorName.equals("CEAllocatorB"))
		{
			return new CEAllocatorB(problem);
		}else
		{
			return null;
		}
	}
}
