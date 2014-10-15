import java.io.FileNotFoundException;
import java.util.LinkedList;

public class algtest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			DataSet dataset = new DataSet(
			"C:\\Users\\john\\Desktop\\nba\\nba");
			int aalg[] = new int[4];
			int choice = 1;
			int ratio = 2;
			if (choice = 3)
				dataset.setMovieWeight();
			System.out.println(Global_Variable.wsum);
			Global_Variable.w = 97180;
			Global_Variable.K = 16;
			Global_Variable.delta = 5;
			aalg[0] = 1;
			aalg[1] = 2;
			aalg[2] = 3;
			aalg[3] = 4;
			for (int i = 0; i < 4; i++) {
				Global_Variable.comparsionNo = 0;
				System.out
						.println("\rtype in 1:K-dominant 2:top-delta 3:W-dominant\rchoice is: "
								+ choice);
				// Scanner s = new Scanner(System.in);
				// int alg = s.nextInt();
				int alg = aalg[i];
				System.out.println("alg = " + alg);
				if (choice == 1) {
					System.out.println("k = " + Global_Variable.K);
				} else if (choice == 2) {
					System.out.println("delta = " + Global_Variable.delta);
				} else {
					System.out.println("w = " + Global_Variable.w);
					System.out.println("raito = " + ratio);
				}
				long startTime = System.currentTimeMillis(); // 获取开始时间
				LinkedList<DataItem> r;
				switch (alg) {
				case 1:
					System.out.println("alg1:Baseline");
					if (choice == 1)
						r = dataset.BaselineAlg();
					else if (choice == 2) {
						r = dataset.Baseline_TopDelta();
					} else {
						r = dataset.Baseline_Weighted();
					}
					break;
				case 2:
					System.out.println("alg2:SortRetrieval");
					if (choice == 1)
						r = dataset.SortRetrievalAlg();
					else if (choice == 2) {
						r = dataset.SortRetrival_TopDelta();
					} else {
						r = dataset.SortRetrival_Weighted();
					}
					break;
				case 3:
					System.out.println("alg3:DominantAbilityBased");
					if (choice == 1)
						r = dataset.DominantAbilityBasedAlg();
					else if (choice == 2) {
						r = dataset.DominantAbilityBased_TopDelta();
					} else {
						r = dataset.DominantAbilityBased_Weighted();
					}
					break;
				case 4:
					System.out.println("alg4:BitmapIndexBased");
					if (choice == 1)
						r = dataset.BitmapIndexBasedAlg();
					else if (choice == 2) {
						r = dataset.BitmapIndexBased_TopDelta();
					} else {
						r = dataset.BitmapIndexBased_Weighted();
					}
					break;
				default:
					System.out.println("oldalg:BitmapIndexBase");
					r = dataset._BitmapIndexBasedAlg();
				}
				long endTime = System.currentTimeMillis(); // 获取结束时间
				long totalTime = endTime - startTime;
				System.out.println("TotalTime:" + totalTime);
				System.out.println("comparsionNo:"
						+ Global_Variable.comparsionNo);
				System.out.println("size of result set:" + r.size());
//				 for (DataItem p : r) {
//				 System.out.println(p.index);
//				 }

			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
