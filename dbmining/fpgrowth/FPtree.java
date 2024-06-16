package fpgrowth;

// FP��                             
public class FPtree extends DataTree 
{
    
    /** FP-tree ���*/    
    protected class FPtreeNode 
    {
        /** FP-tree ���ṹ���������Ա�־��Ƶ�ʡ����׽������õ�*/
        private FPgrowthItemPrefixSubtreeNode node = null;
		/** ���ӽ�������*/ 
        private FPtreeNode[] childRefs = null;
        
		/** Ĭ�Ϲ��캯��*/	
		protected FPtreeNode() 
		{}  
		    
		/** ���������캯��*/		
		protected FPtreeNode(FPgrowthItemPrefixSubtreeNode newNode) 
		{
		    node = newNode;
	    }
	}
    
    /** ���ݽṹ��FPtree������������ӽ�� */   
    private class FPgrowthItemPrefixSubtreeNode 
    {
        /** ���Ա�־ */
        private short itemName;
		/** ֧�ֶȼ��� */
		private int itemCount;
		/** ���׽������� */
		private FPgrowthItemPrefixSubtreeNode parentRef = null;
		/**FP-tree�У�������ͬ���Ա�־������ */
		private FPgrowthItemPrefixSubtreeNode nodeLink = null;
		
		/** Ĭ�Ϲ��캯�� */		
		private FPgrowthItemPrefixSubtreeNode() 
		{}
		
		/** ���������캯�� */		
		private FPgrowthItemPrefixSubtreeNode(short name, int support, 
				FPgrowthItemPrefixSubtreeNode backRef) 
		{
		    itemName = name;
		    itemCount = support;
		    parentRef = backRef;
	    }
	}
    
    /** FP-tree��ͷ���б�������ݽṹ */    
    protected class FPgrowthHeaderTable 
    {
        /** ���Ա�־����1��ʼ��*/
    	protected short itemName;
    	/** �γ�ͳһ���Ե�һ���� */
        protected FPgrowthItemPrefixSubtreeNode nodeLink = null;
        
        //Ψһ�Ĺ��캯��
		protected FPgrowthHeaderTable (short columnNum) 
		{
		    itemName = columnNum;
	    }  
    }
	
    /** ���ݽṹ��������Ƚ��Ľṹ��������������FP-tree*/    
    private class FPgrowthSupportedSets 
    {
        /** ���Ա�־���� */
        private short[] itemSet = null;
        /** ��itemSet���Ӧ��֧�ֶ�*/
        private int support;
		/** �γ����� */
		private FPgrowthSupportedSets nodeLink = null;
	        
		/** ���������캯�� */		
		private FPgrowthSupportedSets(short[] newitemSet, int newSupport, 
				FPgrowthSupportedSets newNodeLink) 
		{
		    itemSet = newitemSet;
	        support = newSupport;
		    nodeLink = newNodeLink;
	    } 
	} 
    
    /**�Զ������ݽṹ�������������Ե��ۼ�֧�ֶ� */    
    private class FPgrowthColumnCounts 
    {
        /** ���Ա�־ */
        private short columnNum;
        /** ���֧�ֶ�*/
        private int support=0;
        
		/** ���������캯�� */	
		private FPgrowthColumnCounts(int column) 
		{
		    columnNum = (short) column;
		}  
 
	}   
	
    /** FP-tree���ڵ�*/
    protected FPtreeNode rootNode = null;
    /** FP-tree��ͷ�� */
    protected FPgrowthHeaderTable[] headerTable; 
    /** ��ʱ���Ƶ��һ������Ƚ�㼯*/
    private static FPgrowthSupportedSets startTempSets = null;
    
    /** ��ʱ���FP-tree����к��ӽ������������±꣨��Ϊ�������·���ռ�ʱ��*/
    private int tempIndex  = 0;    	
        
    /** 
     * ���������в������� FPtree 
     * */  
    public FPtree(String[] args) 
    {
    	super(args);
	
		// ��ʼ������㣬Ϊ��
		rootNode = new FPtreeNode();
		
		// ������ͷ��	
		headerTable = new FPgrowthHeaderTable[numOneItemSets+1];

	}	
    /** ����Ԥ����*/ 
    public void preprocessDataSet()
    {
    	System.out.println("Ԥ����"+fileName);
    	preprocessDataSet(fileName);
    }
   
