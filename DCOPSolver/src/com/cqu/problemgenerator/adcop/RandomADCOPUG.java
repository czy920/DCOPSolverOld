package com.cqu.problemgenerator.adcop;

import org.jdom2.Element;
import util.HashList;
import util.graph.Edge;
import util.graph.UndirectedGraph;

/**
 * Created by YanChenDeng on 2016/4/11.
 */
public class RandomADCOPUG<T extends Comparable<? super T>> extends UndirectedGraph<T>{
    @Override
    public Element getConstraints() {

        int nbConstraint = this.getNbEdges();
        Element constraints = new Element("constraints");
        constraints.setAttribute("nbConstraints", "" + nbConstraint);
        HashList edges = this.getEdges();

        for(int c = 0; c < nbConstraint; ++c) {
            Element constraint = new Element("constraint");
            constraint.setAttribute("name", "C" + c);
            constraint.setAttribute("model", "TKC");
            constraint.setAttribute("arity", Integer.toString(2));
            Edge e = (Edge)edges.get(c);
            Comparable var1 = e.getSource().getData();
            Integer agId1 = Integer.valueOf(((Integer)this.clusterOf.get(var1)).intValue() + 1);
            Integer index1 = Integer.valueOf(((HashList)this.clusters.get(this.clusterOf.get(var1))).indexOf(var1) + 1);
            Comparable var2 = e.getTarget().getData();
            Integer agId2 = Integer.valueOf(((Integer)this.clusterOf.get(var2)).intValue() + 1);
            Integer index2 = Integer.valueOf(((HashList)this.clusters.get(this.clusterOf.get(var2))).indexOf(var2) + 1);
            constraint.setAttribute("scope", "X" + agId1 + "." + index1 + " X" + agId2 + "." + index2);
            constraint.setAttribute("scope", "X" + agId2 + "." + index2 + " X" + agId1 + "." + index1);
            constraint.setAttribute("reference", "R" + 2 * c);
            constraint.setAttribute("reference", "R" + (2 * c + 1) );
            constraints.addContent(constraint);
        }

        return constraints;
    }

    @Override
    public Element getSoftRelations(boolean maxDisCSP, int domainSize, int tightness, int minCost, int maxCost) {
        int nbConstraint = this.getNbEdges();
        int nbTuples = domainSize * domainSize * (100 - tightness) / 100;
        Element relations = new Element("relations");
        relations.setAttribute("nbRelations", "" + nbConstraint);

        for(int c = 0; c < 2 * nbConstraint; ++c) {
            Element relation = new Element("relation");
            relation.setAttribute("name", "R" + c);
            relation.setAttribute("arity", "2");
            relation.setAttribute("nbTuples", "" + nbTuples);
            relation.setAttribute("semantics", "soft");
            relation.setAttribute("defaultCost", maxDisCSP?Integer.toString(1):"infinity");
            String tuples = "";
            if(maxDisCSP) {
                String cost = "0:";
                tuples = cost + GenerateRandomTuples(domainSize, nbTuples);
            } else {
                tuples = GenerateRandomSoftTuples(domainSize, nbTuples, minCost, maxCost);
            }

            relation.setText(tuples);
            relations.addContent(relation);
        }

        return relations;
    }
}
