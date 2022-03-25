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

/**
 * Defines an instance with a known number of solutions using exactly and alldifferent constraints.
 */
public class Synthetic_UniformSampling {

    static Random rand = new Random();

    public static void main(String[] args) {

	int nbVar = Integer.parseInt(args[0]);
	int nbVal = Integer.parseInt(args[1]);
 	double  fraction = Double.parseDouble(args[2]);

        Solver cp = Factory.makeSolver();
		cp.setTraceSearchFlag(false);
		cp.setMode(Solver.PropaMode.SP);

        IntVar[] x = new IntVar[nbVar];

        for (int i = 0; i < nbVar; i++) {
	    x[i] = makeIntVar(cp, 0, nbVal-1);
	    x[i].setName("x"+"["+i+"]");
        }

	IntVar[] x1 = new IntVar[nbVar/2];
	IntVar[] x2 = new IntVar[nbVar - nbVar/2];

        for (int i = 0; i < nbVar/2; i++)
	    x1[i] = x[i];
	int c1 = x1.length/3;
 	cp.post(exactly(x1,0,c1));

        for (int i = 0; i < nbVar - nbVar/2; i++)
	    x2[i] = x[nbVar/2+i];
 	cp.post(allDifferent(x2));

	//****************************************************************

    	IntVar[] branchingVars = cp.sample(fraction,x);
 	DFSearch search = makeDfs(cp, firstFail(branchingVars));

	/*
  	search.onSolution(() -> {
		System.out.print("     ");
		for (int i = 0; i < nbVar; i++) {
		    System.out.print(x[i].min());
		}
		System.out.println();
	    }
	);
	*/

   	SearchStatistics stats = search.solve();
 	System.out.println(stats);
    }
}
