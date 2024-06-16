package fpgrowth;

public class DataTree extends AssociationRuleMining
{  
    /** Ttree��������FP-tree */
    protected TreeNode[] startTtreeRef;

    /** 
     * ���캯�� 
     * */   
    public DataTree(String[] args) 
    {
    	super(args);
	}
  
    /** ���µ���Ŀ����ӵ�T-tree�� */    
    public void addToTtree(short[] itemSet, int support) 
    {
        // �����������һ��
    	int endIndex = itemSet.length-1;	

        startTtreeRef = addToTtree(startTtreeRef,numOneItemSets+1,
			endIndex,itemSet,support);
	}
   
    /** �ݹ麯������T-tree�м����µĽ�� */    
    protected TreeNode[] addToTtree(TreeNode[] linkRef, int size, int endIndex,
    				short[] itemSet, int support) 
    {
		// T-treeΪ�ջ�յķ�֧	
		if (linkRef == null) 
		{
		    linkRef = new TreeNode[size];
		    for(int index=1;index<linkRef.length;index++) 
				linkRef[index] = null;
	    }
	
		// ��õ�ǰ���Ա�־���������Ա�־�ҵ�T-tree����Ӧ����
		int currentAttribute = itemSet[endIndex]; 
		if (linkRef[currentAttribute] == null)
			linkRef[currentAttribute] = new TreeNode();
		// �뵱ǰ������Ӧ
		if (endIndex == 0) 
		{
		    linkRef[currentAttribute].support =
		    			linkRef[currentAttribute].support + support;
		    return linkRef;
		}	   
		// ��һ����	
		linkRef[currentAttribute].childRef = 
			addToTtree(linkRef[currentAttribute].childRef,
				currentAttribute,endIndex-1,itemSet,support);

		return linkRef;
	}

    /** ѭ������Ttree���������� */
    protected void generateARs() 
    {
		for (int index=1;index <= numOneItemSets;index++) 
		{
		    if (startTtreeRef[index] !=null) 
		    {
		    	// ֻ�й�������ֻӦ����Ƶ����
		        if (startTtreeRef[index].support >= minSupport) 
		        {
		        	// Ttree��һ�㣬���������������ֻ�ṩһ��Ƶ����
		            short[] itemSetSoFar = new short[1];
		            itemSetSoFar[0] = (short) index;
		            generateARs(itemSetSoFar,index, startTtreeRef[index].childRef);
			    }
			}
	    }
	}
    /** �ݹ麯����Ϊÿһ�������������еĹ������� */
    protected void generateARs(short[] itemSetSofar, int size,
    							TreeNode[] linkRef) 
    {
		// �ﵽҶ��㣬�ݹ����
		if (linkRef == null) 
			return;
	
		// �м�ķ�֧���
		for (int index=1; index < size; index++) 
		{
		    if (linkRef[index] != null) 
		    {
		        if (linkRef[index].support >= minSupport) 
		        {
				    short[] tempItemSet = realloc2(itemSetSofar,(short) index);
				    // itemSetSofar���ٰ���������Բ�����������
				    generateARsFromItemset(tempItemSet,linkRef[index].support);
			        // �ݹ���ã�������ȣ�ģʽ����
				    generateARs(tempItemSet,index,linkRef[index].childRef);
		        }
		    }
	    }
	}

    /** ����������Ŀ���õ���������ļ��� */
    private void generateARsFromItemset(short[] itemSet, double support) 
    {
    	// �õ����е��������
    	short[][] combinations = combinations(itemSet);

		// ����õ��Ľ��
		for(int index=0;index<combinations.length;index++) 
		{
			// �ҵ����ϵĲ�
		    short[] complement = complement(combinations[index],itemSet);
		    // ��������ϲ��գ�����ܲ�������Ҫ��Ĺ�������
		    if (complement != null) 
		    {
		        double confidenceForAR = getConfidence(combinations[index], support);
		        // �������Ŷ�Ҫ��Ĺ�������
				if (confidenceForAR >= confidence) 
				{
					insertRuleintoRulelist(combinations[index], complement,confidenceForAR);
				}
		    }
		}
	}
   
    /**���ݹ��������ǰ�Ἧ���ܵ�֧�ֶȣ���ù����������Ŷ� */    
    protected double getConfidence(short[] antecedent, double support) 
    {
        // ���ǰ�Ἧ��֧�ֶ�
        double supportForAntecedent = (double) getSupportForItemSetInTtree(antecedent);				
		// ����õ����Ŷ�
		double confidenceForAR = ((double) support/supportForAntecedent)*10000;
		int tempConf = (int) confidenceForAR;
		confidenceForAR = (double) tempConf/100;
		return confidenceForAR; 
	}

