package fpgrowth;

import java.io.*;
import java.util.*;


public class AssociationRuleMining 
{	
	// 关联规则结点
    protected class RuleNode 
    {
    	/** 关联规则的前提*/
		protected short[] antecedent;
		/** 关联规则的结果*/
		protected short[] consequent;
		/** 关联规则的置信度*/
		double confidenceForRule=0.0;
		/** 指向下一结点*/
		RuleNode next = null;
		
		/** 三参数的构造函数 */		
		protected RuleNode(short[] ante, short[]cons, double confValue) 
		{
		    antecedent        = ante;
		    consequent        = cons;
		    confidenceForRule = confValue;
		}		
	}
    
    /** 网站访问日志数据集的数据项 */
    protected class Attr
	{
		// 属性标志
		public short ID; 
		// the title of the Vroot
		public String titel = null;
		// 相对URL
		public String url = null;
		
		public Attr()
		{
		}
	}
    // 数据集预处理相关的文件读写，打开原数据，输出具有一定格式的新数据（默认文件名my.data）
    protected BufferedReader fileInputForPreproc;
    protected BufferedWriter fileOuputForPreproc;
    /** 保存数据集的数据项，未做预处理之前 */
    protected Attr [] attrsOfDataSet = null;
	
    /** 保存关联规则 */
    protected RuleNode startRulelist = null;	
    /** 二维数组，用来保存数据集 */
    protected short[][] dataArray = null;
    /** 下面的两个数组用来存放一项集，或者说由有序的一项集等价转换而来，用于优化算法（一定要注意）*/
    protected int[][] conversionArray   = null;
    protected short[] reconversionArray = null;
	
	// 常量定义，用来限定支持度和置信度的范围
    private static final double MIN_SUPPORT = 0.0;
    private static final double MAX_SUPPORT = 100.0;
    private static final double MIN_CONFIDENCE = 0.0;
    private static final double MAX_CONFIDENCE = 100.0;	
    
    /** 数据集文件名 */
    protected String  fileName   = null;
    /** 数据集属性个数，由于属性标志为由1到某个最大值，则此最大值即为个数 */
    protected int     numCols    = 0;
    /** 数据集文件行数，也即记录条数 */
    protected int     numRows    = 0;
    /** 支持度 */
    protected double  support    = 20.0;
    /** 最小支持度（记录数）  */
    protected double  minSupport = 0;
    /** 置信度*/
    protected double  confidence = 80.0;
    /** 频繁一项集数目 */
    protected int numOneItemSets = 0;

    /** 数据集是否被预处理 */
    protected boolean preprocessFlag = false;
    /** 命令行参数标志 */
    protected boolean errorFlag  = true;
    /** 数据集格式正确标志 */
    protected boolean inputFormatOkFlag = true;
    /** 是否获得降序排列的频繁一项集*/
    protected boolean isOrderedFlag = false;

    /** 文件输入流 */
    protected BufferedReader fileInput;
    /** 文件路径，为了扩展 */
    protected File filePath = null;


    /** 
     * 构造函数 
     * */
    public AssociationRuleMining(String[] args) 
    {
    	// 真正处理命令行参数
    	for(int index=0;index<args.length;index++) 
		{
    		String argument = args[index];
    		if (argument.charAt(0) == '-') 
            {
    		    char flag = argument.charAt(1);
    		    argument = argument.substring(3,argument.length());
    		    switch (flag) 
    		    {
    			case 'C':
    				confidence = Double.parseDouble(argument);
    			    break;
    	        case 'F':
    	    	    fileName = argument;
    	    	    break;
    	        case 'S':
    	            support = Double.parseDouble(argument);
    	            break;
    	        default:
    	            System.out.println("输入错误: 不可识别参数 " +
    		    		"-" + flag + argument);
    			    errorFlag = false;
    		    }
            }
            else 
            {
    		    errorFlag = false;
            }
		}

		// 命令行参数检查
    	if ((support < MIN_SUPPORT) || (support > MAX_SUPPORT)) 
		{
			System.out.println("输入错误: 支持度应" +
			 			"位于 (" + MIN_SUPPORT +
			 				" - " + MAX_SUPPORT + ")");
		    errorFlag = false;
		}
    	
    	if ((confidence < MIN_CONFIDENCE) || (confidence > MAX_CONFIDENCE)) 
		{
			System.out.println("输入错误: 置信度应 " +
			               "位于 (" + MIN_CONFIDENCE +
						" - " + MAX_CONFIDENCE + ")");
		    errorFlag = false;
		}
    	
    	if (fileName == null) 
		{
		    System.out.println("输入错误: 必须指定数据集文件名");
	        errorFlag = false;
		}
    	// 如果输入错误，提示正确格式
		if (!errorFlag)  
			displayCmd();
    }
    
