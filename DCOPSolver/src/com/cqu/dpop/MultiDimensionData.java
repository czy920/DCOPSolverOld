package com.cqu.dpop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.cqu.util.CollectionUtil;

public class MultiDimensionData {
	
	private List<Dimension> dimensions;
	private int[] data;

	public MultiDimensionData(List<Dimension> dimentions, int[] data) {
		super();
		this.dimensions = dimentions;
		this.data = data;
	}

	public List<Dimension> getDimensions() {
		return dimensions;
	}

	public int[] getData() {
		return data;
	}
	
	public int indexOf(String dimensionName)
	{
		for(int i=0;i<dimensions.size();i++)
		{
			if(dimensions.get(i).getName().equals(dimensionName))
			{
				return i;
			}
		}
		return -1;
	}
	
	public static int indexOf(List<Dimension> dimensions, String dimensionName)
	{
		for(int i=0;i<dimensions.size();i++)
		{
			if(dimensions.get(i).getName().equals(dimensionName))
			{
				return i;
			}
		}
		return -1;
	}
	
	/**
	 * decide if the dimension is reductable;<br/>
	 * if the constraintCount is equal to constraintCountTotal, yes; otherwise, no;<br/>
	 * constraintCount<=constraintCountTotal
	 * @param dimensionName
	 * @return
	 */
	public boolean isReductable(String dimensionName)
	{
		return this.dimensions.get(this.indexOf(dimensionName)).isReductable();
	}
	
	public ReductDimensionResult reductDimension(String dimensionName, int reductDimentionMethod)
	{
		int dimensionToReductIndex=this.indexOf(dimensionName);
		Dimension dimensionToReduct=this.dimensions.get(dimensionToReductIndex);
		
		List<Dimension> dimensionsNew=new ArrayList<Dimension>();
		for(Dimension dimen : this.dimensions)
		{
			dimensionsNew.add(new Dimension(dimen));
		}
		dimensionsNew.remove(dimensionToReductIndex);
		
		//compute periods for each dimension in new multiple dimension data
		int[] periodsNew=new int[dimensions.size()];
		for(int i=0;i<dimensions.size();i++)
		{
			int temp=1;
			for(int j=i+1;j<dimensions.size();j++)
			{
				temp*=dimensions.get(j).getSize();
			}
			periodsNew[i]=temp;
		}
		for(int i=0;i<dimensionToReductIndex;i++)
		{
			periodsNew[i]/=dimensionToReduct.getSize();
		}
		periodsNew[dimensionToReductIndex]=0;
		
		int[] dataNew=new int[data.length/dimensionToReduct.getSize()];
		int[] resultIndexes=new int[dataNew.length];
		//降低指定维度
		int[] agentValueIndexes=new int[dimensions.size()];
		int dataIndex=0;
		int dataIndexNew=0;
		int curDimension=agentValueIndexes.length-1;
		if(reductDimentionMethod==ReductDimensionResult.REDUCT_DIMENSION_WITH_MIN)
		{
			Arrays.fill(dataNew, Integer.MAX_VALUE);
			while(dataIndex<data.length)
			{
				if(data[dataIndex]<dataNew[dataIndexNew])
				{
					dataNew[dataIndexNew]=data[dataIndex];
					resultIndexes[dataIndexNew]=agentValueIndexes[dimensionToReductIndex];
				}
				agentValueIndexes[curDimension]+=1;
				dataIndexNew+=periodsNew[curDimension];
				while(agentValueIndexes[curDimension]>=dimensions.get(curDimension).getSize())
				{
					agentValueIndexes[curDimension]=0;
					dataIndexNew-=dimensions.get(curDimension).getSize()*periodsNew[curDimension];
					
					curDimension-=1;
					if(curDimension==-1)
					{
						dataIndexNew=dimensions.get(0).getSize()*periodsNew[0];
						break;
					}
					agentValueIndexes[curDimension]+=1;
					if(curDimension!=dimensionToReductIndex)
					{
						dataIndexNew+=periodsNew[curDimension];
					}
				}
				curDimension=agentValueIndexes.length-1;
				dataIndex++;
			}
		}else
		{
			Arrays.fill(dataNew, Integer.MIN_VALUE);
			while(dataIndex<data.length)
			{
				if(data[dataIndex]>dataNew[dataIndexNew])
				{
					dataNew[dataIndexNew]=data[dataIndex];
					resultIndexes[dataIndexNew]=agentValueIndexes[dimensionToReductIndex];
				}
				agentValueIndexes[curDimension]+=1;
				dataIndexNew+=periodsNew[curDimension];
				while(agentValueIndexes[curDimension]>=dimensions.get(curDimension).getSize())
				{
					agentValueIndexes[curDimension]=0;
					dataIndexNew-=dimensions.get(curDimension).getSize()*periodsNew[curDimension];
					
					curDimension-=1;
					if(curDimension==-1)
					{
						dataIndexNew=dimensions.get(0).getSize()*periodsNew[0];
						break;
					}
					agentValueIndexes[curDimension]+=1;
					if(curDimension!=dimensionToReductIndex)
					{
						dataIndexNew+=periodsNew[curDimension];
					}
				}
				curDimension=agentValueIndexes.length-1;
				dataIndex++;
			}
		}
		
		MultiDimensionData mdData=new MultiDimensionData(dimensionsNew, dataNew);
		return new ReductDimensionResult(mdData, resultIndexes);
	}
	
