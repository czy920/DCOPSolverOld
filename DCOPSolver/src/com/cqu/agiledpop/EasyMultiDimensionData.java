package com.cqu.agiledpop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.cqu.dpop.Dimension;
import com.cqu.dpop.MultiDimensionData;
import com.cqu.dpop.ReductDimensionResult;
import com.cqu.util.CollectionUtil;

/**
 * 最大维度为2
 * @author Administrator
 *
 */
public class EasyMultiDimensionData {
	
	private Dimension dimen1;
	private Dimension dimen2;
	private int[] data;

	public EasyMultiDimensionData(Dimension dimen1, Dimension dimen2, int[] data) {
		super();
		if(dimen1.getPriority()<dimen2.getPriority())
		{
			this.dimen1 = dimen1;
			this.dimen2 = dimen2;
		}else
		{
			this.dimen1 = dimen2;
			this.dimen2 = dimen1;
		}
		this.data = data;
	}
	
	public boolean isDimen1Reductable()
	{
		return this.dimen1.isReductable();
	}
	
	public boolean isDimen2Reductable()
	{
		return this.dimen2.isReductable();
	}
	
	public EasyMultiDimensionData reductDimension()
	{
		if(this.dimen2!=null)
		{
			int[] dataNew=new int[dimen1.getSize()];
			int[] resultIndexes=new int[dimen1.getSize()];
			for(int i=0;i<dimen1.getSize();i++)
			{
				int min=Integer.MIN_VALUE;
				for(int j=0;j<dimen2.getSize();j++)
				{
					if(min>data[i*dimen2.getSize()+j])
					{
						min=data[i*dimen2.getSize()+j];
						resultIndexes[i]=j;
					}
				}
				dataNew[i]=min;
			}
			this.data=dataNew;
			return new EasyMultiDimensionData(new Dimension(dimen1), null, resultIndexes);
		}else
		{
			int min=Integer.MIN_VALUE;
			int index=0;
			for(int i=0;i<dimen1.getSize();i++)
			{
				if(min>data[i])
				{
					min=data[i];
					index=i;
				}
			}
			this.data=new int[]{min};
			return new EasyMultiDimensionData(null, null, new int[]{index});
		}
	}
	
	public static EasyMultiDimensionData[] mergeAndSwap(EasyMultiDimensionData emdData1, EasyMultiDimensionData emdData2)
	{
		return null;
	}
	
	public static EasyMultiDimensionData mergeAndReduct(EasyMultiDimensionData emdData1, EasyMultiDimensionData emdData2)
	{
		return null;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return CollectionUtil.arrayToString(data);
	}
	
	public int size()
	{
		return this.data.length*4;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		EasyMultiDimensionData emdDataA=new EasyMultiDimensionData(new Dimension("A1", 2, 0), new Dimension("A2", 3, 1), new int[]{1, 0, 5, 3, 4, 2});
		EasyMultiDimensionData emdDataB=new EasyMultiDimensionData(new Dimension("A2", 3, 1), new Dimension("A3", 2, 2), new int[]{3, 4, 2, 5, 1, 2});
		//System.out.println(emdDataA.mergeDimension(mdDataB).toString());
	}
}
