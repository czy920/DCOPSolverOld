package com.cqu.hybridmbdpop;

import java.util.Collections;
import java.util.List;

import com.cqu.core.Context;
import com.cqu.dpop.Dimension;

public class ContextWrapped {
	
	private Context context;
	private List<Dimension> dimensions;
	private int[] valueIndexes;
	private int currentDimension;
	
	public ContextWrapped(Context context, List<Dimension> dimensions) {
		// TODO Auto-generated constructor stub
		this.context=context;
		this.dimensions=dimensions;
		Collections.sort(this.dimensions);

		valueIndexes=new int[dimensions.size()];
		this.currentDimension=valueIndexes.length-1;
	}
	
	/**
	 * 下一个组合
	 * @return
	 */
	public boolean next()
	{
		valueIndexes[currentDimension]++;
		context.addOrUpdate(Integer.parseInt(dimensions.get(currentDimension).getName()), valueIndexes[currentDimension]);
		while(valueIndexes[currentDimension]>=dimensions.get(currentDimension).getSize())
		{
			valueIndexes[currentDimension]=0;
			context.addOrUpdate(Integer.parseInt(dimensions.get(currentDimension).getName()), valueIndexes[currentDimension]);
			currentDimension--;
			if(currentDimension<0)
			{
				return false;
			}
			valueIndexes[currentDimension]++;
			context.addOrUpdate(Integer.parseInt(dimensions.get(currentDimension).getName()), valueIndexes[currentDimension]);
		}
		return true;
	}
	
	public int getValueIndex(Integer id)
	{
		return this.context.get(id);
	}
}