    /** �����޼��Ժ�����ݼ�������FP-tree */    
    public void createFPtree() 
    {    
    	System.out.println("���� FP-tree");
		// ������ͷ��	
		headerTable = new FPgrowthHeaderTable[numOneItemSets+1];		
		for (int index=1;index<headerTable.length;index++) 
		{
		    headerTable[index] = new FPgrowthHeaderTable((short) index);
		}
		    
		// ����FP-tree	
		for (int index=0;index<dataArray.length;index++) 
		{
		    if (dataArray[index] != null) 
		    	addToFPtree(rootNode,0,dataArray[index],1,headerTable);
		}         	                                                  
	}  
    /** �������е�һ����ӵ�FP-tree�� */    
    private void addToFPtree(FPtreeNode ref, int place, short[] itemSet, 
    				int support, FPgrowthHeaderTable[] headerRef) 
    {  
		if (place < itemSet.length) 
		{
		    if (!addToFPtree1(ref,place,itemSet,support,headerRef)) 
		    	addToFPtree2(ref,place,itemSet,support,headerRef);
		}
	}
    
    /** ���FP-tree�ǿգ����Ǿ�Ҫ���ҿ��ܵĹ�ͬǰ׺ */    
    private boolean addToFPtree1(FPtreeNode ref, int place, short[] itemSet,
    			int support, FPgrowthHeaderTable[] headerRef) 
    {	
    	// ֻ�����Ѿ���������FP-tree
		if (ref.childRefs != null) 
		{
		    for (int index=0;index<ref.childRefs.length;index++) 
		    {
		        // ����Ѿ����Ա�־�Ѵ��ڣ�������֧�ֶȼ���ֵ
		        if (itemSet[place] == ref.childRefs[index].node.itemName) 
		        {
		            ref.childRefs[index].node.itemCount =
			                 ref.childRefs[index].node.itemCount + support;
				    // �ݹ���ô�����һ��
				    addToFPtree(ref.childRefs[index],place+1,itemSet,support,
				    		headerRef);
				    return true;
			    }
		        // ���������ԣ����С�ڵ�ǰ�ĺ��ӽ�����õı�־ֵ������������µķ�֧
		        if (itemSet[place] < ref.childRefs[index].node.itemName) 
		        	return false;
			}
		}	
		return false;
	}
   
    /** ΪFP-tree����һ���µķ�֧ */    
    private void addToFPtree2(FPtreeNode ref, int place, short[] itemSet,
    				int support, FPgrowthHeaderTable[] headerRef) 
    {		
		// �����µ�FP-tree���
		FPgrowthItemPrefixSubtreeNode newPrefixNode = new 
		    		FPgrowthItemPrefixSubtreeNode(itemSet[place],support,ref.node);
		FPtreeNode newFPtreeNode = new FPtreeNode(newPrefixNode);
		
		// ���ӵ���ͷ����
		addRefToFPgrowthHeaderTable(itemSet[place],newPrefixNode,headerRef);
		// ���ӵ�FP-tree��
		ref.childRefs = reallocFPtreeChildRefs(ref.childRefs,newFPtreeNode);
		// ��������ʣ�µ�����
		addRestOfitemSet(ref.childRefs[tempIndex]/* �ո��¼���Ľ��*/,newPrefixNode/* �½����Ϊδ���ĸ��׽��*/,
						place+1,itemSet, support,headerRef);
	}
    
    /** �ݹ麯��������¼��ʣ������Ա�־���뵽FP-tree��*/    
    private void addRestOfitemSet(FPtreeNode ref, FPgrowthItemPrefixSubtreeNode backRef, 
    				int place, short[] itemSet, int support, 
						FPgrowthHeaderTable[] headerRef) 
    {        
		// �ݹ����
		if (place<itemSet.length) 
		{
		    // ����һ���µ�FP-tree���
		    FPgrowthItemPrefixSubtreeNode newPrefixNode = new
		    		FPgrowthItemPrefixSubtreeNode(itemSet[place],support,backRef);
		    FPtreeNode newFPtreeNode = new FPtreeNode(newPrefixNode);
		    // ���ӵ���ͷ����
		    addRefToFPgrowthHeaderTable(itemSet[place],newPrefixNode,headerRef);
			// ���ӵ�FP-tree��
		    ref.childRefs = reallocFPtreeChildRefs(ref.childRefs,newFPtreeNode);
		    // ����������һ��
		    addRestOfitemSet(ref.childRefs[tempIndex],newPrefixNode,place+1,
		    					itemSet,support,headerRef);
		}
	}
   
