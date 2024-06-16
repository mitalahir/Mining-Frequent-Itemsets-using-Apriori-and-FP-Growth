package fpgrowth;

public class DataTree extends AssociationRuleMining
{  
    /** Ttree保存条件FP-tree */
    protected TreeNode[] startTtreeRef;

    /** 
     * 构造函数 
     * */   
    public DataTree(String[] args) 
    {
    	super(args);
	}
  
    /** 将新的项目集添加到T-tree中 */    
    public void addToTtree(short[] itemSet, int support) 
    {
        // 标记数组的最后一项
    	int endIndex = itemSet.length-1;	

        startTtreeRef = addToTtree(startTtreeRef,numOneItemSets+1,
			endIndex,itemSet,support);
	}
   
    /** 递归函数，向T-tree中加入新的结点 */    
    protected TreeNode[] addToTtree(TreeNode[] linkRef, int size, int endIndex,
    				short[] itemSet, int support) 
    {
		// T-tree为空或空的分支	
		if (linkRef == null) 
		{
		    linkRef = new TreeNode[size];
		    for(int index=1;index<linkRef.length;index++) 
				linkRef[index] = null;
	    }
	
		// 获得当前属性标志，根据属性标志找到T-tree的相应子树
		int currentAttribute = itemSet[endIndex]; 
		if (linkRef[currentAttribute] == null)
			linkRef[currentAttribute] = new TreeNode();
		// 与当前结点相对应
		if (endIndex == 0) 
		{
		    linkRef[currentAttribute].support =
		    			linkRef[currentAttribute].support + support;
		    return linkRef;
		}	   
		// 下一层结点	
		linkRef[currentAttribute].childRef = 
			addToTtree(linkRef[currentAttribute].childRef,
				currentAttribute,endIndex-1,itemSet,support);

		return linkRef;
	}

    /** 循环处理Ttree中所有子树 */
    protected void generateARs() 
    {
		for (int index=1;index <= numOneItemSets;index++) 
		{
		    if (startTtreeRef[index] !=null) 
		    {
		    	// 只有关联规则只应包含频繁项
		        if (startTtreeRef[index].support >= minSupport) 
		        {
		        	// Ttree第一层，不会产生关联规则，只提供一个频繁项
		            short[] itemSetSoFar = new short[1];
		            itemSetSoFar[0] = (short) index;
		            generateARs(itemSetSoFar,index, startTtreeRef[index].childRef);
			    }
			}
	    }
	}
    /** 递归函数，为每一个子树生成所有的关联规则 */
    protected void generateARs(short[] itemSetSofar, int size,
    							TreeNode[] linkRef) 
    {
		// 达到叶结点，递归出口
		if (linkRef == null) 
			return;
	
		// 中间的分支结点
		for (int index=1; index < size; index++) 
		{
		    if (linkRef[index] != null) 
		    {
		        if (linkRef[index].support >= minSupport) 
		        {
				    short[] tempItemSet = realloc2(itemSetSofar,(short) index);
				    // itemSetSofar最少包含两项，可以产生关联规则
				    generateARsFromItemset(tempItemSet,linkRef[index].support);
			        // 递归调用，深度优先，模式增长
				    generateARs(tempItemSet,index,linkRef[index].childRef);
		        }
		    }
	    }
	}

    /** 由所给的项目集得到关联规则的集合 */
    private void generateARsFromItemset(short[] itemSet, double support) 
    {
    	// 得到所有的连接情况
    	short[][] combinations = combinations(itemSet);

		// 处理得到的结果
		for(int index=0;index<combinations.length;index++) 
		{
			// 找到集合的补
		    short[] complement = complement(combinations[index],itemSet);
		    // 如果补集合不空，则可能产生符合要求的关联规则
		    if (complement != null) 
		    {
		        double confidenceForAR = getConfidence(combinations[index], support);
		        // 符合置信度要求的关联规则
				if (confidenceForAR >= confidence) 
				{
					insertRuleintoRulelist(combinations[index], complement,confidenceForAR);
				}
		    }
		}
	}
   
