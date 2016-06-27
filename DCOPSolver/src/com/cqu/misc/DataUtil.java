package com.cqu.misc;

import util.HashList;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;

/**
 * Created by YanChenDeng on 2016/6/23.
 */
public class DataUtil {
    private Map<String,List<Integer>> originalData;
    private Map<String,List<Integer>> trimedData;
    private List<Integer> averageData;
    private int sampleCount;

    public DataUtil(String dir,int sampleCount) throws Exception{
        this.sampleCount = sampleCount;
        this.originalData = new HashMap<>();
        this.trimedData = new HashMap<>();
        this.averageData = new LinkedList<>();
        loadOriginalData(dir);
        trimData();
        calcuAverage();
    }

    private void loadOriginalData(String dir) throws Exception{
        File dirFile = new File(dir);
        if (!dirFile.isDirectory())
            throw new UnsupportedOperationException("arg dir must refer to a directory!");
        String[] subFileName = dirFile.list();
        for (String file : subFileName){
            Scanner scanner = new Scanner(new FileInputStream(file));
            List<Integer> data = new LinkedList<>();
            while (scanner.hasNextLine()){
                data.add(Integer.parseInt(scanner.nextLine()));
            }
            originalData.put(file,data);
        }
    }

    private void trimData(){
        for (String file : originalData.keySet()){
            List<Integer> tData = new LinkedList<>();
            List<Integer> oData = originalData.get(file);
            if (sampleCount > oData.size())
                throw new UnsupportedOperationException("sample count must less than total count!");
            int stride = oData.size() / sampleCount;
            for (int i = 0; i < (oData.size() - oData.size() % sampleCount); i+= stride){
                tData.add(oData.get(i));
            }
            trimedData.put(file,tData);
        }
    }

    private void calcuAverage(){
        int[] totalData = new int[sampleCount];
        for (String file : trimedData.keySet()){
            List<Integer> tData = trimedData.get(file);
            for (int i = 0; i < sampleCount; i++){
                totalData[i] += tData.get(i);
            }
        }
        averageData = new LinkedList<>();
        for (int i = 0; i < sampleCount ; i++){
            averageData.add(totalData[i] / trimedData.size());
        }
    }

    public List<Integer> getAverageData() {
        return averageData;
    }

    public Map<String, List<Integer>> getTrimedData() {
        return trimedData;
    }

    public Map<String, List<Integer>> getOriginalData() {
        return originalData;
    }
}
