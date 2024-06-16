package fpgrowth;

import java.io.*;
import java.util.*;


public class AssociationRuleMining 
{	
	// ����������
    protected class RuleNode 
    {
    	/** ���������ǰ��*/
		protected short[] antecedent;
		/** ��������Ľ��*/
		protected short[] consequent;
		/** ������������Ŷ�*/
		double confidenceForRule=0.0;
		/** ָ����һ���*/
		RuleNode next = null;
		
		/** �������Ĺ��캯�� */		
		protected RuleNode(short[] ante, short[]cons, double confValue) 
		{
		    antecedent        = ante;
		    consequent        = cons;
		    confidenceForRule = confValue;
		}		
	}
    
    /** ��վ������־���ݼ��������� */
    protected class Attr
	{
		// ���Ա�־
		public short ID; 
		// the title of the Vroot
		public String titel = null;
		// ���URL
		public String url = null;
		
		public Attr()
		{
		}
	}
    // ���ݼ�Ԥ������ص��ļ���д����ԭ���ݣ��������һ����ʽ�������ݣ�Ĭ���ļ���my.data��
    protected BufferedReader fileInputForPreproc;
    protected BufferedWriter fileOuputForPreproc;
    /** �������ݼ��������δ��Ԥ����֮ǰ */
    protected Attr [] attrsOfDataSet = null;
	
    /** ����������� */
    protected RuleNode startRulelist = null;	
    /** ��ά���飬�����������ݼ� */
    protected short[][] dataArray = null;
    /** ��������������������һ�������˵�������һ��ȼ�ת�������������Ż��㷨��һ��Ҫע�⣩*/
    protected int[][] conversionArray   = null;
    protected short[] reconversionArray = null;
	
	// �������壬�����޶�֧�ֶȺ����Ŷȵķ�Χ
    private static final double MIN_SUPPORT = 0.0;
    private static final double MAX_SUPPORT = 100.0;
    private static final double MIN_CONFIDENCE = 0.0;
    private static final double MAX_CONFIDENCE = 100.0;	
    
    /** ���ݼ��ļ��� */
    protected String  fileName   = null;
    /** ���ݼ����Ը������������Ա�־Ϊ��1��ĳ�����ֵ��������ֵ��Ϊ���� */
    protected int     numCols    = 0;
    /** ���ݼ��ļ�������Ҳ����¼���� */
    protected int     numRows    = 0;
    /** ֧�ֶ� */
    protected double  support    = 20.0;
    /** ��С֧�ֶȣ���¼����  */
    protected double  minSupport = 0;
    /** ���Ŷ�*/
    protected double  confidence = 80.0;
    /** Ƶ��һ���Ŀ */
    protected int numOneItemSets = 0;

    /** ���ݼ��Ƿ�Ԥ���� */
    protected boolean preprocessFlag = false;
    /** �����в�����־ */
    protected boolean errorFlag  = true;
    /** ���ݼ���ʽ��ȷ��־ */
    protected boolean inputFormatOkFlag = true;
    /** �Ƿ��ý������е�Ƶ��һ�*/
    protected boolean isOrderedFlag = false;

    /** �ļ������� */
    protected BufferedReader fileInput;
    /** �ļ�·����Ϊ����չ */
    protected File filePath = null;


