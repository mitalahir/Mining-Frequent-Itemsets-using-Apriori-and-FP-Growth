/* ---------------------------------------------------------------------- */
/*                                                                        */
/*                         FP GROWTH APPLICATION                          */
/*                                                                        */
/*                             Frans Coenen                               */
/*                                                                        */
/*                                                                        */
/*                                                                        */ 
/* ---------------------------------------------------------------------- */

import java.io.*;

public class FPgrowthApp {
    
    // ------------------- FIELDS ------------------------
    
    // None
    
    // ---------------- CONSTRUCTORS ---------------------
    
    // None
    
    // ------------------ METHODS ------------------------
    
    public static void main(String[] args) throws IOException {
        
	// Create instance of class FPTree
	
	FPtree newFPtree = new FPtree(args);
	
	// Read data to be mined from file
	
	newFPtree.inputDataSet();
	
	// Reorder and prune input data according to frequency of single 
	//attributes	
	newFPtree.idInputDataOrdering();
	newFPtree.recastInputDataAndPruneUnsupportedAtts(); 
	newFPtree.setNumOneItemSets();
//newFPtree.outputDataArray();

        // Build initial FP-tree
	
	double time1 = (double) System.currentTimeMillis(); 
	newFPtree.createFPtree();
	newFPtree.outputDuration(time1,(double) System.currentTimeMillis());
newFPtree.outputFPtreeStorage();			
newFPtree.outputFPtree();
//newFPtree.outputItemPrefixSubtree();

	// Mine FP-tree
	
	time1 = (double) System.currentTimeMillis(); 
	newFPtree.startMining();
	newFPtree.outputDuration(time1,(double) 
					System.currentTimeMillis());
newFPtree.outputStorage(); 
newFPtree.outputNumFreqSets(); 
//newFPtree.outputTtree(); // Frequent sets arec stored in this structure
newFPtree.outputRules();	
	}
    }
    