    /* 读入数据集到二维数组dataArray中 */
    public void preprocessDataSet(String oldFileName)
    {
    	if(!preprocessFlag)
    	{
    		//  首先得到数据集中的属性个数，并申请内存空间，保存全部属性 
    		int counter = 0;
    		try 
    		{
    		    // 打开数据集文件
    		    FileReader file = new FileReader(oldFileName);
    		    fileInputForPreproc = new BufferedReader(file);
    		    //System.out.println("打开文件成功");
    		    
    		    String line = fileInputForPreproc.readLine();
        		while(line!=null)
        		{
        			if(line.charAt(0)=='A')// 属性
        			{
        				counter++;
        			}
        			//与给定的网站访问日志数据集的格式有关，请看文件anonymous-msweb.data
        			else if(line.charAt(0) == 'C'||line.charAt(0) == 'V')
        				break;
        			line = fileInputForPreproc.readLine();        			
        		}				
        		fileInputForPreproc.close();
    		 }
    		catch(IOException e) 
    		{
    			System.err.print(e);
    		    System.exit(1);
    		}
    		attrsOfDataSet = new Attr[counter+1];
    		// 初始化数组
    		for(int index = 0;index<attrsOfDataSet.length;index++)
    		{
    			attrsOfDataSet[index]= new Attr();
    		}
    		
    		counter = 0;
    		try
    		{
    		    FileReader file = new FileReader(oldFileName);
    		    fileInputForPreproc = new BufferedReader(file);
    		    //System.out.println("打开文件成功");
    		    
    			String line = fileInputForPreproc.readLine();
    			String[] temp = null;
    			while(line!=null)
    			{
    				if(line.charAt(0)=='A')
    				{
    					counter++;
    					temp = line.split(",");
    					attrsOfDataSet[counter].ID = new Short(temp[1]).shortValue();
    					attrsOfDataSet[counter].titel = temp[3];
    					attrsOfDataSet[counter].url = temp[4];
    				}
    				else if(line.charAt(0) == 'C'||line.charAt(0) == 'V')
    					break;
    				line = fileInputForPreproc.readLine();
    			}
    			fileInputForPreproc.close();
    		}
    		catch(IOException  e)
    		{
    			System.out.print(e);
    			System.exit(1);
    		}
    		
    		counter = 0;
    		// 是否为第一条记录的开始
    		boolean startflag = true;
    		try
    		{
    		    FileReader file = new FileReader(oldFileName);
    		    fileInputForPreproc = new BufferedReader(file);
    		    //System.out.println("打开文件"+oldFileName);
    		    
    		    // 存放一条记录中的所有项目
    		    short[]dataofline = new short[attrsOfDataSet.length];
    		    int counterforitem = 0;// 为一条记录中的项目数记录
    		    
    			String line = fileInputForPreproc.readLine();			
    			while(line!=null)
    			{
    				if(line.charAt(0)=='C')
    				{ 
    					// 记录开始，打开输出流
    					if(startflag)
    					{
    						startflag = false;
    						// 挖掘算法将对它（my.data）进行操作
    						FileWriter file2 = new FileWriter("my.data");
    						fileOuputForPreproc = new BufferedWriter(file2);
    						counterforitem = 0;// 开始计数
    					}
    					else
    					{
    						// 输出上一条记录
    						String str = "";
    						for(int i=0;i<counterforitem;i++)
    						{
    							str = str+dataofline[i]+" ";
    						}
    						str+="\n";
    						fileOuputForPreproc.write(str);
    						fileOuputForPreproc.flush();
    						// 新一条记录开始
    						counterforitem = 0;// 重新开始计数
    					}
    				}
    				if(line.charAt(0)=='V')// 记录中的一项
    				{
    					counterforitem = counterforitem + 1;// 项目数增加
    					short temp = 0;
    					temp = new Short(line.split(",")[1]).shortValue();
    					for(short index = 1;index <attrsOfDataSet.length;index ++)
    					{
    						if(temp == attrsOfDataSet[index].ID)
    						{
    							for(int i=0;i<counterforitem-1;i++)
    							{
    								// 插入到数组的正确位置（数值有序，升序）
    								if(index<dataofline[i])
    								{
    									temp = dataofline[i];
    									dataofline[i]=index;
    									index = temp;
    								}
    							}
    							// 最大的就应该放到最后(转换ID，从1开始计数)
    							dataofline[counterforitem-1]=index;    							
    							break;
    						}
    					}
    				}
    				line = fileInputForPreproc.readLine();
    			}
    			
    			// 关闭文件输出流
    			fileOuputForPreproc.close();
    		}
    		catch(IOException  e)
    		{
    			System.out.println(e);
    			System.exit(1);
    		}
    		fileName = "my.data";
    		// fileName = "sample.txt";
    	}
    }