    /** ��ø�����Ŀ����֧�ֶȣ��������������������Ŷ� */
    protected int getSupportForItemSetInTtree(short[] itemSet) 
    {
    	int endInd = itemSet.length-1;

    	// Ttree �е���Ŀ�����ţ����Դ���Ŀ�������һ�ʼ�Ƚ�
    	if (startTtreeRef[itemSet[endInd]] != null) 
    	{
		    // ��Ŀ�������һ��ȽϽ���
		    if (endInd == 0) 
		    	return(startTtreeRef[itemSet[0]].support);
		    // �Ƚ���һ��
		    else 
		    {
		    	TreeNode[] tempRef = startTtreeRef[itemSet[endInd]].childRef;
		        if (tempRef != null) 
		        	return(getSupForIsetInTtree2(itemSet, endInd-1,tempRef));
		    	// Ttree�в���ȫ��������Ŀ��
		        else 
		        	return 0;
		    }
	    }
    	// Ttree�в���������Ŀ��
    	else 
    		return 0;
	}

    /** �ݹ麯�������αȽ���Ŀ���е�ÿһ�� */
    private int getSupForIsetInTtree2(short[] itemSet, int index,
    							TreeNode[] linkRef) 
    {
    	if (linkRef[itemSet[index]] != null) 
    	{
    		// �����һ��ɹ����ݹ����
    		if (index == 0) 
    			return(linkRef[itemSet[0]].support);
    		// ��һ��
    		else if (linkRef[itemSet[index]].childRef != null)
	    	 return(getSupForIsetInTtree2(itemSet,index-1, linkRef[itemSet[index]].childRef));
    		else 
    			return 0;
	    }	
    	// Ttree�в���������Ŀ��
    	else 
    		return 0;    
    }


    /** ���Ƶ��ģʽ */
    public void outputFrequentSets() 
    {
		System.out.println("Ƶ��ģʽ���£�[N] {I} = S��NΪ���кţ�IΪ��Ŀ����SΪ֧�ֶȣ���");
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
			    			 index/*�±�ԽС��Ƶ��Խ�ߣ�����ΪҶ�������ͻ�Խ��*/,
			    			 startTtreeRef[index].childRef);
			    }
		    }
	    }
	}

    /**�ݹ麯������������е�����Ƶ��ģʽ */
    private int outputFrequentSets(int number, String itemSetSofar, int size,
    							TreeNode[] linkRef) 
    {
		if (linkRef == null) 
			return number;
	
		// �ǿ�
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

    /**����������ŶȵĹ������� */
    public void outputARs()
    {
    	if(startRulelist==null)
    	{
        	System.out.print("δ���������ŶȵĹ����������������������Ŷ�\n");
        	return;
    	}
    	System.out.println("������������([N] {P}����>{R} C��NΪ���кţ�PΪǰ�᣻RΪ�����CΪ���Ŷ�)��");
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
    		System.out.print("} ����> {");
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
        	System.out.print("δ���������ŶȵĹ�������������������֧�ֶȺ����Ŷ�\n");
        	return;
    	}
    	System.out.println("������������([N] {P}����>{R} C��NΪ���кţ�PΪǰ�᣻RΪ�����CΪ���Ŷ�)��");
    	RuleNode tempNode = null;
    	int num = 0;
    	for(tempNode=startRulelist;tempNode!=null;tempNode=tempNode.next)
    	{
    		num ++;
    		System.out.print("["+num+"] {");
    		for(int index=0;index<tempNode.antecedent.length;index ++)
    		{
    			System.out.print(""+attrsOfDataSet[reconvertItem(tempNode.antecedent[index])].titel/*�����Ա�־�����û���ʶ����Ϣ*/);
    			if(index+1<tempNode.antecedent.length)
    				System.out.print(" ");
    		}
    		System.out.print("} ����> {");
    		for(int index=0;index<tempNode.consequent.length;index ++)
    		{
    			System.out.print(""+attrsOfDataSet[reconvertItem(tempNode.consequent[index])].titel);
    			if(index+1<tempNode.consequent.length)
    				System.out.print(" ");
    		}
    		System.out.print("} "+tempNode.confidenceForRule+"%\n");
    	}
    }

    /** ��Ttree��ӡ����Ļ��
    public void outputTtree() 
    {
    	System.out.println("���ݽṹTtree");
		int number = 1;
		for (short index=1; index < startTtreeRef.length; index++) 
		{
			// �ǿ���
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
    /** �ݹ麯����������������������   
    private void outputTtree(String number, String itemSetSofar,
    				TreeNode[] linkRef) 
    {
		int num=1;
		number = number + ".";
		itemSetSofar = itemSetSofar + " ";
		
		// Ϊ��
		if (linkRef == null) 
			return;
		
		// ѭ��������������
		for (short index=1;index<linkRef.length;index++) 
		{
		    if (linkRef[index] != null) 
		    {
		        String newItemSet = itemSetSofar + (reconvertItem(index));
		        System.out.print("[" + number + num + "] {" + newItemSet);
		        System.out.println("} = " + linkRef[index].support);
		        // �ݹ���ã��������
		        outputTtree(number + num,newItemSet,linkRef[index].childRef); 
		        num++;
			}
	    }    
	}*/    
	
}