    /** ����µ�FP-tree��㵽���ͷ�еĺ�����ͬ���Ա�־�������У�������������ǰ�棩*/    
    private void addRefToFPgrowthHeaderTable(short columnNumber, 
    		     FPgrowthItemPrefixSubtreeNode newNode, 
		     FPgrowthHeaderTable[] headerRef) 
    {
        FPgrowthItemPrefixSubtreeNode tempRef;
		// ѭ������
		for (int index=1;index<headerRef.length;index++) 
		{
		    // �ҵ���ȷ�����Ա�־
		    if (columnNumber == headerRef[index].itemName) 
		    {
		        tempRef = headerRef[index].nodeLink;
				headerRef[index].nodeLink = newNode;
				newNode.nodeLink = tempRef;
				break;
			}
		}   
    }
    
    /** �ھ�FP-tree */    
    public void startMining() 
    {        
    	System.out.println("�ھ� FP-tree");	
    	// �ھ�FP-tree���������ݽṹTtree
    	startMining(headerTable,null);	
		// ����Ttree���ɹ�������
		generateARs();
	}  
    /** FP�����㷨�ھ�Ƶ��ģʽ */	    
    private void startMining(FPgrowthHeaderTable[] tableRef, 
    						       short[] itemSetSofar) 
    {		
		// ��β����ͷ�������δ�����ͷ���е�ÿһ��	
        int headerTableEnd = tableRef.length-1;
        for(int index=headerTableEnd;index>=1/*��ͷ���0λ��δ��*/;index--) 
        {
		    // nodeLinkΪ�գ���ʾ�����Ա�־������Ƶ��һ�
		    if (tableRef[index].nodeLink != null) 
		    {
		        // ����ͷ���ҵ�FP-tree�а�����ͬ���Ա�־��������
		        startMining(tableRef[index].nodeLink,tableRef[index].itemName,
							itemSetSofar);
		    }
	    }
	}	
    /**FP�����㷨����ʵ�ֲ���  */    	
    protected void startMining(FPgrowthItemPrefixSubtreeNode nodeLink,	
     				short itemName, short[] itemSetSofar) 
    {	
    	// ����nodeLink��������Ա�־��֧�ֶ�   	
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
		        
		/** ��õ�ǰ���Ա�־��FP-tree�е�����λ�ö�Ӧ���������Ƚ��*/  
		startTempSets=null;
		generateAncestorCodes(nodeLink); 		
		// ����������Ŀ�������Ϊ���������ת����һ�����Ա�־
		if (startTempSets != null) 
		{
		    // ���㵥���Ե��ۼ�֧�ֶ�
		    FPgrowthColumnCounts[] countArray = countFPgrowthSingles(); 
		    // ���ɵ�ǰ��ͷ��
		    FPgrowthHeaderTable[] localHeaderTable = 
		    				createLocalHeaderTable(countArray); 
		    if (localHeaderTable != null) 
		    {
				// ȥ����Ƶ��һ�
				pruneAncestorCodes(countArray); 
		        // �����µ�FP-tree
		        generateLocalFPtree(localHeaderTable);
				// �ھ������ɵ�FP-tree
				startMining(localHeaderTable,newCodeSofar);
			}
	    }
	}
    
    /** ��õ�ǰ���Ա�־��FP=tree�е�����λ�ö�Ӧ���������Ƚ�㣬����ʱ�����startTempSets��ָ���������*/                 
    private void generateAncestorCodes(FPgrowthItemPrefixSubtreeNode ref) 
    {
        short[] ancestorCode = null;
		int support;

        while(ref != null) 
        {
        	// ��õ�ǰ����֧�ֶȼ���
		    support = ref.itemCount;
		    // ��õ�ǰ�����������Ƚ������Ա�־��������������
		    ancestorCode = getAncestorCode(ref.parentRef);
		    // ����������Ƚ��
		    if (ancestorCode != null)
		    	// ������ǰ������µ�һ��
		    	startTempSets = new FPgrowthSupportedSets(ancestorCode,support,
									startTempSets);
		    // ��һ��	
		    ref = ref.nodeLink;
	    }	
	}
    