    /* 读入数据集到二维数组dataArray中 */
    public void inputDataSet() 
    {
        // 读数据集文件，并初步检查数据集文件的格式
    	try 
        {
		    // 数据格式是否正确的标志
		    inputFormatOkFlag=true;
		    numRows = getNumberOfLines(fileName);
		    if (inputFormatOkFlag) 
		    {
		    	// 根据数据集的行数来确定数组的行数
		        dataArray = new short[numRows][];
		        System.out.println();
		        System.out.println("开始读数据集文件: " + fileName);		        
		        readInputDataSet(fileName);
		        
		    	System.out.println("数据记录数 = " + numRows);
		    	countNumCols();
		    	System.out.println("数据属性数 = " + numCols);
		    	minSupport = (numRows * support)/100.0;
	        	System.out.println("最小支持数 = " + twoDecPlaces(minSupport) + " (records)");
	        	System.out.println();
			}
	    }
        catch(IOException e)
        {
		    System.out.println(e);
		    closeFile();
		    System.exit(1);
	    }

	}

    /**获取数据集中数据记录的条数，或者说是文件的行数 */
    protected int getNumberOfLines(String nameOfFile) throws IOException 
    {
        int counter = 0;
        // 打开数据集文件
        if (filePath==null) 
        	openFileName(nameOfFile);
        else 
        	openFilePath();

        // 一次读取一行，循环读取得到行数，对于读取的每一行，我们进行格式检查
		String line = fileInput.readLine();
		while (line != null) 
		{
		    checkLine(counter+1,line);	
		    StringTokenizer dataLine = new StringTokenizer(line);
	        int numberOfTokens = dataLine.countTokens();
		    if (numberOfTokens == 0) 
		    	break;	
		    counter++;
	        line = fileInput.readLine();
		}

		// 关闭文件输入流
        closeFile();
        return counter;
	}

    /** 数据集中的记录应该是由正整数和空格组成，
     ** 如果包含任何其他格式的数据都将视为非法
     **/
    protected void checkLine(int counter, String str) 
    {
        for (int index=0;index <str.length();index++) 
        {
            if (!Character.isDigit(str.charAt(index)) &&
	    			!Character.isWhitespace(str.charAt(index))) 
            {
				System.out.println("数据格式错误:\n" + "第 " + counter 
								+ " 行数据包含非法数据格式");
				System.exit(1);
            }
	    }
	}
    // 读取数据集
    protected void readInputDataSet(String fName) throws IOException 
    {
    	int rowIndex=0;

    	// 打开数据集文件
    	if (filePath==null) 
    		openFileName(fName);
    	else 
    		openFilePath();

    	// 循环处理每一行
		String line = fileInput.readLine();
		while (line != null) 
		{
		    // 处理一行
		    if (!processInputLine(line,rowIndex)) 
		    	break;
		    // 二维数组的行
		    rowIndex++;
	        line = fileInput.readLine();
		 }

		// 关闭输入流
		closeFile();
	}

    /** 读取数据集文件中的部分数据，由startRowIndex和endRowIndex确定 */
    protected void readInputDataSetSeg(String fName, int startRowIndex,
    					int endRowIndex) throws IOException 
    {
		// 打开数据集文件
		if (filePath==null) 
			openFileName(fName);
		else 
			openFilePath();
	
		// 循环处理每一行
		String line = fileInput.readLine();
		for (int index=startRowIndex;index<endRowIndex;index++) 
		{
		    processInputLine(line,index);
	        line = fileInput.readLine();
		}
	
		// 关闭输入流
		closeFile();
	}

