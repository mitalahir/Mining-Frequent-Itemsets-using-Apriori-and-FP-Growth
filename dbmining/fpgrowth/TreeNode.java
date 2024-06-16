package fpgrowth;

public class TreeNode 
{   
    /** 相关支持度 */
    public int support = 0;   
    /** 孩子结点*/
    public TreeNode[] childRef = null;
    /** 当前结点个数*/
    public static int numberOfNodes = 0;
    /** 默认构造函数 */	
    public TreeNode() 
    {
    	numberOfNodes++;
	}	
    /** 含有一个参数的构造函数 */	
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
