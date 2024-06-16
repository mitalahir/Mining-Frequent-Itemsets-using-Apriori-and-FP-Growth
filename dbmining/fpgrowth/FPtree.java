package fpgrowth;

// FP树                             
public class FPtree extends DataTree 
{
    
    /** FP-tree 结点*/    
    protected class FPtreeNode 
    {
        /** FP-tree 结点结构，包含属性标志、频率、父亲结点的引用等*/
        private FPgrowthItemPrefixSubtreeNode node = null;
		/** 孩子结点的引用*/ 
        private FPtreeNode[] childRefs = null;
        
		/** 默认构造函数*/	
		protected FPtreeNode() 
		{}  
		    
		/** 单参数构造函数*/		
		protected FPtreeNode(FPgrowthItemPrefixSubtreeNode newNode) 
		{
		    node = newNode;
	    }
	}
    
    /** 数据结构，FPtree结点所包含的子结点 */   
    private class FPgrowthItemPrefixSubtreeNode 
    {
        /** 属性标志 */
        private short itemName;
		/** 支持度计数 */
		private int itemCount;
		/** 父亲结点的引用 */
		private FPgrowthItemPrefixSubtreeNode parentRef = null;
		/**FP-tree中，具有相同属性标志的引用 */
		private FPgrowthItemPrefixSubtreeNode nodeLink = null;
		
		/** 默认构造函数 */		
		private FPgrowthItemPrefixSubtreeNode() 
		{}
		
		/** 三参数构造函数 */		
		private FPgrowthItemPrefixSubtreeNode(short name, int support, 
				FPgrowthItemPrefixSubtreeNode backRef) 
		{
		    itemName = name;
		    itemCount = support;
		    parentRef = backRef;
	    }
	}
    
    /** FP-tree项头表中表项的数据结构 */    
    protected class FPgrowthHeaderTable 
    {
        /** 属性标志（从1开始）*/
    	protected short itemName;
    	/** 形成统一属性的一条链 */
        protected FPgrowthItemPrefixSubtreeNode nodeLink = null;
        
        //唯一的构造函数
		protected FPgrowthHeaderTable (short columnNum) 
		{
		    itemName = columnNum;
	    }  
    }
	
    /** 数据结构，存放祖先结点的结构，用来构造条件FP-tree*/    
    private class FPgrowthSupportedSets 
    {
        /** 属性标志数组 */
        private short[] itemSet = null;
        /** 与itemSet相对应的支持度*/
        private int support;
		/** 形成链表 */
		private FPgrowthSupportedSets nodeLink = null;
	        
		/** 三参数构造函数 */		
		private FPgrowthSupportedSets(short[] newitemSet, int newSupport, 
				FPgrowthSupportedSets newNodeLink) 
		{
		    itemSet = newitemSet;
	        support = newSupport;
		    nodeLink = newNodeLink;
	    } 
	} 
    
    /**自定义数据结构，用来储存属性的累计支持度 */    
    private class FPgrowthColumnCounts 
    {
        /** 属性标志 */
        private short columnNum;
        /** 相关支持度*/
        private int support=0;
        
		/** 单参数构造函数 */	
		private FPgrowthColumnCounts(int column) 
		{
		    columnNum = (short) column;
		}  
 
	}   
	
    /** FP-tree根节点*/
    protected FPtreeNode rootNode = null;
    /** FP-tree项头表 */
    protected FPgrowthHeaderTable[] headerTable; 
    /** 暂时存放频繁一项集的祖先结点集*/
    private static FPgrowthSupportedSets startTempSets = null;
    
    /** 暂时存放FP-tree结点中孩子结点引用数组的下标（当为数组重新分配空间时）*/
    private int tempIndex  = 0;    	
        
    /** 
     * 根据命令行参数构造 FPtree 
     * */  
    public FPtree(String[] args) 
    {
    	super(args);
	
		// 初始化根结点，为空
		rootNode = new FPtreeNode();
		
		// 构造项头表	
		headerTable = new FPgrowthHeaderTable[numOneItemSets+1];

	}	
    /** 数据预处理*/ 
    public void preprocessDataSet()
    {
    	System.out.println("预处理："+fileName);
    	preprocessDataSet(fileName);
    }
   
    /** 根据修剪以后的数据集，生成FP-tree */    
    public void createFPtree() 
    {    
    	System.out.println("生成 FP-tree");
		// 创建项头表	
		headerTable = new FPgrowthHeaderTable[numOneItemSets+1];		
		for (int index=1;index<headerTable.length;index++) 
		{
		    headerTable[index] = new FPgrowthHeaderTable((short) index);
		}
		    
		// 生成FP-tree	
		for (int index=0;index<dataArray.length;index++) 
		{
		    if (dataArray[index] != null) 
		    	addToFPtree(rootNode,0,dataArray[index],1,headerTable);
		}         	                                                  
	}  
    /** 将数组中的一行添加到FP-tree中 */    
    private void addToFPtree(FPtreeNode ref, int place, short[] itemSet, 
    				int support, FPgrowthHeaderTable[] headerRef) 
    {  
		if (place < itemSet.length) 
		{
		    if (!addToFPtree1(ref,place,itemSet,support,headerRef)) 
		    	addToFPtree2(ref,place,itemSet,support,headerRef);
		}
	}
    
