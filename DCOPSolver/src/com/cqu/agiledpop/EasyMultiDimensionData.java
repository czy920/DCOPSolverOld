package com.cqu.agiledpop;

import com.cqu.dpop.Dimension;
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
		if(dimen1!=null&&dimen2!=null)
		{
			if(dimen1.getPriority()<dimen2.getPriority())
			{
				this.dimen1 = dimen1;
				this.dimen2 = dimen2;
			}else
			{
				this.dimen1 = dimen2;
				this.dimen2 = dimen1;
			}
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
	
	public boolean exist(String dimenName)
	{
		return dimen1.equals(dimenName)||dimen2.equals(dimenName);
	}
	
	/**
	 * 降维，降优先级低的维度,dimen1>dimen2
	 * @return
	 */
	public EasyMultiDimensionData reductDimension()
	{
		if(dimen2!=null)
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
		}else if(dimen1!=null)
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
		}else
		{
			return null;
		}
	}
	
	/**
	 * 并维，比如A1A2+A1=A1A2，并维后不超过2维
	 * @param emdData2
	 * @return
	 */
	public EasyMultiDimensionData merge(EasyMultiDimensionData emdData2)
	{
		if(dimen2!=null)
		{
			if(emdData2.dimen2!=null)
			{
				if(dimen1.equals(emdData2.dimen1)==true&&dimen2.equals(emdData2.dimen2)==true)
				{
					//A1A2+A1A2=A1A2
					int[] dataNew=new int[data.length];
					for(int i=0;i<dimen1.getSize();i++)
					{
						for(int j=0;j<dimen2.getSize();j++)
						{
							dataNew[i*dimen2.getSize()+j]=data[i*dimen2.getSize()+j]+emdData2.data[i*dimen2.getSize()+j];
						}
					}
					return new EasyMultiDimensionData(this.constructMergedDimension(dimen1, emdData2.dimen1), 
							this.constructMergedDimension(dimen2, emdData2.dimen2), dataNew);
				}
			}else if(emdData2.dimen1!=null)
			{
				if(dimen2.equals(emdData2.dimen1)==true)
				{
					//A1A2+A2=A1A2
					int[] dataNew=this.mergeDataWithCondition(data, dimen1.getSize(), dimen2.getSize(), 
							emdData2.data, emdData2.dimen1.getSize(), false);
					return new EasyMultiDimensionData(new Dimension(dimen1), this.constructMergedDimension(dimen2, emdData2.dimen1), dataNew);
				}else if(dimen1.equals(emdData2.dimen1)==true)
				{
					//A1A2+A1=A1A2
					int[] dataNew=this.mergeDataWithCondition(data, dimen1.getSize(), dimen2.getSize(), 
							emdData2.data, emdData2.dimen1.getSize(), true);
					return new EasyMultiDimensionData(this.constructMergedDimension(dimen1, emdData2.dimen1), new Dimension(dimen2), dataNew);
				}
			}
		}else if(dimen1!=null)
		{
			if(emdData2.dimen2!=null)
			{
				if(dimen1.equals(emdData2.dimen2)==true)
				{
					//A2+A1A2=A1A2
					int[] dataNew=this.mergeDataWithCondition(emdData2.data, emdData2.dimen1.getSize(), emdData2.dimen2.getSize(), 
							data, dimen1.getSize(), false);
					return new EasyMultiDimensionData(new Dimension(dimen1), this.constructMergedDimension(emdData2.dimen2, dimen1), dataNew);
				}else if(dimen1.equals(emdData2.dimen1)==true)
				{
					//A1+A1A2=A1A2
					int[] dataNew=this.mergeDataWithCondition(emdData2.data, emdData2.dimen1.getSize(), emdData2.dimen2.getSize(), 
							data, dimen1.getSize(), true);
					return new EasyMultiDimensionData(this.constructMergedDimension(emdData2.dimen1, dimen1), new Dimension(dimen2), dataNew);
				}
			}else if(emdData2.dimen1!=null)
			{
				if(dimen1.equals(emdData2.dimen1)==true)
				{
					//A1+A1=A1
					int[] dataNew=new int[data.length];
					for(int i=0;i<dimen1.getSize();i++)
					{
						dataNew[i]=data[i]+emdData2.data[i];
					}
					return new EasyMultiDimensionData(this.constructMergedDimension(dimen1, emdData2.dimen1), null, dataNew);
				}
			}
		}
		return null;
	}
	
	/**
	 * 条件并维, A1A2+A1=A1A2 or A1A2+A2=A1A2
	 * @param data1
	 * @param size1
	 * @param data2
	 * @param size2
	 * @return
	 */
	private int[] mergeDataWithCondition(int[] data1, int row1, int col1, int[] data2, int row2, boolean mergeRow)
	{
		int[] dataNew=new int[data1.length];
		if(mergeRow==true)
		{
			for(int i=0;i<row1;i++)
			{
				for(int j=0;j<col1;j++)
				{
					dataNew[i*col1+j]=data1[i*col1+j]+data2[i];
				}
			}
		}else
		{
			for(int i=0;i<row1;i++)
			{
				for(int j=0;j<col1;j++)
				{
					dataNew[i*col1+j]=data1[i*col1+j]+data2[j];
				}
			}
		}
		return dataNew;
	}
	
	private Dimension constructMergedDimension(Dimension base, Dimension toMerge)
	{
		Dimension dimenNew=new Dimension(base);
		dimenNew.mergeConstraintCount(toMerge);
		return dimenNew;
	}
	
	/**
	 * A1A2+A2A3=A1A2+A2A3 or A1A2+A1A3 or A1A3+A2A3
	 * A1A2+A1A3=A1A2+A2A3 or A1A2+A1A3 or A1A3+A2A3
	 * A1A3+A2A3=A1A2+A2A3 or A1A2+A1A3 or A1A3+A2A3
	 * @param emdData1
	 * @param emdData2
	 * @return
	 */
	public static EasyMultiDimensionData[] mergeAndDispart(EasyMultiDimensionData emdData1, EasyMultiDimensionData emdData2, String mutualDimension)
	{
		if(emdData1.dimen2.equals(emdData2.dimen1)==true)
		{
			if(emdData1.dimen1.equals(mutualDimension)==true)
			{
				
			}else if(emdData1.dimen2.equals(mutualDimension)==true)
			{
				
			}else if(emdData2.dimen2.equals(mutualDimension)==true)
			{
				
			}
		}else if(emdData1.dimen1.equals(emdData2.dimen1)==true)
		{
			if(emdData1.dimen2.compareTo(emdData2.dimen2)>0)
			{
				if(emdData1.dimen1.equals(mutualDimension)==true)
				{
					
				}else if(emdData1.dimen2.equals(mutualDimension)==true)
				{
					
				}else if(emdData2.dimen2.equals(mutualDimension)==true)
				{
					
				}
			}else
			{
				if(emdData1.dimen1.equals(mutualDimension)==true)
				{
					
				}else if(emdData1.dimen2.equals(mutualDimension)==true)
				{
					
				}else if(emdData2.dimen2.equals(mutualDimension)==true)
				{
					
				}
			}
		}else if(emdData1.dimen2.equals(emdData2.dimen2)==true)
		{
			if(emdData1.dimen1.compareTo(emdData2.dimen1)>0)
			{
				if(emdData1.dimen1.equals(mutualDimension)==true)
				{
					
				}else if(emdData1.dimen2.equals(mutualDimension)==true)
				{
					
				}else if(emdData2.dimen2.equals(mutualDimension)==true)
				{
					
				}
			}else
			{
				if(emdData1.dimen1.equals(mutualDimension)==true)
				{
					
				}else if(emdData1.dimen2.equals(mutualDimension)==true)
				{
					
				}else if(emdData2.dimen2.equals(mutualDimension)==true)
				{
					
				}
			}
		}
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
