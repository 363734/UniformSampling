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
import minicpbp.search.LDSearch;
import minicpbp.search.SearchStatistics;

import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.Arrays;
import java.util.Random;

import static minicpbp.cp.BranchingScheme.*;
import static minicpbp.cp.Factory.*;

/**
 * The Graph Vertex Coloring problem.
 */
public class GraphColoring_UniformSampling {

    static SearchStatistics stats = new SearchStatistics();
    static boolean firstSoln = true;

    public static void main(String[] args) {
	/*
	int id1 = Integer.parseInt(args[0]);
	int id2 = Integer.parseInt(args[1]);
  	double fraction = Double.parseDouble(args[2]);
	*/

	int id1 = Integer.parseInt(args[0]);
  	double fraction = Double.parseDouble(args[1]);

	try {
	    // Scanner scanner = new Scanner(new FileReader("minicp/examples/data/GraphColoring/"+id1+"_fullins_"+id2+".col"));
	    // Scanner scanner = new Scanner(new FileReader("minicp/examples/data/GraphColoring/"+id1+"_insertions_"+id2+".col"));
	    Scanner scanner = new Scanner(new FileReader("data/GraphColoring/myciel"+id1+".col"));
	    int n = scanner.nextInt(); // nb of vertices
	    int a = scanner.nextInt(); // nb of edges
	    int c = scanner.nextInt(); // nb of colors
	    Solver cp = Factory.makeSolver();
		cp.setTraceSearchFlag(false);
		cp.setMode(Solver.PropaMode.SP);
	    IntVar[] clr = Factory.makeIntVarArray(cp, n, c);
	    // wlog colour the first edge
// 	    int u = scanner.nextInt();
// 	    int v = scanner.nextInt();
	    int u = scanner.nextInt()-1;
	    int v = scanner.nextInt()-1;
	    clr[u].assign(0);
	    clr[v].assign(1);
	    // constrain the other edges
	    for(int i = 1; i<a; i++){
// 		u = scanner.nextInt();
// 		v = scanner.nextInt();
		u = scanner.nextInt()-1;
		v = scanner.nextInt()-1;
		cp.post(Factory.notEqual(clr[u],clr[v]));
	    }
	    scanner.close();

 	    IntVar[] branchingVars = cp.sample(fraction,clr);
 	    DFSearch search = Factory.makeDfs(cp, firstFail(branchingVars));

	    /*
	    search.onSolution(() -> {
		    for(int i = 0; i<n; i++)
			System.out.print(clr[i].min());
		    System.out.println();
		}
		);
	    */

	    stats = search.solve();
 	    System.out.format("Statistics: %s\n", stats);
	    
	}
	catch (IOException e) {
	    System.err.println("Error : " + e.getMessage()) ;
	    System.exit(2) ;
	}
    }	
}
