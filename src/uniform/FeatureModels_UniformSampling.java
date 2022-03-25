/*
 * mini-cp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License  v3
 * as published by the Free Software Foundation.
 *
 * mini-cp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY.
 * See the GNU Lesser General Public License  for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with mini-cp. If not, see http://www.gnu.org/licenses/lgpl-3.0.en.html
 *
 * Copyright (c)  2018. by Laurent Michel, Pierre Schaus, Pascal Van Hentenryck
 *
 * mini-cpbp, replacing classic propagation by belief propagation 
 * Copyright (c)  2019. by Gilles Pesant
 */

package uniform;


import minicpbp.cp.Factory;
import minicpbp.engine.core.IntVar;
import minicpbp.engine.core.Solver;
import minicpbp.engine.core.Constraint;
import minicpbp.engine.constraints.*;
import minicpbp.search.DFSearch;
import minicpbp.search.SearchStatistics;

import java.util.Arrays;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Random;

import static minicpbp.cp.BranchingScheme.*;
import static minicpbp.cp.Factory.*;

/*
Feature Models problem used by Vavrille et al., Proc. CP 2021
https://github.com/MathieuVavrille/tableSampling/blob/master/csplibmodels/feature.mzn
 */
public class FeatureModels_UniformSampling {
    
    public static void main(String[] args) {

	int N=15;
	int K=2;
	int R=4;
	int S=2;
	
	int[][] r =  { { 150, 120, 20, 1000} , { 75, 10, 8, 200} , { 400, 100, 20, 200} , { 450, 100, 40, 0} , { 100, 500, 40, 0} , { 200, 400, 25, 25} , { 50, 250, 20, 500} , { 60, 120, 19, 200} , { 280, 150, 40, 1500} , { 200, 300, 40, 500} , { 250, 375, 50, 150} , { 100, 300, 25, 50} , { 100, 250, 20, 50} , { 0, 100, 15, 0} , { 200, 150, 10, 0} } ;
	int[][] value =  { { 6, 7, 9, 5, 3, 9, 5, 7, 6, 2, 1, 3, 7, 8, 1} , { 2, 5, 3, 7, 2, 3, 3, 1, 5, 1, 5, 7, 9, 3, 5} };
	int[][][] urgency = 
	 {
	  {
	   {5, 4, 0},
	   {5, 0, 4},
	   {9, 0, 0},
	   {2, 7, 0},
	   {7, 2, 0},
	   {7, 2, 0},
	   {9, 0, 0},
	   {8, 1, 0},
	   {9, 0, 0},
	   {5, 4, 0},
	   {8, 1, 0},
	   {9, 0, 0},
	   {9, 0, 0},
	   {9, 0, 0},
	   {0, 0, 9} },
	  {
	   {0, 3, 6},
	   {9, 0, 0},
	   {2, 7, 0},
	   {7, 2, 0},
	   {9, 0, 0},
	   {5, 4, 0},
	   {2, 7, 0},
	   {0, 0, 9},
	   {0, 8, 1},
	   {0, 0, 9},
	   {0, 7, 2},
	   {0, 6, 3},
	   {9, 0, 0},
	   {6, 3, 0},
	   {3, 6, 0} }
	 };
	int[][] C = { {7, 8} , {9, 12} , {13, 14} };
	int[][] P = { {2, 1} , {5, 6} , {3, 11} , {8, 9} , {13, 15} };
	int[] lambda = {4,6};
	int[] ksi = {7,3};
	int[][] Cap = { {1300, 1450, 158, 2200} , {1046, 1300, 65, 1750} };
	int[][] WAS = new int[N][K];

	for (int i = 0; i < N; i++) {
	    for (int k = 0; k < K; k++) {
		int s = 0;
		for (int j = 0; j < S; j++) {
		    s += lambda[j]*value[j][i]*urgency[j][i][k];
		}
		WAS[i][k] = ksi[k]*s;
	    }
	}

  	double fraction = Double.parseDouble(args[0]);
  	int lowerBound = Integer.parseInt(args[1]); // they use 17738

        Solver cp = Factory.makeSolver();
	
        IntVar[] x = new IntVar[N];
	for (int i = 0; i < N; i++) {
 	    x[i] = makeIntVar(cp, 0, K);
	}
	IntVar F = makeIntVar(cp, lowerBound, 20222); 

	// dependency constraints
	for (int i = 0; i < 3; i++) {
	    cp.post(lessOrEqual(x[C[i][0]-1],x[C[i][1]-1]));
	    cp.post(lessOrEqual(x[C[i][1]-1],x[C[i][0]-1])); // i.e. they are equal
	}
	for (int i = 0; i < 5; i++) {
	    cp.post(lessOrEqual(x[P[i][0]-1],x[P[i][1]-1]));
	}

	// ressource constraints
	for (int k = 0; k < K; k++) {
	    for (int j = 0; j < R; j++) {
		IntVar[] demand = new IntVar[N];
		IntVar cap = makeIntVar(cp,0,Cap[k][j]);
		for (int i = 0; i < N; i++) {
		    demand[i] = mul(isEqual(x[i],k),r[i][j]);
		}
		cp.post(sum(demand,cap));
	    }
	}

	// objective function
	IntVar[] term = new IntVar[K*N];
	for (int k = 0; k < K; k++) {
	    for (int i = 0; i < N; i++) {
		term[k*N+i] = mul(isEqual(x[i],k),WAS[i][k]);
	    }
	}
	cp.post(sum(term,F));


      	IntVar[] branchingVars = cp.sample(fraction,x);
       	DFSearch search = makeDfs(cp, firstFail(branchingVars));
//       	DFSearch search = makeDfs(cp, firstFail(x));
	    
  	search.onSolution(() -> {
		System.out.print("     ");
		for (int i = 0; i < N; i++) {
		    if (!x[i].isBound()) {
			System.out.println("UNBOUND VAR!!!");
			System.exit(1);
		    }
		    System.out.print((x[i].min()+1)+" ");
		}
		System.out.println("   "+F.min());
	    }
	);

       	SearchStatistics stats = search.solve();
//        	System.out.println(stats);

    }
}
