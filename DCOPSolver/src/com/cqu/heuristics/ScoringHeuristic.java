package com.cqu.heuristics;

import java.io.Serializable;

import com.cqu.varOrdering.dfs.DFSview;
/** Interface for a heuristic that associates a score to every variable
 * 
 * This heuristic can be used to construct variable orderings. 
 * @param <S> the type used for the scores
 * @note All such heuristics should have a constructor that takes in a DCOPProblemInterface describing the agent's problem 
 * and an Element describing the parameters of the heuristic. 
 */
public interface ScoringHeuristic <S extends Comparable<S> & Serializable > {
	/** @return the scores for the variables */
	public int getScores ();
	public int getScores (Integer nodeID, DFSview dfsview);
}
