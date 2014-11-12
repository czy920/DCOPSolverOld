package com.cqu.bfsdpop;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cqu.util.CollectionUtil;

public class ListSizeComparator<T> implements Comparator<Integer>{
	
	private Map<Integer, List<T>> objectLists;
	private Integer[] keys;
	
	public ListSizeComparator(Map<Integer, List<T>> objectLists)
    {
        this.objectLists = objectLists;
        
        keys = new Integer[objectLists.size()];
        objectLists.keySet().toArray(keys);
    }
	
	@Override
	public int compare(Integer arg0, Integer arg1) {
		// TODO Auto-generated method stub
		return new Integer(objectLists.get(arg0).size()).compareTo(new Integer(objectLists.get(arg1).size()));
	}
	
	/**
	 * 
	 * @return keys sorted by the list sizes corresponding to the keys
	 */
	public Integer[] sort()
	{
		Arrays.sort(keys, this);
		return keys;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Map<Integer, List<Integer>> objectLists=new HashMap<Integer, List<Integer>>();
		{
			List<Integer> lista=new ArrayList<Integer>();
			objectLists.put(0, lista);
			
			lista.add(1);
			lista.add(3);
			objectLists.put(2, lista);
		}
		{
			List<Integer> listb=new ArrayList<Integer>();
			listb.add(1);
			objectLists.put(3, listb);
		}
		{
			List<Integer> listc=new ArrayList<Integer>();
			listc.add(1);
			listc.add(2);
			listc.add(3);
			listc.add(4);
			objectLists.put(1, listc);
		}
		Integer[] keys=new ListSizeComparator<Integer>(objectLists).sort();
		System.out.println(CollectionUtil.arrayToString(CollectionUtil.toInt(keys)));
	}
}