	public MultiDimensionData mergeDimension(MultiDimensionData mdDataB)
	{
		MultiDimensionData mdDataA=this;
		
		List<Dimension> dimensionsNew=new ArrayList<Dimension>();
		for(Dimension dimen : mdDataA.dimensions)
		{
			dimensionsNew.add(new Dimension(dimen));
		}
		for(Dimension dimen : mdDataB.dimensions)
		{
			int index=MultiDimensionData.indexOf(dimensionsNew, dimen.getName());
			if(index==-1)
			{
				dimensionsNew.add(dimen);
			}else
			{
				dimensionsNew.get(index).mergeConstraintCount(dimen);
			}
		}
		Collections.sort(dimensionsNew);
		
		//compute periods for each dimension in multiple dimension data
		int[] periodsA=new int[dimensionsNew.size()];
		for(int i=0;i<dimensionsNew.size();i++)
		{
			int index=mdDataA.indexOf(dimensionsNew.get(i).getName());
			if(index==-1)
			{
				periodsA[i]=0;
			}else
			{
				int temp=1;
				for(int j=index+1;j<mdDataA.dimensions.size();j++)
				{
					temp*=mdDataA.dimensions.get(j).getSize();
				}
				periodsA[i]=temp;
			}
		}
		int[] periodsB=new int[dimensionsNew.size()];
		for(int i=0;i<dimensionsNew.size();i++)
		{
			int index=mdDataB.indexOf(dimensionsNew.get(i).getName());
			if(index==-1)
			{
				periodsB[i]=0;
			}else
			{
				int temp=1;
				for(int j=index+1;j<mdDataB.dimensions.size();j++)
				{
					temp*=mdDataB.dimensions.get(j).getSize();
				}
				periodsB[i]=temp;
			}
		}
		
		int dataNewLength=1;
		for(Dimension dimen : dimensionsNew)
		{
			dataNewLength*=dimen.getSize();
		}
		int[] dataNew=new int[dataNewLength];
		//合并指定维度
		int[] agentValueIndexes=new int[dimensionsNew.size()];
		int dataIndexNew=0;
		int dataIndexA=0;
		int dataIndexB=0;
		int curDimension=agentValueIndexes.length-1;
		Arrays.fill(dataNew, 0);
		while(dataIndexNew<dataNew.length)
		{
			dataNew[dataIndexNew]+=mdDataA.data[dataIndexA];
			dataNew[dataIndexNew]+=mdDataB.data[dataIndexB];
			
			agentValueIndexes[curDimension]+=1;
			dataIndexA+=periodsA[curDimension];
			dataIndexB+=periodsB[curDimension];
			while(agentValueIndexes[curDimension]>=dimensionsNew.get(curDimension).getSize())
			{
				dataIndexA-=periodsA[curDimension]*agentValueIndexes[curDimension];
				dataIndexB-=periodsB[curDimension]*agentValueIndexes[curDimension];
				agentValueIndexes[curDimension]=0;
				
				curDimension-=1;
				if(curDimension==-1)
				{
					break;
				}
				agentValueIndexes[curDimension]+=1;
				dataIndexA+=periodsA[curDimension];
				dataIndexB+=periodsB[curDimension];
			}
			curDimension=agentValueIndexes.length-1;
			dataIndexNew++;
		}
		
		return new MultiDimensionData(dimensionsNew, dataNew);
	}
	
	public MultiDimensionData testMergeDimension(MultiDimensionData mdDataB)
	{
		MultiDimensionData mdDataA = this;
		
		List<Dimension> dimensionsNew=new ArrayList<Dimension>();
		for(Dimension dimen : mdDataA.dimensions)
		{
			dimensionsNew.add(new Dimension(dimen));
		}
		for(Dimension dimen : mdDataB.dimensions)
		{
			int index=MultiDimensionData.indexOf(dimensionsNew, dimen.getName());
			if(index==-1)
			{
				dimensionsNew.add(dimen);
			}else
			{
				dimensionsNew.get(index).mergeConstraintCount(dimen);
			}
		}
		Collections.sort(dimensionsNew);
		return new MultiDimensionData(dimensionsNew, null);
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
		MultiDimensionData mdDataA, mdDataB;
		{
			List<Dimension> dimensions=new ArrayList<Dimension>();
			dimensions.add(new Dimension("A1", 2, 0));
			dimensions.add(new Dimension("A2", 3, 1));
			mdDataA=new MultiDimensionData(dimensions, new int[]{1, 0, 5, 3, 4, 2});
		}
		{
			List<Dimension> dimensions=new ArrayList<Dimension>();
			dimensions.add(new Dimension("A2", 3, 1));
			dimensions.add(new Dimension("A3", 2, 2));
			mdDataB=new MultiDimensionData(dimensions, new int[]{3, 4, 2, 5, 1, 2});
		}

		System.out.println(mdDataA.mergeDimension(mdDataB).toString());
	}
}