    /**根据关联规则的前提集与总的支持度，获得关联规则置信度 */    
    protected double getConfidence(short[] antecedent, double support) 
    {
        // 获得前提集的支持度
        double supportForAntecedent = (double) getSupportForItemSetInTtree(antecedent);				
		// 相除得到置信度
		double confidenceForAR = ((double) support/supportForAntecedent)*10000;
		int tempConf = (int) confidenceForAR;
		confidenceForAR = (double) tempConf/100;
		return confidenceForAR; 
	}

    /** 获得给定项目集的支持度，用来计算关联规则的置信度 */
    protected int getSupportForItemSetInTtree(short[] itemSet) 
    {
    	int endInd = itemSet.length-1;

    	// Ttree 中的项目倒序存放，所以从项目集的最后一项开始比较
    	if (startTtreeRef[itemSet[endInd]] != null) 
    	{
		    // 项目集到达第一项，比较结束
		    if (endInd == 0) 
		    	return(startTtreeRef[itemSet[0]].support);
		    // 比较下一项
		    else 
		    {
		    	TreeNode[] tempRef = startTtreeRef[itemSet[endInd]].childRef;
		        if (tempRef != null) 
		        	return(getSupForIsetInTtree2(itemSet, endInd-1,tempRef));
		    	// Ttree中不完全包含的项目集
		        else 
		        	return 0;
		    }
	    }
    	// Ttree中不包含的项目集
    	else 
    		return 0;
	}

    /** 递归函数，依次比较项目集中的每一项 */
    private int getSupForIsetInTtree2(short[] itemSet, int index,
    							TreeNode[] linkRef) 
    {
    	if (linkRef[itemSet[index]] != null) 
    	{
    		// 到达第一项，成功，递归出口
    		if (index == 0) 
    			return(linkRef[itemSet[0]].support);
    		// 下一项
    		else if (linkRef[itemSet[index]].childRef != null)
	    	 return(getSupForIsetInTtree2(itemSet,index-1, linkRef[itemSet[index]].childRef));
    		else 
    			return 0;
	    }	
    	// Ttree中不包含的项目集
    	else 
    		return 0;    
    }


    /** 输出频繁模式 */
    public void outputFrequentSets() 
    {
		System.out.println("频繁模式如下（[N] {I} = S，N为序列号；I为项目集；S为支持度）：");
		int number = 1;
		for (short index=1; index <= numOneItemSets; index++) 
		{
		    if (startTtreeRef[index] !=null) 
		    {
		        if (startTtreeRef[index].support >= minSupport) 
		        {
		            String itemSetSofar = new Short(reconvertItem(index)).toString();
		            System.out.println("[" + number + "] {" + itemSetSofar + 
			    		       "} = " + startTtreeRef[index].support);
		            number = outputFrequentSets(number+1,itemSetSofar,
			    			 index/*下标越小，频率越高，以其为叶结点的树就会越少*/,
			    			 startTtreeRef[index].childRef);
			    }
		    }
	    }
	}

    /**递归函数，输出子数中的所有频繁模式 */
    private int outputFrequentSets(int number, String itemSetSofar, int size,
    							TreeNode[] linkRef) 
    {
		if (linkRef == null) 
			return number;
	
		// 非空
		itemSetSofar = itemSetSofar + " ";
		for (short index=1; index < size; index++) 
		{
		    if (linkRef[index] != null) 
		    {
		        if (linkRef[index].support >= minSupport) 
		        {
		            String newItemSet = itemSetSofar + (reconvertItem(index));
		            System.out.println("[" + number + "] {" + newItemSet +
			                             "} = " + linkRef[index].support);
		            number = outputFrequentSets(number + 1,newItemSet,index,
			    			             linkRef[index].childRef);
		        }
		    }
		}
		return number;
	}

