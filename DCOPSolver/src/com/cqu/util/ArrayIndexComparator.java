package com.cqu.util;

import java.util.Comparator;

public class ArrayIndexComparator<T> implements Comparator<Integer>{

	private final T[] array;
	private int[] indexes;

    public ArrayIndexComparator(T[] array)
    {
        this.array = array;
        
        indexes = new int[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i;
        }
    }

	@SuppressWarnings("unchecked")
	@Override
	public int compare(Integer o1, Integer o2) {
		// TODO Auto-generated method stub
		return ((Comparable<T>) array[o1]).compareTo(array[o2]);
	}
	
	public int[] getSortIndexes()
	{
		return indexes;
	}
}
