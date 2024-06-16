package fpgrowth;

public class FPgrowth 
{
	/**
	 * 参数格式  -C=80.0  -S=20.0 -F=sample.txt
	 */
	public static void main(String[] args) 
	{
		// 根据命令行参数生成 FPtree 实例	
		FPtree myFPtree = new FPtree(args);	
		// 数据预处理
		myFPtree.preprocessDataSet();
		
		// 读取数据集到内存数组中
		myFPtree.inputDataSet();		
		// 	根据内存数组获取一项集
		myFPtree.getOrderedOneItem();
		// 重新处理内存数组中的数据，去掉频率小于最小支持数的元素
		myFPtree.pruneUnsupportElements(); 
		
	    // 生成FP-tree		
		double time1 = (double) System.currentTimeMillis(); 
		myFPtree.createFPtree();
		double time2 = (double) System.currentTimeMillis();
		// 输出FP-tree
		myFPtree.outputFPtree();
		// 所耗时间
		myFPtree.outputDuration(time1,time2);

		// 挖掘FP-tree 		
		time1 = (double) System.currentTimeMillis(); 
		myFPtree.startMining();
		time2 = (double) System.currentTimeMillis();
		// 输出 Ttree
		// myFPtree.outputTtree();
		// 输出频繁模式
		myFPtree.outputFrequentSets();		
		// 输出关联规则
		myFPtree.outputARs2();
		// myFPtree.outputARs();
		// 所耗时间
		myFPtree.outputDuration(time1,time2);
		
		return;
	}

}
