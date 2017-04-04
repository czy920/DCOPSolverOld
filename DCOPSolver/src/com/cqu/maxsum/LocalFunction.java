package com.cqu.maxsum;

/**
 * Created by dyc on 2017/3/6.
 * The wrapper class for constraint matrix
 */
public class LocalFunction {
    private int[][] utilitySquare;
    private int rowVariable;
    private int colVariable;

    /**
     * Constructor
     * @param utilitySquare The constraint matrix
     * @param rowVariable The variable that indexes row
     * @param colVariable The variable that indexes column
     * @param reverse Whether need to reverse utility
     */
    public LocalFunction(int[][] utilitySquare, int rowVariable, int colVariable,boolean reverse) {
        this.rowVariable = rowVariable;
        this.colVariable = colVariable;
        this.utilitySquare = new int[utilitySquare.length][utilitySquare[0].length];
        for (int row = 0; row < utilitySquare.length; row++){
            for (int col = 0; col < utilitySquare[0].length; col++){
                if (reverse)
                    this.utilitySquare[row][col] = -utilitySquare[row][col];
                else
                    this.utilitySquare[row][col] = utilitySquare[row][col];
            }
        }
    }

    /**
     * Project out target variable from summed utility matrix
     * @param target The target variable
     * @param receivedUtility The received global utility
     * @param context The corresponding context (optional)
     * @return Utility function for target
     */
    public int[] project(int target,int[] receivedUtility,int[] context){
        int[] projectedUtility;
        if (target == colVariable){
            projectedUtility = new int[utilitySquare[0].length];
            if (context != null && context.length != projectedUtility.length){
                throw new RuntimeException("context and utility must be the same length!");
            }
            for (int col = 0; col < projectedUtility.length; col++){
                int max = Integer.MIN_VALUE;
                int maxContext = -1;
                for (int row = 0; row < utilitySquare.length; row++){
                    int utilitySum = utilitySquare[row][col] + receivedUtility[row];
                    if (utilitySum > max){
                        max = utilitySum;
                        maxContext = row;
                    }
                }
                projectedUtility[col] = max;
                if (context != null){
                    context[col] = maxContext;
                }
            }
        }
        else if (target == rowVariable){
            projectedUtility = new int[utilitySquare.length];
            if (context != null && context.length != projectedUtility.length){
                throw new RuntimeException("context and utility must be the same length!");
            }
            for (int row = 0; row < projectedUtility.length; row++){
                int max = Integer.MIN_VALUE;
                int maxContext = -1;
                for (int col = 0; col < utilitySquare[0].length; col++){
                    int utilitySum = utilitySquare[row][col] + receivedUtility[col];
                    if (utilitySum > max){
                        max = utilitySum;
                        maxContext = col;
                    }
                }
                projectedUtility[row] = max;
                if (context != null){
                    context[row] = maxContext;
                }
            }
        }
        else {
            throw new RuntimeException("Target is not in the scope!");
        }
        return projectedUtility;
    }

    /**
     * Project out target variable from summed utility matrix
     * @param target The target variable
     * @param receivedUtility The received global utility
     * @return Utility function for target
     */
    public int[] project(int target,int[] receivedUtility){
        return project(target,receivedUtility,null);
    }

    /**
     * Restrict the utility square to certain row according to the received assignment
     * @param target The target variable
     * @param receivedAssignment The received assignment
     * @return Utility function for target
     */
    public int[] restrict(int target,int receivedAssignment){
        int[] utility = null;
        if (target == rowVariable){
            utility = new int[utilitySquare.length];
            for (int i = 0; i < utility.length; i++){
                utility[i] = utilitySquare[i][receivedAssignment];
            }
        }
        else if (target == colVariable){
            utility = new int[utilitySquare[0].length];
            for (int i = 0; i < utility.length; i++){
                utility[i] = utilitySquare[receivedAssignment][i];
            }
        }
        else {
            throw new RuntimeException("Target is not in the scope!");
        }
        return utility;
    }
}