    /**	将读入的一行数据放到二维数组中 */
    private boolean processInputLine(String line, int rowIndex) 
    {
        // 为空则返回 false
    	if (line==null) 
    		return false;

    	StringTokenizer dataLine = new StringTokenizer(line);
        int numberOfTokens = dataLine.countTokens();
        // 空行，或到达文件尾部
        if (numberOfTokens == 0) 
        	return false;

		// 将此行数据转换为短整形
		short[] code = binConversion(dataLine,numberOfTokens);
		// 放入到二维数组中
		int codeLength = code.length;
		dataArray[rowIndex] = new short[codeLength];
		for (int colIndex=0;colIndex<codeLength;colIndex++)
			dataArray[rowIndex][colIndex] = code[colIndex];
	
		return true;
	}

    /** 根据数据集格式计算数据项种类 */
    protected void countNumCols() 
    {
        int maxAttribute=0;

        // 循环找到最大值，由于数据集文件的格式，最大值即为数据项种类
        for(int index=0;index<dataArray.length;index++) 
        {
		    int lastIndex = dataArray[index].length-1;
		    if (dataArray[index][lastIndex] > maxAttribute)
		    	maxAttribute = dataArray[index][lastIndex];
	    }

		numCols = maxAttribute;
		// 默认的频繁一项集的数目
		numOneItemSets = numCols; 	
	}

    /** 打开数据集文件*/
    protected void openFileName(String nameOfFile) 
    {
		try 
		{
		    // 打开数据集文件
		    FileReader file = new FileReader(nameOfFile);
		    fileInput = new BufferedReader(file);
		 }
		catch(IOException e) 
		{
			System.err.print(e);
		    System.exit(1);
		}
	}

    /** 同上 */
    protected void openFilePath() 
    {
		try 
		{
		    FileReader file = new FileReader(filePath);
		    fileInput = new BufferedReader(file);
		}
		catch(IOException e) 
		{
			System.err.print(e);
		    System.exit(1);
		}
	}

    /** 关闭文件*/
    protected void closeFile() 
    {
        if (fileInput != null) 
        {
		    try 
		    {
		    	fileInput.close();
			}
		    catch (IOException e) 
		    {
		    	System.err.print(e);
		        System.exit(1);
			}
	    }
	}

    /**数据格式转换，并根据数据项的数目分配动态空间*/
    protected short[] binConversion(StringTokenizer dataLine,
    				int numberOfTokens)
    {
        short number;
        short[] newItemSet = null;
		
		for (int tokenCounter=0;tokenCounter < numberOfTokens;tokenCounter++) 
		{
	        number = new Short(dataLine.nextToken()).shortValue();
	        // 动态分配空间     
		    newItemSet = realloc1(newItemSet,number);
		}
		return newItemSet;
	}

    /** 处理数据集所对应的内存数组，获得按降序排列的一项集
     * （同频繁一项集的区别，未与最小支持度比较）
     */    
    public void getOrderedOneItem() 
    {	
		// 初始化二维数组，用来存放一项集
		int[][] countArray = new int[numCols+1][2];
		for (int index=0;index<countArray.length;index++)
		{
		    countArray[index][0] = index;
		    countArray[index][1] = 0;
		}

		// 由二维数组dataArray，得到数据集中所有属性的频率（按照	属性标志的升序排列）	
		for(int rowIndex=0;rowIndex<dataArray.length;rowIndex++) 
		{
		    if (dataArray[rowIndex] != null) 
		    {
				for (int colIndex=0;colIndex<dataArray[rowIndex].length;
							colIndex++) 
					// 这是由属性标志的特点决定的，属性标志刚好可作为下标
					countArray[dataArray[rowIndex][colIndex]][1]++;
			}
		}    
		// 冒泡排序	
		orderCountArray(countArray);
	       
		// 转换后数组，用于以后提高算法效率
		conversionArray   = new int[numCols+1][2];
        reconversionArray = new short[numCols+1];
	
		for(int index=1;index<countArray.length;index++) 
		{
		    conversionArray[countArray[index][0]][0] = index;
		    conversionArray[countArray[index][0]][1] = countArray[index][1];
			reconversionArray[index] = (short) countArray[index][0];
		}

		isOrderedFlag = true;
	}
    
    /** 冒泡排序处理countArray，使其变成按照频率降序排列 */      
    private void orderCountArray(int[][] countArray) 
    {
        int attribute, quantity;	
        boolean isOrdered;
        int index; 
               
        do 
        {
        	isOrdered = true;
            index  = 1; 
            while (index < (countArray.length-1)) 
            {
                if (countArray[index][1] >= countArray[index+1][1]) 
                	index++;
		        else 
		        {
		            isOrdered=false;
				    attribute = countArray[index][0];
				    quantity  = countArray[index][1];
		            countArray[index][0] = countArray[index+1][0];
		            countArray[index][1] = countArray[index+1][1];
                    countArray[index+1][0] = attribute;
                    countArray[index+1][1] = quantity;

                    index++;  
	            }
            }     
	    }
        while (isOrdered==false);
    }    

