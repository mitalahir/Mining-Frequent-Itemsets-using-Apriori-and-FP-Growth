package fpgrowth;

public class FPgrowth 
{
	/**
	 * ������ʽ  -C=80.0  -S=20.0 -F=sample.txt
	 */
	public static void main(String[] args) 
	{
		// ���������в������� FPtree ʵ��	
		FPtree myFPtree = new FPtree(args);	
		// ����Ԥ����
		myFPtree.preprocessDataSet();
		
		// ��ȡ���ݼ����ڴ�������
		myFPtree.inputDataSet();		
		// 	�����ڴ������ȡһ�
		myFPtree.getOrderedOneItem();
		// ���´����ڴ������е����ݣ�ȥ��Ƶ��С����С֧������Ԫ��
		myFPtree.pruneUnsupportElements(); 
		
	    // ����FP-tree		
		double time1 = (double) System.currentTimeMillis(); 
		myFPtree.createFPtree();
		double time2 = (double) System.currentTimeMillis();
		// ���FP-tree
		myFPtree.outputFPtree();
		// ����ʱ��
		myFPtree.outputDuration(time1,time2);

		// �ھ�FP-tree 		
		time1 = (double) System.currentTimeMillis(); 
		myFPtree.startMining();
		time2 = (double) System.currentTimeMillis();
		// ��� Ttree
		// myFPtree.outputTtree();
		// ���Ƶ��ģʽ
		myFPtree.outputFrequentSets();		
		// �����������
		myFPtree.outputARs2();
		// myFPtree.outputARs();
		// ����ʱ��
		myFPtree.outputDuration(time1,time2);
		
		return;
	}

}