    /** 如果FP-tree非空，我们就要查找可能的共同前缀 */    
    private boolean addToFPtree1(FPtreeNode ref, int place, short[] itemSet,
    			int support, FPgrowthHeaderTable[] headerRef) 
    {	
    	// 只处理已经有子数的FP-tree
		if (ref.childRefs != null) 
		{
		    for (int index=0;index<ref.childRefs.length;index++) 
		    {
		        // 如果已经属性标志已存在，则增加支持度计数值
		        if (itemSet[place] == ref.childRefs[index].node.itemName) 
		        {
		            ref.childRefs[index].node.itemCount =
			                 ref.childRefs[index].node.itemCount + support;
				    // 递归调用处理下一项
				    addToFPtree(ref.childRefs[index],place+1,itemSet,support,
				    		headerRef);
				    return true;
			    }
		        // 由于有序性，如果小于当前的孩子结点引用的标志值，则必须生成新的分支
		        if (itemSet[place] < ref.childRefs[index].node.itemName) 
		        	return false;
			}
		}	
		return false;
	}
   
    /** 为FP-tree增加一个新的分支 */    
    private void addToFPtree2(FPtreeNode ref, int place, short[] itemSet,
    				int support, FPgrowthHeaderTable[] headerRef) 
    {		
		// 生成新的FP-tree结点
		FPgrowthItemPrefixSubtreeNode newPrefixNode = new 
		    		FPgrowthItemPrefixSubtreeNode(itemSet[place],support,ref.node);
		FPtreeNode newFPtreeNode = new FPtreeNode(newPrefixNode);
		
		// 连接到项头表中
		addRefToFPgrowthHeaderTable(itemSet[place],newPrefixNode,headerRef);
		// 连接到FP-tree中
		ref.childRefs = reallocFPtreeChildRefs(ref.childRefs,newFPtreeNode);
		// 继续处理剩下的属性
		addRestOfitemSet(ref.childRefs[tempIndex]/* 刚刚新加入的结点*/,newPrefixNode/* 新结点作为未来的父亲结点*/,
						place+1,itemSet, support,headerRef);
	}
    
    /** 递归函数，将记录中剩余的属性标志加入到FP-tree中*/    
    private void addRestOfitemSet(FPtreeNode ref, FPgrowthItemPrefixSubtreeNode backRef, 
    				int place, short[] itemSet, int support, 
						FPgrowthHeaderTable[] headerRef) 
    {        
		// 递归出口
		if (place<itemSet.length) 
		{
		    // 生成一个新的FP-tree结点
		    FPgrowthItemPrefixSubtreeNode newPrefixNode = new
		    		FPgrowthItemPrefixSubtreeNode(itemSet[place],support,backRef);
		    FPtreeNode newFPtreeNode = new FPtreeNode(newPrefixNode);
		    // 连接到项头表中
		    addRefToFPgrowthHeaderTable(itemSet[place],newPrefixNode,headerRef);
			// 连接到FP-tree中
		    ref.childRefs = reallocFPtreeChildRefs(ref.childRefs,newFPtreeNode);
		    // 继续处理下一项
		    addRestOfitemSet(ref.childRefs[tempIndex],newPrefixNode,place+1,
		    					itemSet,support,headerRef);
		}
	}
   
    /** 添加新的FP-tree结点到项表头中的含有相同属性标志的链表中（添加在链表的最前面）*/    
    private void addRefToFPgrowthHeaderTable(short columnNumber, 
    		     FPgrowthItemPrefixSubtreeNode newNode, 
		     FPgrowthHeaderTable[] headerRef) 
    {
        FPgrowthItemPrefixSubtreeNode tempRef;
		// 循环查找
		for (int index=1;index<headerRef.length;index++) 
		{
		    // 找到正确的属性标志
		    if (columnNumber == headerRef[index].itemName) 
		    {
		        tempRef = headerRef[index].nodeLink;
				headerRef[index].nodeLink = newNode;
				newNode.nodeLink = tempRef;
				break;
			}
		}   
    }
    