    /** 处理二维数组中的数据，修剪数据得到只包含频繁元素的二维数组 */
    public void pruneUnsupportElements() 
    {
        short[] itemSet;
		int attribute;
	
		// 外层循环
        for(int rowIndex=0;rowIndex<dataArray.length;rowIndex++) 
        {
		    // 如果为空行，则到下一行（下一条记录）
		    if (dataArray[rowIndex]!= null) 
		    {
		        itemSet = null;
		        // 循环处理当前记录的每一个元素，看其频率是否小于最小支持数
		    	for(int colIndex=0;colIndex<dataArray[rowIndex].length;colIndex++) 
		    	{
		            attribute = dataArray[rowIndex][colIndex];

				    if (conversionArray[attribute][1] >= minSupport) 
				    {
				    	itemSet = reallocInsert(itemSet,
			    		(short) conversionArray[attribute][0]);
			        }
			    }
		    	
		    	dataArray[rowIndex] = itemSet;
		    }
	    }

        // 重置频繁一项集的个数
        int counter = 0;
		// 循环处理每一项	
		for (int index=1;index < conversionArray.length;index++) 
		{
			if (conversionArray[index][1] >= minSupport) 
				counter++;
	    }
        numOneItemSets = counter;
	}

    /** 根据转换表，获得属性标志（转换表为二维数组，在生成一项集时得到）*/    
    protected short reconvertItem(short item) 
    {
        // 转换失败则返回原数值
		if (reconversionArray==null) 
			return item; 

		return reconversionArray[item];
	}

    /** 向关联规则链表中插入新的一项（按照置信度降序排列） */
    protected void insertRuleintoRulelist(short[] antecedent,
    				short[] consequent, double confidenceForRule) 
    {
		// 创建新的结点
		RuleNode newNode = new RuleNode(antecedent,consequent,
								confidenceForRule);
	
		// 如果当前还未有关联规则
		if (startRulelist == null)
		{
		    startRulelist = newNode;
		    return;
		}
	
		// 按置信度由高到底插入新关联规则结点
		
		// 链表的尾部
		if (confidenceForRule > startRulelist.confidenceForRule) 
		{
		    newNode.next = startRulelist;
		    startRulelist  = newNode;
		    return;
		}
		// 在链表的中间插入
		RuleNode markerNode = startRulelist;
		RuleNode linkRuleNode = startRulelist.next;
		while (linkRuleNode != null) 
		{
		    if (confidenceForRule > linkRuleNode.confidenceForRule) 
		    {
		        markerNode.next = newNode;
		        newNode.next = linkRuleNode;
		        return;
			}
		    markerNode = linkRuleNode;
		    linkRuleNode = linkRuleNode.next;
		}
	
		// 添加在尾部
		markerNode.next = newNode;
	}

    /** 重新分配空间（每次加一，以便利用空间），插入新项目 */
    protected short[] reallocInsert(short[] oldItemSet, short newElement) 
    {

		if (oldItemSet == null) 
		{
		    short[] newItemSet = {newElement};
		    return newItemSet;
		}
	
		// 重新分配空间
		int oldItemSetLength = oldItemSet.length;
		short[] newItemSet = new short[oldItemSetLength+1];

		int index1;
		for (index1=0;index1 < oldItemSetLength;index1++) 
		{
		    if (newElement < oldItemSet[index1]) 
		    {
				newItemSet[index1] = newElement;

				for(int index2 = index1+1;index2<newItemSet.length;index2++)
					newItemSet[index2] = oldItemSet[index2-1];
				return newItemSet;
			}
		    else 
		    	newItemSet[index1] = oldItemSet[index1];
		}
	
		// 尾部
		newItemSet[newItemSet.length-1] = newElement;
	
		return newItemSet;
	}

    /** 动态分配内存空间，如果有新的元素则重新分配，提高空间利用率（新元素加大数组的最后） */
    protected short[] realloc1(short[] oldItemSet, short newElement) 
    {

		if (oldItemSet == null) 
		{
		    short[] newItemSet = {newElement};
		    return newItemSet;
		}
		// 重新分配空间
		int oldItemSetLength = oldItemSet.length;
		short[] newItemSet = new short[oldItemSetLength+1];

		int index;
		for (index=0;index < oldItemSetLength;index++)
			newItemSet[index] = oldItemSet[index];
		newItemSet[index] = newElement;

		return newItemSet;
	}