    /** 
     * ���캯�� 
     * */
    public AssociationRuleMining(String[] args) 
    {
    	// �������������в���
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
    	            System.out.println("�������: ����ʶ����� " +
    		    		"-" + flag + argument);
    			    errorFlag = false;
    		    }
            }
            else 
            {
    		    errorFlag = false;
            }
		}

		// �����в������
    	if ((support < MIN_SUPPORT) || (support > MAX_SUPPORT)) 
		{
			System.out.println("�������: ֧�ֶ�Ӧ" +
			 			"λ�� (" + MIN_SUPPORT +
			 				" - " + MAX_SUPPORT + ")");
		    errorFlag = false;
		}
    	
    	if ((confidence < MIN_CONFIDENCE) || (confidence > MAX_CONFIDENCE)) 
		{
			System.out.println("�������: ���Ŷ�Ӧ " +
			               "λ�� (" + MIN_CONFIDENCE +
						" - " + MAX_CONFIDENCE + ")");
		    errorFlag = false;
		}
    	
    	if (fileName == null) 
		{
		    System.out.println("�������: ����ָ�����ݼ��ļ���");
	        errorFlag = false;
		}
    	// ������������ʾ��ȷ��ʽ
		if (!errorFlag)  
			displayCmd();
    }
    
    /* �������ݼ�����ά����dataArray�� */
    public void preprocessDataSet(String oldFileName)
    {
    	if(!preprocessFlag)
    	{
    		//  ���ȵõ����ݼ��е����Ը������������ڴ�ռ䣬����ȫ������ 
    		int counter = 0;
    		try 
    		{
    		    // �����ݼ��ļ�
    		    FileReader file = new FileReader(oldFileName);
    		    fileInputForPreproc = new BufferedReader(file);
    		    //System.out.println("���ļ��ɹ�");
    		    
    		    String line = fileInputForPreproc.readLine();
        		while(line!=null)
        		{
        			if(line.charAt(0)=='A')// ����
        			{
        				counter++;
        			}
        			//���������վ������־���ݼ��ĸ�ʽ�йأ��뿴�ļ�anonymous-msweb.data
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
    		// ��ʼ������
    		for(int index = 0;index<attrsOfDataSet.length;index++)
    		{
    			attrsOfDataSet[index]= new Attr();
    		}
    		
    		counter = 0;
    		try
    		{
    		    FileReader file = new FileReader(oldFileName);
    		    fileInputForPreproc = new BufferedReader(file);
    		    //System.out.println("���ļ��ɹ�");
    		    
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
    		// �Ƿ�Ϊ��һ����¼�Ŀ�ʼ
    		boolean startflag = true;
    		try
    		{
    		    FileReader file = new FileReader(oldFileName);
    		    fileInputForPreproc = new BufferedReader(file);
    		    //System.out.println("���ļ�"+oldFileName);
    		    
    		    // ���һ����¼�е�������Ŀ
    		    short[]dataofline = new short[attrsOfDataSet.length];
    		    int counterforitem = 0;// Ϊһ����¼�е���Ŀ����¼
    		    
    			String line = fileInputForPreproc.readLine();			
    			while(line!=null)
    			{
    				if(line.charAt(0)=='C')
    				{ 
    					// ��¼��ʼ���������
    					if(startflag)
    					{
    						startflag = false;
    						// �ھ��㷨��������my.data�����в���
    						FileWriter file2 = new FileWriter("my.data");
    						fileOuputForPreproc = new BufferedWriter(file2);
    						counterforitem = 0;// ��ʼ����
    					}
    					else
    					{
    						// �����һ����¼
    						String str = "";
    						for(int i=0;i<counterforitem;i++)
    						{
    							str = str+dataofline[i]+" ";
    						}
    						str+="\n";
    						fileOuputForPreproc.write(str);
    						fileOuputForPreproc.flush();
    						// ��һ����¼��ʼ
    						counterforitem = 0;// ���¿�ʼ����
    					}
    				}
    				if(line.charAt(0)=='V')// ��¼�е�һ��
    				{
    					counterforitem = counterforitem + 1;// ��Ŀ������
    					short temp = 0;
    					temp = new Short(line.split(",")[1]).shortValue();
    					for(short index = 1;index <attrsOfDataSet.length;index ++)
    					{
    						if(temp == attrsOfDataSet[index].ID)
    						{
    							for(int i=0;i<counterforitem-1;i++)
    							{
    								// ���뵽�������ȷλ�ã���ֵ��������
    								if(index<dataofline[i])
    								{
    									temp = dataofline[i];
    									dataofline[i]=index;
    									index = temp;
    								}
    							}
    							// ���ľ�Ӧ�÷ŵ����(ת��ID����1��ʼ����)
    							dataofline[counterforitem-1]=index;    							
    							break;
    						}
    					}
    				}
    				line = fileInputForPreproc.readLine();
    			}
    			
    			// �ر��ļ������
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

    /* �������ݼ�����ά����dataArray�� */
    public void inputDataSet() 
    {
        // �����ݼ��ļ���������������ݼ��ļ��ĸ�ʽ
    	try 
        {
		    // ���ݸ�ʽ�Ƿ���ȷ�ı�־
		    inputFormatOkFlag=true;
		    numRows = getNumberOfLines(fileName);
		    if (inputFormatOkFlag) 
		    {
		    	// �������ݼ���������ȷ�����������
		        dataArray = new short[numRows][];
		        System.out.println();
		        System.out.println("��ʼ�����ݼ��ļ�: " + fileName);		        
		        readInputDataSet(fileName);
		        
		    	System.out.println("���ݼ�¼�� = " + numRows);
		    	countNumCols();
		    	System.out.println("���������� = " + numCols);
		    	minSupport = (numRows * support)/100.0;
	        	System.out.println("��С֧���� = " + twoDecPlaces(minSupport) + " (records)");
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

    /**��ȡ���ݼ������ݼ�¼������������˵���ļ������� */
    protected int getNumberOfLines(String nameOfFile) throws IOException 
    {
        int counter = 0;
        // �����ݼ��ļ�
        if (filePath==null) 
        	openFileName(nameOfFile);
        else 
        	openFilePath();

        // һ�ζ�ȡһ�У�ѭ����ȡ�õ����������ڶ�ȡ��ÿһ�У����ǽ��и�ʽ���
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

		// �ر��ļ�������
        closeFile();
        return counter;
	}

    /** ���ݼ��еļ�¼Ӧ�������������Ϳո���ɣ�
     ** ��������κ�������ʽ�����ݶ�����Ϊ�Ƿ�
     **/
    protected void checkLine(int counter, String str) 
    {
        for (int index=0;index <str.length();index++) 
        {
            if (!Character.isDigit(str.charAt(index)) &&
	    			!Character.isWhitespace(str.charAt(index))) 
            {
				System.out.println("���ݸ�ʽ����:\n" + "�� " + counter 
								+ " �����ݰ����Ƿ����ݸ�ʽ");
				System.exit(1);
            }
	    }
	}
    // ��ȡ���ݼ�
    protected void readInputDataSet(String fName) throws IOException 
    {
    	int rowIndex=0;

    	// �����ݼ��ļ�
    	if (filePath==null) 
    		openFileName(fName);
    	else 
    		openFilePath();

    	// ѭ������ÿһ��
		String line = fileInput.readLine();
		while (line != null) 
		{
		    // ����һ��
		    if (!processInputLine(line,rowIndex)) 
		    	break;
		    // ��ά�������
		    rowIndex++;
	        line = fileInput.readLine();
		 }

		// �ر�������
		closeFile();
	}

    /** ��ȡ���ݼ��ļ��еĲ������ݣ���startRowIndex��endRowIndexȷ�� */
    protected void readInputDataSetSeg(String fName, int startRowIndex,
    					int endRowIndex) throws IOException 
    {
		// �����ݼ��ļ�
		if (filePath==null) 
			openFileName(fName);
		else 
			openFilePath();
	
		// ѭ������ÿһ��
		String line = fileInput.readLine();
		for (int index=startRowIndex;index<endRowIndex;index++) 
		{
		    processInputLine(line,index);
	        line = fileInput.readLine();
		}
	
		// �ر�������
		closeFile();
	}

    /**	�������һ�����ݷŵ���ά������ */
    private boolean processInputLine(String line, int rowIndex) 
    {
        // Ϊ���򷵻� false
    	if (line==null) 
    		return false;

    	StringTokenizer dataLine = new StringTokenizer(line);
        int numberOfTokens = dataLine.countTokens();
        // ���У��򵽴��ļ�β��
        if (numberOfTokens == 0) 
        	return false;

		// ����������ת��Ϊ������
		short[] code = binConversion(dataLine,numberOfTokens);
		// ���뵽��ά������
		int codeLength = code.length;
		dataArray[rowIndex] = new short[codeLength];
		for (int colIndex=0;colIndex<codeLength;colIndex++)
			dataArray[rowIndex][colIndex] = code[colIndex];
	
		return true;
	}

    /** �������ݼ���ʽ�������������� */
    protected void countNumCols() 
    {
        int maxAttribute=0;

        // ѭ���ҵ����ֵ���������ݼ��ļ��ĸ�ʽ�����ֵ��Ϊ����������
        for(int index=0;index<dataArray.length;index++) 
        {
		    int lastIndex = dataArray[index].length-1;
		    if (dataArray[index][lastIndex] > maxAttribute)
		    	maxAttribute = dataArray[index][lastIndex];
	    }

		numCols = maxAttribute;
		// Ĭ�ϵ�Ƶ��һ�����Ŀ
		numOneItemSets = numCols; 	
	}

    /** �����ݼ��ļ�*/
    protected void openFileName(String nameOfFile) 
    {
		try 
		{
		    // �����ݼ��ļ�
		    FileReader file = new FileReader(nameOfFile);
		    fileInput = new BufferedReader(file);
		 }
		catch(IOException e) 
		{
			System.err.print(e);
		    System.exit(1);
		}
	}

    /** ͬ�� */
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

    /** �ر��ļ�*/
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

    /**���ݸ�ʽת�������������������Ŀ���䶯̬�ռ�*/
    protected short[] binConversion(StringTokenizer dataLine,
    				int numberOfTokens)
    {
        short number;
        short[] newItemSet = null;
		
		for (int tokenCounter=0;tokenCounter < numberOfTokens;tokenCounter++) 
		{
	        number = new Short(dataLine.nextToken()).shortValue();
	        // ��̬����ռ�     
		    newItemSet = realloc1(newItemSet,number);
		}
		return newItemSet;
	}

    /** �������ݼ�����Ӧ���ڴ����飬��ð��������е�һ�
     * ��ͬƵ��һ�������δ����С֧�ֶȱȽϣ�
     */    
    public void getOrderedOneItem() 
    {	
		// ��ʼ����ά���飬�������һ�
		int[][] countArray = new int[numCols+1][2];
		for (int index=0;index<countArray.length;index++)
		{
		    countArray[index][0] = index;
		    countArray[index][1] = 0;
		}

		// �ɶ�ά����dataArray���õ����ݼ����������Ե�Ƶ�ʣ�����	���Ա�־���������У�	
		for(int rowIndex=0;rowIndex<dataArray.length;rowIndex++) 
		{
		    if (dataArray[rowIndex] != null) 
		    {
				for (int colIndex=0;colIndex<dataArray[rowIndex].length;
							colIndex++) 
					// ���������Ա�־���ص�����ģ����Ա�־�պÿ���Ϊ�±�
					countArray[dataArray[rowIndex][colIndex]][1]++;
			}
		}    
		// ð������	
		orderCountArray(countArray);
	       
		// ת�������飬�����Ժ�����㷨Ч��
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
    
    /** ð��������countArray��ʹ���ɰ���Ƶ�ʽ������� */      
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

    /** �����ά�����е����ݣ��޼����ݵõ�ֻ����Ƶ��Ԫ�صĶ�ά���� */
    public void pruneUnsupportElements() 
    {
        short[] itemSet;
		int attribute;
	
		// ���ѭ��
        for(int rowIndex=0;rowIndex<dataArray.length;rowIndex++) 
        {
		    // ���Ϊ���У�����һ�У���һ����¼��
		    if (dataArray[rowIndex]!= null) 
		    {
		        itemSet = null;
		        // ѭ������ǰ��¼��ÿһ��Ԫ�أ�����Ƶ���Ƿ�С����С֧����
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

        // ����Ƶ��һ��ĸ���
        int counter = 0;
		// ѭ������ÿһ��	
		for (int index=1;index < conversionArray.length;index++) 
		{
			if (conversionArray[index][1] >= minSupport) 
				counter++;
	    }
        numOneItemSets = counter;
	}

    /** ����ת����������Ա�־��ת����Ϊ��ά���飬������һ�ʱ�õ���*/    
    protected short reconvertItem(short item) 
    {
        // ת��ʧ���򷵻�ԭ��ֵ
		if (reconversionArray==null) 
			return item; 

		return reconversionArray[item];
	}

    /** ��������������в����µ�һ��������ŶȽ������У� */
    protected void insertRuleintoRulelist(short[] antecedent,
    				short[] consequent, double confidenceForRule) 
    {
		// �����µĽ��
		RuleNode newNode = new RuleNode(antecedent,consequent,
								confidenceForRule);
	
		// �����ǰ��δ�й�������
		if (startRulelist == null)
		{
		    startRulelist = newNode;
		    return;
		}
	
		// �����Ŷ��ɸߵ��ײ����¹���������
		
		// �����β��
		if (confidenceForRule > startRulelist.confidenceForRule) 
		{
		    newNode.next = startRulelist;
		    startRulelist  = newNode;
		    return;
		}
		// ��������м����
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
	
		// �����β��
		markerNode.next = newNode;
	}

    /** ���·���ռ䣨ÿ�μ�һ���Ա����ÿռ䣩����������Ŀ */
    protected short[] reallocInsert(short[] oldItemSet, short newElement) 
    {

		if (oldItemSet == null) 
		{
		    short[] newItemSet = {newElement};
		    return newItemSet;
		}
	
		// ���·���ռ�
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
	
		// β��
		newItemSet[newItemSet.length-1] = newElement;
	
		return newItemSet;
	}

    /** ��̬�����ڴ�ռ䣬������µ�Ԫ�������·��䣬��߿ռ������ʣ���Ԫ�ؼӴ��������� */
    protected short[] realloc1(short[] oldItemSet, short newElement) 
    {

		if (oldItemSet == null) 
		{
		    short[] newItemSet = {newElement};
		    return newItemSet;
		}
		// ���·���ռ�
		int oldItemSetLength = oldItemSet.length;
		short[] newItemSet = new short[oldItemSetLength+1];

		int index;
		for (index=0;index < oldItemSetLength;index++)
			newItemSet[index] = oldItemSet[index];
		newItemSet[index] = newElement;

		return newItemSet;
	}

    /** ����ʵ��Ԫ�ظ�����̬����ռ䣬��߿ռ�������*/
    protected short[] realloc2(short[] oldItemSet, short newElement) 
    {
		// ����Ϊ��	
		if (oldItemSet == null) 
		{
		    short[] newItemSet = {newElement};
		    return newItemSet;
		}	
		// Ϊ�µ����������һ��Ŀռ䣬���������Ԫ��	
		int oldItemSetLength = oldItemSet.length;
		short[] newItemSet = new short[oldItemSetLength+1];
		// ����Ԫ�ط�������ĵ�һ��
		newItemSet[0] = newElement;
		for (int index=0;index < oldItemSetLength;index++)
			newItemSet[index+1] = oldItemSet[index];	
		return newItemSet;
	}
  
    /** �õ�����1��������2�Ĳ�����������{1,2}��{1,2,3}���򷵻�(3) */   
    protected short[] complement(short[] itemSet1, short[] itemSet2) 
    {
        int lengthOfComp = itemSet2.length-itemSet1.length;
	
        // ���Ⱦ����������в�����
        if (lengthOfComp<1) 
        	return null;
		// �����ϳ���
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
    
    /** �ж�ָ�����Ƿ��ڸ����ļ����� */    
    protected boolean notMemberOf(short number, short[] itemSet) 
    {        
    	// ѭ������
		for(int index=0;index<itemSet.length;index++) 
		{
		    if (number < itemSet[index]) 
		    	return true;
		    if (number == itemSet[index]) 
		    	return false;
	    }			
		return true;
	}
  
    /**�õ����е��������������������[1,2,3]�����ǽ��õ�
    ������� [[1],[2],[3],[1,2],[1,3],[2,3],[1,2,3]] */
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

    /** �ݹ����ѿ����� */
    private int combinations(short[] inputSet, int inputIndex,
    		short[] sofar, short[][] outputSet, int outputIndex) 
    {
    	short[] tempSet;
    	int index=inputIndex;
    	// ��ָ��λ���±�ѭ������һ��
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
  
    /** ���п��ܵ������� */   
    private int getCombinations(short[] set) 
    {
    	int numComb;	
    	numComb = (int) Math.pow(2.0,set.length)-1;	    
        return numComb;
    }
    
    /** ����Ŀ��� */ 
    protected short[] copyItemSet(short[] itemSet) 
    {
		// Ϊ�շ���
		if (itemSet == null) 
			return null;

		short[] newItemSet = new short[itemSet.length];
		for(int index=0;index<itemSet.length;index++) 
		{
		    newItemSet[index] = itemSet[index];
	    }

		return newItemSet;
	} 

    /** �����ʽ��ʾ*/
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
		
    /** ���ʱ��*/  
    public double outputDuration(double time1, double time2) 
    {
        double duration = (time2-time1)/1000;
        System.out.println("����ʱ�� = " + twoDecPlaces(duration) + 
			" �� (" + twoDecPlaces(duration/60) + " ��)");
        System.out.println();
        return duration;
	}
    // ������ֵ��ȷ�� 
    protected double twoDecPlaces(double number) 
    {
    	int numInt = (int) ((number+0.005)*100.0);
    	number = ((double) numInt)/100.0;
    	return number;
	}    

}
