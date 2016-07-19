package com.cqu.main;

import com.cqu.core.EventListener;
import com.cqu.core.Result;
import com.cqu.core.ResultCycle;
import com.cqu.core.Solver;
import com.cqu.settings.Settings;

import java.util.*;

public class Test {

	public Test() {
		// TODO Auto-generated constructor stub
		
	}
	public static void main(String[] args) {
		Random random1 = new Random();
		for (int i = 0; i < 9; i++)
			System.out.println(random1.nextInt(100));
		if (true)
			return;
		int domain = 3;
		int neighbour = 2;
		Random random = new Random();
		Map<Integer,int[][]> costs = new HashMap<>();
		for (int i = 1; i <= neighbour; i++){
			int[][] cost = new int[domain][domain];
			for (int j = 0; j < domain; j++){
				for (int k = 0; k < domain; k++){
					cost[j][k] = random.nextInt(100);
				}
			}
			costs.put(i,cost);
		}
		Map<String,Integer> sumUpCost = new HashMap<>();
		List<Map<Integer,Integer>> maxCost = new ArrayList<>();
		for (int i = 0 ; i < neighbour; i++){

		}
		int[] index = new int[neighbour + 1];
		int current = neighbour;
		while (true){
			if (index[0] == domain)
				break;
			if (index[current] == domain){
				index[current--] = 0;
				index[current]++;
				continue;
			}
			current = neighbour;
			StringBuffer stringBuffer = new StringBuffer();
			int cost = 0;

			for (int i = 0; i < neighbour + 1; i++){
				stringBuffer.append(index[i] + " ");
				if (i == 0)
					continue;
				cost += costs.get(i)[index[0]][index[i]];
			}
			sumUpCost.put(stringBuffer.toString(),cost);
			index[current] ++;
		}
		System.out.println(sumUpCost);
	}

}
