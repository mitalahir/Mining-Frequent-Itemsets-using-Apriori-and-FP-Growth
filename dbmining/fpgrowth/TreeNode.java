package fpgrowth;

public class TreeNode 
{   
    /** ���֧�ֶ� */
    public int support = 0;   
    /** ���ӽ��*/
    public TreeNode[] childRef = null;
    /** ��ǰ������*/
    public static int numberOfNodes = 0;
    /** Ĭ�Ϲ��캯�� */	
    public TreeNode() 
    {
    	numberOfNodes++;
	}	
    /** ����һ�������Ĺ��캯�� */	
    public TreeNode(int sup) 
    {
		support = sup;
		numberOfNodes++;
	}
    public static int getNumberOfNodes()
    {
        return numberOfNodes;
	}

}