    /**输出符合置信度的关联规则 */
    public void outputARs()
    {
    	if(startRulelist==null)
    	{
        	System.out.print("未有满足置信度的关联规则，如果可以请调整置信度\n");
        	return;
    	}
    	System.out.println("关联规则如下([N] {P}——>{R} C，N为序列号；P为前提；R为结果；C为置信度)：");
    	RuleNode tempNode = null;
    	int num = 0;
    	for(tempNode=startRulelist;tempNode!=null;tempNode=tempNode.next)
    	{
    		num ++;
    		System.out.print("["+num+"] {");
    		for(int index=0;index<tempNode.antecedent.length;index ++)
    		{
    			System.out.print(""+reconvertItem(tempNode.antecedent[index]));
    			if(index+1<tempNode.antecedent.length)
    				System.out.print(" ");
    		}
    		System.out.print("} ——> {");
    		for(int index=0;index<tempNode.consequent.length;index ++)
    		{
    			System.out.print(""+reconvertItem(tempNode.consequent[index]));
    			if(index+1<tempNode.consequent.length)
    				System.out.print(" ");
    		}
    		System.out.print("} "+tempNode.confidenceForRule+"%\n");
    	}    	
    }
    
    public void outputARs2()
    {
    	if(startRulelist==null)
    	{
        	System.out.print("未有满足置信度的关联规则，如果可以请调整支持度和置信度\n");
        	return;
    	}
    	System.out.println("关联规则如下([N] {P}——>{R} C，N为序列号；P为前提；R为结果；C为置信度)：");
    	RuleNode tempNode = null;
    	int num = 0;
    	for(tempNode=startRulelist;tempNode!=null;tempNode=tempNode.next)
    	{
    		num ++;
    		System.out.print("["+num+"] {");
    		for(int index=0;index<tempNode.antecedent.length;index ++)
    		{
    			System.out.print(""+attrsOfDataSet[reconvertItem(tempNode.antecedent[index])].titel/*将属性标志换成用户可识别信息*/);
    			if(index+1<tempNode.antecedent.length)
    				System.out.print(" ");
    		}
    		System.out.print("} ——> {");
    		for(int index=0;index<tempNode.consequent.length;index ++)
    		{
    			System.out.print(""+attrsOfDataSet[reconvertItem(tempNode.consequent[index])].titel);
    			if(index+1<tempNode.consequent.length)
    				System.out.print(" ");
    		}
    		System.out.print("} "+tempNode.confidenceForRule+"%\n");
    	}
    }

    /** 将Ttree打印到屏幕上
    public void outputTtree() 
    {
    	System.out.println("数据结构Ttree");
		int number = 1;
		for (short index=1; index < startTtreeRef.length; index++) 
		{
			// 非空树
		    if (startTtreeRef[index] !=null) 
		    {
		        String itemSetSofar = new Short(reconvertItem(index)).toString();
		        System.out.print("[" + number + "] {" + itemSetSofar);
		        System.out.println("} = " + startTtreeRef[index].support);
		        outputTtree(new Integer(number).toString(),itemSetSofar,
				                        startTtreeRef[index].childRef);
		        number++;
			}
	    }   
	}*/		
    /** 递归函数，深度优先输出完整子数   
    private void outputTtree(String number, String itemSetSofar,
    				TreeNode[] linkRef) 
    {
		int num=1;
		number = number + ".";
		itemSetSofar = itemSetSofar + " ";
		
		// 为空
		if (linkRef == null) 
			return;
		
		// 循环处理所有子树
		for (short index=1;index<linkRef.length;index++) 
		{
		    if (linkRef[index] != null) 
		    {
		        String newItemSet = itemSetSofar + (reconvertItem(index));
		        System.out.print("[" + number + num + "] {" + newItemSet);
		        System.out.println("} = " + linkRef[index].support);
		        // 递归调用，深度优先
		        outputTtree(number + num,newItemSet,linkRef[index].childRef); 
		        num++;
			}
	    }    
	}*/    
	
}