    /** ���ݸ���������������Ƚ������Ա�־ */    	
    private short[] getAncestorCode(FPgrowthItemPrefixSubtreeNode ref) 
    {
        short[] itemSet = null;
        
        // û�и��׽��
        if (ref == null) 
        	return null;	
		// ����������Ƚ��		
		while (ref != null) 
		{
		    itemSet = realloc2(itemSet,ref.itemName);
		    ref = ref.parentRef;
		}
		return itemSet;
	}
   
    /** ɾ�����з�Ƶ��һ�  */    
    private void pruneAncestorCodes(FPgrowthColumnCounts[] countArray) 
    {
		FPgrowthSupportedSets ref = startTempSets;
		
		// ѭ���������е�������Ŀ��		
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
    /** ��������ɾ���±�Ϊn����*/    
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
   
    /**����FP-tree�е����Ե��ۼ�֧�ֶ� */    
    private FPgrowthColumnCounts[] countFPgrowthSingles() 
    {
        int index, place=0;
        // �õ�������Ŀ������
		FPgrowthSupportedSets nodeLink = startTempSets;
		
		// �����㹻�Ŀռ䣬���ڲ����븳ֵ		
		FPgrowthColumnCounts[] countArray = new 
						FPgrowthColumnCounts[numOneItemSets+1];			
		// ��ʼ������	
		for (index=1;index<numOneItemSets+1;index++) 
			countArray[index] = new FPgrowthColumnCounts(index);
		    
		// ѭ����������������Ŀ��		
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
  
    /** Ϊ����FP-tree������ͷ��*/    
    private FPgrowthHeaderTable[] 
    		createLocalHeaderTable(FPgrowthColumnCounts[] countArray) 
    {
		int counter = 1;	
		// ���㵱ǰ��Ƶ��һ�������	 
		for (int index=1;index<countArray.length;index++) 
		{
		    if (countArray[index].support >= minSupport) 
		    	counter++;
		}
	    
		// Ϊ��ͷ������ռ�	
		if (counter == 1) 
			return null;
		FPgrowthHeaderTable[] localHeaderTable = 
						new FPgrowthHeaderTable[counter];
		    
		// ���ݵ�ǰ�µ�Ƶ��һ�Ϊ��ͷ��ֵ	
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
 
    /** �����޼����������Ŀ���������µ�FP-tree������FP-tree��*/    
    private FPtreeNode generateLocalFPtree(FPgrowthHeaderTable[] tableRef) 
    {
         FPgrowthSupportedSets ref = startTempSets;
         FPtreeNode localRoot = new FPtreeNode(); 

         while(ref != null) 
         { 	 
        	 // ��ӵ�����FP-tree��   
        	 if (ref.itemSet != null) 
        		 addToFPtree(localRoot,0,ref.itemSet, ref.support,tableRef);  
	       	 ref = ref.nodeLink;
	    }
         return localRoot;
	} 
   
    /** ����ռ䣬����һ���½�� */    
    private FPtreeNode[] reallocFPtreeChildRefs(FPtreeNode[] oldArray, 
    			FPtreeNode newNode) 
    {

		if (oldArray == null) 
		{
		    FPtreeNode[] newArray = {newNode};
		    // �µĺ��ӽ������λ���±�
		    tempIndex = 0;
		    return newArray;
		}
		
		// һ������һ���ڵ������		
		int oldArrayLength = oldArray.length;
		FPtreeNode[] newArray = new FPtreeNode[oldArrayLength+1];
		
		// ����һ���½��		
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
  
    /** ��FP-tree��ӡ����Ļ�� */    
    public void outputFPtree() 
    {
        outputFPtreeNode2(rootNode.childRefs,"");
	}
    /** �ݹ���ã�����FP-tree */    	
    private void outputFPtreeNode2(FPtreeNode ref[],String nodeID) 
    {
    	// ����Ϊ���򷵻�
        if (ref == null) 
        	return;	
        // ���������������	
        for (int index=0;index<ref.length;index++) 
        {
		    System.out.print("(" + nodeID + (index+1) + ") ");
	        System.out.print((reconvertItem(ref[index].node.itemName)) + ":" + ref[index].node.itemCount);
	        System.out.println();
		    outputFPtreeNode2(ref[index].childRefs,nodeID+(index+1)+"."/*��ǲ�ι�ϵ*/);
	    }
	}

}