    /** 根据实际元素个数动态申请空间，提高空间利用率*/
    protected short[] realloc2(short[] oldItemSet, short newElement) 
    {
		// 数组为空	
		if (oldItemSet == null) 
		{
		    short[] newItemSet = {newElement};
		    return newItemSet;
		}	
		// 为新的数组申请多一项的空间，用来存放新元素	
		int oldItemSetLength = oldItemSet.length;
		short[] newItemSet = new short[oldItemSetLength+1];
		// 新增元素放在数组的第一项
		newItemSet[0] = newElement;
		for (int index=0;index < oldItemSetLength;index++)
			newItemSet[index+1] = oldItemSet[index];	
		return newItemSet;
	}
  
    /** 得到数组1关于数组2的补，如我们有{1,2}与{1,2,3}，则返回(3) */   
    protected short[] complement(short[] itemSet1, short[] itemSet2) 
    {
        int lengthOfComp = itemSet2.length-itemSet1.length;
	
        // 长度决定不可能有补集合
        if (lengthOfComp<1) 
        	return null;
		// 补集合长度
		short[] complement  = new short[lengthOfComp];
		int complementIndex = 0;
		for(int index=0;index<itemSet2.length;index++) 
		{
		    if (notMemberOf(itemSet2[index],itemSet1)) 
		    {
		    	complement[complementIndex] = itemSet2[index];
		    	complementIndex++;
		    }	
	    }		
		return complement;
	}
    
    /** 判断指定项是否在给定的集合中 */    
    protected boolean notMemberOf(short number, short[] itemSet) 
    {        
    	// 循环处理
		for(int index=0;index<itemSet.length;index++) 
		{
		    if (number < itemSet[index]) 
		    	return true;
		    if (number == itemSet[index]) 
		    	return false;
	    }			
		return true;
	}
  
    /**得到所有的连接情况，例如我们有[1,2,3]，我们将得到
    结果集合 [[1],[2],[3],[1,2],[1,3],[2,3],[1,2,3]] */
    protected short[][] combinations(short[] inputSet) 
    {
		if (inputSet == null) 
			return null;
		else 
		{
		    short[][] outputSet = new short[getCombinations(inputSet)][];
		    combinations(inputSet,0,null,outputSet,0);
		    return outputSet;
	    }
	}

    /** 递归计算笛卡尔积 */
    private int combinations(short[] inputSet, int inputIndex,
    		short[] sofar, short[][] outputSet, int outputIndex) 
    {
    	short[] tempSet;
    	int index=inputIndex;
    	// 从指定位置下标循环处理一遍
		while(index < inputSet.length)
		{
            tempSet = realloc1(sofar,inputSet[index]);
            outputSet[outputIndex] = tempSet;
		    outputIndex = combinations(inputSet,index+1,
		    copyItemSet(tempSet),outputSet,outputIndex+1);
	    	index++;
	    }

    	return outputIndex;
   }   
  
    /** 所有可能的连接数 */   
    private int getCombinations(short[] set) 
    {
    	int numComb;	
    	numComb = (int) Math.pow(2.0,set.length)-1;	    
        return numComb;
    }
    
    /** 数组的拷贝 */ 
    protected short[] copyItemSet(short[] itemSet) 
    {
		// 为空返回
		if (itemSet == null) 
			return null;

		short[] newItemSet = new short[itemSet.length];
		for(int index=0;index<itemSet.length;index++) 
		{
		    newItemSet[index] = itemSet[index];
	    }

		return newItemSet;
	} 

    /** 命令格式提示*/
    protected void displayCmd() 
    {
        System.out.println();
        System.out.println("-C  = Confidence (default 80%)");
        System.out.println("-F  = File name");
        System.out.println("-N  = Number of classes (Optional)");
        System.out.println("-S  = Support (default 20%)");
        System.out.println();
        System.exit(1);
	}
		
    /** 输出时间*/  
    public double outputDuration(double time1, double time2) 
    {
        double duration = (time2-time1)/1000;
        System.out.println("所耗时间 = " + twoDecPlaces(duration) + 
			" 秒 (" + twoDecPlaces(duration/60) + " 分)");
        System.out.println();
        return duration;
	}
    // 处理数值精确度 
    protected double twoDecPlaces(double number) 
    {
    	int numInt = (int) ((number+0.005)*100.0);
    	number = ((double) numInt)/100.0;
    	return number;
	}    

}