    /** 挖掘FP-tree */    
    public void startMining() 
    {        
    	System.out.println("挖掘 FP-tree");	
    	// 挖掘FP-tree，生成数据结构Ttree
    	startMining(headerTable,null);	
		// 根据Ttree生成关联规则
		generateARs();
	}  
    /** FP增长算法挖掘频繁模式 */	    
    private void startMining(FPgrowthHeaderTable[] tableRef, 
    						       short[] itemSetSofar) 
    {		
		// 从尾部到头部，依次处理项头表中的每一项	
        int headerTableEnd = tableRef.length-1;
        for(int index=headerTableEnd;index>=1/*项头表的0位置未用*/;index--) 
        {
		    // nodeLink为空，表示此属性标志不属于频繁一项集
		    if (tableRef[index].nodeLink != null) 
		    {
		        // 由项头表找到FP-tree中包含相同属性标志的所有项
		        startMining(tableRef[index].nodeLink,tableRef[index].itemName,
							itemSetSofar);
		    }
	    }
	}	
    /**FP增长算法核心实现部分  */    	
    protected void startMining(FPgrowthItemPrefixSubtreeNode nodeLink,	
     				short itemName, short[] itemSetSofar) 
    {	
    	// 根据nodeLink计算此属性标志的支持度   	
        int counter = 0;
        FPgrowthItemPrefixSubtreeNode nodeLinkTemp = null;
        nodeLinkTemp = nodeLink;
        while(nodeLinkTemp != null) 
        {
		    counter = counter+nodeLinkTemp.itemCount;
		    nodeLinkTemp = nodeLinkTemp.nodeLink;		    
	    }    	
		int support = counter; 		
		short[] newCodeSofar = realloc2(itemSetSofar,itemName);
		
		addToTtree(newCodeSofar,support); 
		        
		/** 获得当前属性标志在FP-tree中的所有位置对应的所有祖先结点*/  
		startTempSets=null;
		generateAncestorCodes(nodeLink); 		
		// 处理祖先项目集，如果为空则结束，转到下一个属性标志
		if (startTempSets != null) 
		{
		    // 计算单属性的累计支持度
		    FPgrowthColumnCounts[] countArray = countFPgrowthSingles(); 
		    // 生成当前项头表
		    FPgrowthHeaderTable[] localHeaderTable = 
		    				createLocalHeaderTable(countArray); 
		    if (localHeaderTable != null) 
		    {
				// 去掉非频繁一项集
				pruneAncestorCodes(countArray); 
		        // 生成新的FP-tree
		        generateLocalFPtree(localHeaderTable);
				// 挖掘新生成的FP-tree
				startMining(localHeaderTable,newCodeSofar);
			}
	    }
	}
    
    /** 获得当前属性标志在FP=tree中的所有位置对应的所有祖先结点，并暂时存放于startTempSets所指向的链表中*/                 
    private void generateAncestorCodes(FPgrowthItemPrefixSubtreeNode ref) 
    {
        short[] ancestorCode = null;
		int support;

        while(ref != null) 
        {
        	// 获得当前结点的支持度计数
		    support = ref.itemCount;
		    // 获得当前结点的所有祖先结点的属性标志（由上至下有序）
		    ancestorCode = getAncestorCode(ref.parentRef);
		    // 如果具有祖先结点
		    if (ancestorCode != null)
		    	// 在链表前面插入新的一项
		    	startTempSets = new FPgrowthSupportedSets(ancestorCode,support,
									startTempSets);
		    // 下一项	
		    ref = ref.nodeLink;
	    }	
	}
    
    /** 根据给定结点获得所有祖先结点的属性标志 */    	
    private short[] getAncestorCode(FPgrowthItemPrefixSubtreeNode ref) 
    {
        short[] itemSet = null;
        
        // 没有父亲结点
        if (ref == null) 
        	return null;	
		// 获得所有祖先结点		
		while (ref != null) 
		{
		    itemSet = realloc2(itemSet,ref.itemName);
		    ref = ref.parentRef;
		}
		return itemSet;
	}
   
    /** 删除所有非频繁一项集  */    
    private void pruneAncestorCodes(FPgrowthColumnCounts[] countArray) 
    {
		FPgrowthSupportedSets ref = startTempSets;
		
		// 循环处理所有的祖先项目集		
		while(ref != null) 
		{ 
		    for(int index=0;index<ref.itemSet.length;index++) 
		    {
		        if (countArray[ref.itemSet[index]].support < minSupport)
		        	ref.itemSet = removeElementN(ref.itemSet,index);
		    }
		    ref = ref.nodeLink;
	    }
	}
    /** 从数组中删除下标为n的项*/    
    protected short[] removeElementN(short [] oldItemSet, int n) 
    {
        if (oldItemSet.length <= n) 
        	return oldItemSet;
		else 
		{
		    short[] newItemSet = new short[oldItemSet.length-1];
		    for (int index=0;index<n;index++) 
		    	newItemSet[index] = oldItemSet[index];
		    for (int index=n+1;index<oldItemSet.length;index++) 
		        newItemSet[index-1] = oldItemSet[index];
		    return newItemSet;
		}
	}
   
    /**计算FP-tree中单属性的累计支持度 */    
    private FPgrowthColumnCounts[] countFPgrowthSingles() 
    {
        int index, place=0;
        // 得到祖先项目集链表
		FPgrowthSupportedSets nodeLink = startTempSets;
		
		// 申请足够的空间，便于查找与赋值		
		FPgrowthColumnCounts[] countArray = new 
						FPgrowthColumnCounts[numOneItemSets+1];			
		// 初始化数组	
		for (index=1;index<numOneItemSets+1;index++) 
			countArray[index] = new FPgrowthColumnCounts(index);
		    
		// 循环处理所有祖先项目集		
		while (nodeLink != null) 
		{
		    for (index=0;index<nodeLink.itemSet.length;index++) 
		    {
				place = nodeLink.itemSet[index];
				countArray[place].support = countArray[place].support +
					nodeLink.support;
			}
		    nodeLink = nodeLink.nodeLink;
	    }    
		return countArray;
	}
  
    /** 为条件FP-tree生成项头表*/    
    private FPgrowthHeaderTable[] 
    		createLocalHeaderTable(FPgrowthColumnCounts[] countArray) 
    {
		int counter = 1;	
		// 计算当前的频繁一项集个数，	 
		for (int index=1;index<countArray.length;index++) 
		{
		    if (countArray[index].support >= minSupport) 
		    	counter++;
		}
	    
		// 为项头表申请空间	
		if (counter == 1) 
			return null;
		FPgrowthHeaderTable[] localHeaderTable = 
						new FPgrowthHeaderTable[counter];
		    
		// 根据当前新的频繁一项集为项头表赋值	
		int place=1;
		for (int index=1;index<countArray.length;index++) 
		{
		    if (countArray[index].support >= minSupport) 
		    {
		        localHeaderTable[place] = new 
			    FPgrowthHeaderTable((short) countArray[index].columnNum);    
		        place++;
		    }
		}    	

		return localHeaderTable;
	}		
 
    /** 根据修剪后的祖先项目集来生成新的FP-tree（条件FP-tree）*/    
    private FPtreeNode generateLocalFPtree(FPgrowthHeaderTable[] tableRef) 
    {
         FPgrowthSupportedSets ref = startTempSets;
         FPtreeNode localRoot = new FPtreeNode(); 

         while(ref != null) 
         { 	 
        	 // 添加到条件FP-tree中   
        	 if (ref.itemSet != null) 
        		 addToFPtree(localRoot,0,ref.itemSet, ref.support,tableRef);  
	       	 ref = ref.nodeLink;
	    }
         return localRoot;
	} 
   
    /** 分配空间，加入一个新结点 */    
    private FPtreeNode[] reallocFPtreeChildRefs(FPtreeNode[] oldArray, 
    			FPtreeNode newNode) 
    {

		if (oldArray == null) 
		{
		    FPtreeNode[] newArray = {newNode};
		    // 新的孩子结点加入的位置下标
		    tempIndex = 0;
		    return newArray;
		}
		
		// 一次增加一个节点的引用		
		int oldArrayLength = oldArray.length;
		FPtreeNode[] newArray = new FPtreeNode[oldArrayLength+1];
		
		// 插入一个新结点		
		for (int index1=0;index1 < oldArrayLength;index1++) 
		{
		    if (newNode.node.itemName < oldArray[index1].node.itemName) 
		    {
				newArray[index1] = newNode;
				for (int index2=index1;index2<oldArrayLength;index2++)
				    newArray[index2+1] = oldArray[index2];
				tempIndex = index1;
				return newArray;
			}
		    newArray[index1] = oldArray[index1];
		}

		newArray[oldArrayLength] = newNode;
		tempIndex = oldArrayLength;
		return newArray;
	}
  
    /** 将FP-tree打印到屏幕上 */    
    public void outputFPtree() 
    {
        outputFPtreeNode2(rootNode.childRefs,"");
	}
    /** 递归调用，输入FP-tree */    	
    private void outputFPtreeNode2(FPtreeNode ref[],String nodeID) 
    {
    	// 子树为空则返回
        if (ref == null) 
        	return;	
        // 否则，输出所有子树	
        for (int index=0;index<ref.length;index++) 
        {
		    System.out.print("(" + nodeID + (index+1) + ") ");
	        System.out.print((reconvertItem(ref[index].node.itemName)) + ":" + ref[index].node.itemCount);
	        System.out.println();
		    outputFPtreeNode2(ref[index].childRefs,nodeID+(index+1)+"."/*标记层次关系*/);
	    }
	}

}

