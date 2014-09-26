import it.uniroma3.mat.extendedset.intset.ConciseSet;
import it.uniroma3.mat.extendedset.intset.IntSet.IntIterator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

public class DataSet {
	public DataItem[] sets;
	public int totalRows = 0;
	public int totalColumns = 0;

	public DataSet(String dataSourcePath) throws FileNotFoundException {
		Global_Variable.DatasetName = dataSourcePath;
		File file = new File(dataSourcePath + ".info");
		if (!file.exists()) {
			System.out.println("不能找到指定文件，请检查目录设置是否正确!");
			return;
		}
		Scanner scanner = new Scanner(file);
		totalColumns = scanner.nextInt();
		Global_Variable.dimension = totalColumns;
		scanner.close();
		System.out.println("维度:" + totalColumns);

		File dataFile = new File(dataSourcePath + ".dat");
		File bitmapFile = new File(dataSourcePath + ".b");
		if (!dataFile.exists() || !bitmapFile.exists()) {
			System.out.println("不能找到指定文件，请检查目录设置是否正确!");
			return;
		}

		Scanner dataScanner = new Scanner(dataFile);
		Scanner bitmapScanner = new Scanner(bitmapFile);
		while (dataScanner.hasNextLine()) {
			totalRows++;
			dataScanner.nextLine();
		}
		Global_Variable.row = totalRows;
		dataScanner.close();
		dataScanner = new Scanner(dataFile);

		sets = new DataItem[totalRows];
		int i;
		for (i = 0; i < totalRows && dataScanner.hasNext(); i++) {
			// 初始化每一个数据点
			sets[i] = new DataItem();
			// 读取数据集合中的下一行
			String tmp = dataScanner.nextLine();
			if (tmp == null) {
				System.out.println("数据读取过程中意外终止:" + i + "th line");
				break;
			}
			String[] numStrings = tmp.split("\t");// --------------------------------dity
			// 接下来我们对每一维的数据进行整数转换
			for (int j = 0; j < totalColumns + 1; j++) {
				if (j == 0)
					sets[i].index = Integer.parseInt(numStrings[j]);
				else
					sets[i].data[j - 1] = Integer.parseInt(numStrings[j]);
			}

			// 接下来我们对每一维的bitmap进行转换
			tmp = bitmapScanner.nextLine();
			String bitstr = "";
			String[] bitmapStrings = tmp.split("\t");//

			for (int j = 0; j < totalColumns; j++) {
				int nTmp = Integer.parseInt(bitmapStrings[j]);
				bitstr += bitmapStrings[j];
				if (nTmp == 1) {
					sets[i].bitmap.add(j);
				} else {
					Global_Variable.incompleteNum++;
				}
			}
			sets[i].bitstr = bitstr;
		}
		System.out.println("at" + i);
		double rate = (double) Global_Variable.incompleteNum
				/ (double) (Global_Variable.row * Global_Variable.dimension);
		Global_Variable.incompleteNum = 0;
		System.out.println("missing rate:" + rate);
	}
	public void setMovieWeight(){
		Global_Variable.W = new int[totalColumns];
		File file = new File("C:\\Users\\john\\Desktop\\weight.txt");
		try {
			Scanner Scanner = new Scanner(file);
			String tmp = Scanner.nextLine();
			String[] weightStrings = tmp.split(" ");
			for (int i = 0; i < totalColumns; i++) {
				Global_Variable.W[i]= Integer.parseInt(weightStrings[i]);
				Global_Variable.wsum += Global_Variable.W[i];
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void setWeight(int ratio) {
		Global_Variable.W = new int[totalColumns];
		int x = ratio - 1;
		for (int i = 0; i < totalColumns; i++) {
			Global_Variable.W[i] = 1;
			if ((i + 1) % (2 * x) == 0)
				Global_Variable.W[i] += x;
			Global_Variable.wsum += Global_Variable.W[i];
		}
	}

	public LinkedList<DataItem> BaseBaseAlg() {
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			boolean iskdominant = true;
			if (p.bitmap.isEmpty())
				continue;
			for (int j = 0; j < totalRows; j++) {
				DataItem q = sets[j];
				if (q.IsKDominate(p)) {
					iskdominant = false;
					break;
				}
			}
			if (iskdominant)
				r.add(p);
		}
		return r;
	}

	public LinkedList<DataItem> _BitmapIndexBasedAlg() {
		long totaltime = 0;
		int edimnum[] = new int[totalRows];
		ArrayList<TreeMap<Integer, ConciseSet>> bitmaplist = new ArrayList<TreeMap<Integer, ConciseSet>>();
		for (int i = 0; i < totalColumns; i++) {
			TreeMap<Integer, ConciseSet> tm = new TreeMap<Integer, ConciseSet>();
			ConciseSet cs = new ConciseSet();
			cs.add(totalRows);
			tm.put(-1, cs);
			bitmaplist.add(tm);
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			p.edim = new ArrayList<Integer>();
			for (int j = 0; j < totalColumns; j++) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				if (p.bitmap.contains(j)) {
					p.edim.add(j);
					edimnum[i]++;
					ConciseSet bitset = bitsetmap.get(p.data[j]);
					if (bitset == null) {
						bitset = new ConciseSet();
						bitsetmap.put(p.data[j], bitset);
					}
				}
			}
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			for (Integer j : p.edim) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				Iterator<Entry<Integer, ConciseSet>> it = bitsetmap.entrySet()
						.iterator();
				while (true) {
					Map.Entry<Integer, ConciseSet> entry = (Map.Entry<Integer, ConciseSet>) it
							.next();
					Integer key = entry.getKey();
					ConciseSet set = entry.getValue();
					if (key == p.data[j])
						break;
					else {
						set.add(p.index);
					}
				}
			}
		}
		long startTime = System.currentTimeMillis();// ---------------------------
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			ConciseSet Q = new ConciseSet();
			for (Integer j : p.edim) {
				Q = Q.union(bitmaplist.get(j).get(p.data[j]));
			}
			for (Integer j : p.edim) {
				ConciseSet P_H = bitmaplist.get(j).get(-1).complemented();
				ConciseSet P = bitmaplist.get(j).lowerEntry(p.data[j])
						.getValue();
				P_H = P_H.union(P);
				// long startTime = System.currentTimeMillis();//
				// ---------------------------
				Q = Q.intersection(P_H);
				// long endTime = System.currentTimeMillis(); //
				// --------------------------
				// totaltime += endTime - startTime;
			}
			if (Q.isEmpty())
				r.add(p); // skyline point
		}

		// System.out.println("counttime:" + totaltime);
		int prune1 = 0, prune2 = 0, prune3 = 0;
		if (Global_Variable.K == totalColumns) {
			long endTime = System.currentTimeMillis(); // --------------------------
			totaltime = endTime - startTime;
			System.out.println("counttime(without preprocess):" + totaltime);
			return r;
		} else {
			int rsize = r.size();
			for (int i = 0; i < rsize; i++) {
				DataItem p = r.poll();
				int k = Global_Variable.K - (totalColumns - edimnum[p.index]);
				ConciseSet Q = new ConciseSet();
				for (Integer j : p.edim) {
					Q = Q.union(bitmaplist.get(j).get(p.data[j]));
				}
				if (Q.isEmpty()) {
					r.offer(p);
					prune1++;
					continue;
				} else if (k <= 1) {
					continue;
				} else {
					boolean isDominant = true;
					int count[] = new int[totalRows];
					int maxcount = 0, maxindex = 0;
					for (Integer j : p.edim) {
						ConciseSet P_H = new ConciseSet();
						ConciseSet P = bitmaplist.get(j).lowerEntry(p.data[j])
								.getValue();
						ConciseSet H = bitmaplist.get(j).get(-1);
						ConciseSet _H = new ConciseSet();
						_H = H.complemented();
						P_H = _H.union(P);
						ConciseSet X = new ConciseSet();
						X = Q.intersection(P_H);
						// int xsize = X.size();
						// int[] xarray = new int[xsize];
						IntIterator ite = X.iterator();
						while (ite.hasNext()) {
							int _i = ite.next();
							count[_i]++;
							if (count[_i] > maxcount) {
								maxcount = count[_i];
								maxindex = _i;
							}
						}
						if (maxcount == k) {
							isDominant = false;
							prune2++;
							break;
						} else if (sets[maxindex].IsKDominate(p)) {
							isDominant = false;
							prune3++;
							break;
						}
						// for (int ii = 0; ii < xsize; ii++) {
						// int _i = xarray[ii];
						// if (++count[_i] == k) {
						// isDominant = false;
						// break;
						// }
						// }
					}
					if (isDominant)
						r.offer(p);
				}
			}
			long endTime = System.currentTimeMillis(); // --------------------------
			totaltime = endTime - startTime;
			System.out.println("counttime(without preprocess):" + totaltime);
			System.out.println("prune1(k-j<=1): " + prune1);
			System.out.println("prune2(count=k-j): " + prune2);
			System.out.println("prune3(maxcount point k-dominate): " + prune3);
			return r;
		}

	}

	public LinkedList<DataItem> BitmapIndexBasedAlg() {
		long totaltime = 0;
		int edimnum[] = new int[totalRows];
		ArrayList<TreeMap<Integer, ConciseSet>> bitmaplist = new ArrayList<TreeMap<Integer, ConciseSet>>();
		for (int i = 0; i < totalColumns; i++) {
			TreeMap<Integer, ConciseSet> tm = new TreeMap<Integer, ConciseSet>();
			ConciseSet cs = new ConciseSet();
			cs.add(totalRows);
			tm.put(-1, cs);
			bitmaplist.add(tm);
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			p.edim = new ArrayList<Integer>();
			for (int j = 0; j < totalColumns; j++) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				if (p.bitmap.contains(j)) {
					p.edim.add(j);
					edimnum[i]++;
					ConciseSet bitset = bitsetmap.get(p.data[j]);
					if (bitset == null) {
						bitset = new ConciseSet();
						bitsetmap.put(p.data[j], bitset);
					}
				}
			}
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			for (Integer j : p.edim) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				Iterator<Entry<Integer, ConciseSet>> it = bitsetmap.entrySet()
						.iterator();
				while (true) {
					Map.Entry<Integer, ConciseSet> entry = (Map.Entry<Integer, ConciseSet>) it
							.next();
					Integer key = entry.getKey();
					ConciseSet set = entry.getValue();
					if (key == p.data[j])
						break;
					else {
						set.add(p.index);
					}
				}
			}
		}
		long startTime = System.currentTimeMillis();// ---------------------------
		int prune1 = 0, prune2 = 0, prune3 = 0;
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			int k = Global_Variable.K - (totalColumns - edimnum[i]);
			ConciseSet Q = new ConciseSet();
			for (Integer j : p.edim) {
				Q = Q.union(bitmaplist.get(j).get(p.data[j]));
			}
			if (Q.isEmpty()) {
				r.add(p);
				continue;
			} else if (k <= 1) {
				prune1++;
				continue;
			} else {
				boolean isDominant = true;
				int count[] = new int[totalRows];
				int maxcount = 0, maxindex = 0;
				for (Integer j : p.edim) {
					ConciseSet P_H = new ConciseSet();
					ConciseSet P = bitmaplist.get(j).lowerEntry(p.data[j])
							.getValue();
					ConciseSet H = bitmaplist.get(j).get(-1);
					ConciseSet _H = new ConciseSet();
					_H = H.complemented();
					P_H = _H.union(P);
					ConciseSet X = new ConciseSet();
					X = Q.intersection(P_H);
					// int xsize = X.size();
					// int[] xarray = new int[xsize];
					IntIterator ite = X.iterator();
					while (ite.hasNext()) {
						int _i = ite.next();
						count[_i]++;
//						if (count[_i] == k) {
//							prune2++;
//							isDominant = false;
//							break;
//						}
						 if (count[_i] > maxcount) {
						 maxcount = count[_i];
						 maxindex = _i;
						 }
					}
					if (maxcount == k) {
						prune2++;
						isDominant = false;
						break;
					} else if (sets[maxindex].IsKDominate(p)) {
						prune3++;
						isDominant = false;
						break;
					}
					// for (int ii = 0; ii < xsize; ii++) {
					// int _i = xarray[ii];
					// if (++count[_i] == k) {
					// isDominant = false;
					// break;
					// }
					// }
					// if (!isDominant)
					// break;
				}
				if (isDominant)
					r.add(p);
			}
		}
		long endTime = System.currentTimeMillis(); // ---------------------------
		totaltime = endTime - startTime;
		System.out.println("counttime:" + totaltime);
		System.out.println("counttime(without preprocess):" + totaltime);
		System.out.println("prune1(k-j<=1): " + prune1);
		System.out.println("prune2(count=k-j): " + prune2);
		System.out.println("prune3(maxcount point k-dominate): " + prune3);
		return r;
	}

	public LinkedList<DataItem> DominantAbilityBasedAlg() {
		class SortPair implements Comparable<SortPair> {
			public int pos;
			public double value;

			public SortPair(int _pos, double _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value > value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		int dim_count[] = new int[totalColumns];
		long dim_sum[] = new long[totalColumns];
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			p.edim = new ArrayList<Integer>();
			for (int j = 0; j < totalColumns; j++) {
				if (p.bitmap.contains(j)) {
					dim_count[j]++;
					dim_sum[j] += p.data[j];
					p.edim.add(j);
				}
			}
		}
		double dim_aver[] = new double[totalColumns];
		for (int i = 0; i < totalColumns; i++) {
			dim_aver[i] = (double) dim_sum[i] / (double) dim_count[i];
		}
		LinkedList<SortPair> d = new LinkedList<SortPair>();
		int emptypoint = 0;
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty()) {
				emptypoint++;
				continue;
			}
			double ability = 1;
			for (Integer j : p.edim) {
				ability *= (double) p.data[j] / (double) dim_sum[j];
			}
			SortPair sp = new SortPair(i, ability);
			d.add(sp);
		}
		Collections.sort(d);
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			if (!sets[i].bitmap.isEmpty()) {
				r.add(sets[i]);
			}
		}
		boolean issecond[] = new boolean[totalRows];
		int comppoint = 0;
		while (!d.isEmpty() && !r.isEmpty()) {
			SortPair sp = d.poll();
			DataItem p = sets[sp.pos];
			boolean isgreater = false;
			if (!issecond[sp.pos]) {
				for (Integer i : p.edim) {
					if (p.data[i] > dim_aver[i]) {
						isgreater = true;
						break;
					}
				}
				if (!isgreater) {
					issecond[sp.pos] = true;
					d.offer(sp);
					continue;
				}
			}
			comppoint++;
			int rsize = r.size();
			for (int i = 0; i < rsize; i++) {
				DataItem q = r.poll();
				if (!p.IsKDominate(q))
					r.offer(q);
			}
		}
		System.out.println("prune off point number:"
				+ (totalRows - comppoint - emptypoint));// 因结果集r为空而退出，使得排序后d中未参与K支配比较的点的个数
		return r;

	}

	public LinkedList<DataItem> SortRetrievalAlg() {

		class SortPair implements Comparable<SortPair> {
			public int pos;
			public int value;

			public SortPair(int _pos, int _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value > value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		ArrayList<ArrayList<SortPair>> existlist = new ArrayList<ArrayList<SortPair>>();
		ArrayList<ArrayList<Integer>> misslist = new ArrayList<ArrayList<Integer>>();
		int edimnum[] = new int[totalRows];
		int pruning = 0;
		for (int i = 0; i < totalColumns; i++) {
			existlist.add(new ArrayList<SortPair>());
			misslist.add(new ArrayList<Integer>());
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			for (int j = 0; j < totalColumns; j++) {
				if (p.bitmap.contains(j)) {
					SortPair sp = new SortPair(i, p.data[j]);
					existlist.get(j).add(sp);
					edimnum[i]++;
				} else {
					misslist.get(j).add(i);
				}
			}
		}
		for (int i = 0; i < totalColumns; i++) {
			Collections.sort(existlist.get(i));
		}
		int arraypos[] = new int[totalColumns];
		int count[] = new int[totalRows];
		boolean removed[] = new boolean[totalRows];
		int judge = totalColumns - Global_Variable.K + 1;
		LinkedList<DataItem> t = new LinkedList<DataItem>();
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			if (!sets[i].bitmap.isEmpty())
				t.add(sets[i]);
		}
		int minpos;
		int minarray;
		while (!t.isEmpty()) {
			minpos = arraypos[0];
			minarray = 0;
			// FindNextDimension
			for (int i = 1; i < totalColumns; i++) {
				if (arraypos[i] < minpos) {
					minarray = i;
					minpos = arraypos[i];
				}
			}
			// 最后一轮迭代之后T可能非空，此时直接跳出循环
			if (minpos == totalRows)
				break;
			ArrayList<Integer> m = new ArrayList<Integer>();
			ArrayList<SortPair> splist = existlist.get(minarray);
			SortPair sp = splist.get(minpos);
			m.add(sp.pos);
			int value = sp.value;
			SortPair nextsp = splist.get(++minpos);
			while (nextsp.value == value) {
				m.add(nextsp.pos);
				if (minpos < splist.size() - 1)
					nextsp = splist.get(++minpos);
				else
					break;
			}
			if (minpos != splist.size() - 1) {
				arraypos[minarray] = minpos;
			} else {
				// 对应数组中所有元素都已拿出过一次，则将下标置为最大
				arraypos[minarray] = totalRows;
			}
			for (Integer i : m) {
				DataItem p = sets[i];
				if (count[i] == 0) {
					int tsize = t.size();
					for (int j = 0; j < tsize; j++) {
						DataItem q = t.poll();
						// 在将被pk支配的点从T中移除的同时，将上一轮中发现被支配的以及移入R中的点p也从T中移除
						if ((!removed[q.index]) && (!p.IsKDominate(q))) {
							t.offer(q);
						} else {
							removed[q.index] = true;
						}
					}
				}
				count[i]++;
				if (count[i] == 1)
					pruning++;
				if (!removed[i]) {
					for (Integer j : misslist.get(minarray)) {
						if (count[j] == 0) {
							DataItem q = sets[j];
							if (q.IsKDominate(p)) {
								removed[p.index] = true;
							}
						}
					}
				}
			}
			for (Integer i : m) {
				if ((!removed[i])
						&& ((count[i] == judge) || count[i] == edimnum[i])) {
					removed[i] = true;
					r.add(sets[i]);
				}
			}
		}
		System.out.println("prune off point number:" + (totalRows - pruning));// 不曾取出与T中余下点进行k支配比较的点
		return r;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LinkedList<DataItem> BaselineAlg() {
		HashMap<String, LinkedList<DataItem>> map = new HashMap<String, LinkedList<DataItem>>();
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			boolean isUniqueSkyline = true;
			boolean isLocalDominant = true;
			DataItem p = sets[i];
			if (p.bitmap.isEmpty()) {
				continue;
			}
			LinkedList<DataItem> t = map.get(p.bitstr);
			if (t == null) {
				t = new LinkedList<DataItem>();
				map.put(p.bitstr, t);
			}
			int tsize = t.size();
			for (int j = 0; j < tsize; j++) {
				DataItem q = t.poll();
				if (!q.bitstr.equals(p.bitstr) || !p.IsDominate(q)) { // 是虚拟点或者不被支配
					t.offer(q);
					if (!q.bitstr.equals(p.bitstr) && q.IsKDominate(p))
						isUniqueSkyline = false;
				} else if (q.bitstr.equals(p.bitstr) && q.IsDominate(p)) { // 不是虚拟点同时支配
					isUniqueSkyline = false;
					isLocalDominant = false;
					break;
				}
			}
			if (isUniqueSkyline) {
				boolean isDominant = true;
				int rsize = r.size();
				for (int j = 0; j < rsize; j++) {
					DataItem q = r.poll();
					if (q.IsKDominate(p)) {
						isDominant = false;
						if (q.bitstr.equals(p.bitstr) && q.IsDominate(p))
							isLocalDominant = false;
						if (!q.bitstr.equals(p.bitstr))
							t.offer(q);
					}
					if (!p.IsKDominate(q))
						r.offer(q);
					else {
						LinkedList<DataItem> qt = map.get(q.bitstr);
						qt.offer(q);
						qt.offer(p);
					}
				}
				if (isDominant)
					r.offer(p);
			}
			if (isLocalDominant)
				t.offer(p);
		}
		int rsize = r.size();
		for (int i = 0; i < rsize; i++) {
			DataItem p = r.poll();
			boolean iskdominant = true;
			Iterator mapiter = map.entrySet().iterator();
			while (mapiter.hasNext()) {
				Map.Entry<String, LinkedList<DataItem>> entry = (Map.Entry<String, LinkedList<DataItem>>) mapiter
						.next();
				String bitstr = entry.getKey();
				LinkedList<DataItem> t = entry.getValue();
				if (!p.bitstr.equals(bitstr)) {
					Iterator<DataItem> listiter = t.listIterator();
					while (listiter.hasNext()) {
						DataItem q = listiter.next();
						if (q.bitstr.equals(bitstr) && q.IsKDominate(p)) {
							iskdominant = false;
							break;
						}
					}
				}
				if (!iskdominant)
					break;
			}
			if (iskdominant)
				r.offer(p);
		}
		return r;
	}

	public LinkedList<DataItem> _BitmapIndexBased_TopDelta() {
		long totaltime = 0;
		int edimnum[] = new int[totalRows];
		ArrayList<TreeMap<Integer, ConciseSet>> bitmaplist = new ArrayList<TreeMap<Integer, ConciseSet>>();
		for (int i = 0; i < totalColumns; i++) {
			TreeMap<Integer, ConciseSet> tm = new TreeMap<Integer, ConciseSet>();
			ConciseSet cs = new ConciseSet();
			cs.add(totalRows);
			tm.put(-1, cs);
			bitmaplist.add(tm);
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			p.edim = new ArrayList<Integer>();
			for (int j = 0; j < totalColumns; j++) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				if (p.bitmap.contains(j)) {
					p.edim.add(j);
					edimnum[i]++;
					ConciseSet bitset = bitsetmap.get(p.data[j]);
					if (bitset == null) {
						bitset = new ConciseSet();
						bitsetmap.put(p.data[j], bitset);
					}
				}
			}
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			for (Integer j : p.edim) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				Iterator<Entry<Integer, ConciseSet>> it = bitsetmap.entrySet()
						.iterator();
				while (true) {
					Map.Entry<Integer, ConciseSet> entry = (Map.Entry<Integer, ConciseSet>) it
							.next();
					Integer key = entry.getKey();
					ConciseSet set = entry.getValue();
					if (key == p.data[j])
						break;
					else {
						set.add(p.index);
					}
				}
			}
		}
		long startTime = System.currentTimeMillis();// ---------------------------
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			ConciseSet Q = new ConciseSet();
			for (Integer j : p.edim) {
				Q = Q.union(bitmaplist.get(j).get(p.data[j]));
			}
			for (Integer j : p.edim) {
				ConciseSet P_H = bitmaplist.get(j).get(-1).complemented();
				ConciseSet P = bitmaplist.get(j).lowerEntry(p.data[j])
						.getValue();
				P_H = P_H.union(P);
				// long startTime = System.currentTimeMillis();//
				// ---------------------------
				Q = Q.intersection(P_H);
				// long endTime = System.currentTimeMillis(); //
				// --------------------------
				// totaltime += endTime - startTime;
			}
			if (Q.isEmpty())
				r.add(p); // skyline point
		}

		// System.out.println("counttime:" + totaltime);
		class SortPair implements Comparable<SortPair> {
			public int pos;
			public int value;

			public SortPair(int _pos, int _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value < value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		int rsize = r.size();
		ArrayList<SortPair> splist = new ArrayList<SortPair>();
		for (int i = 0; i < rsize; i++) {
			DataItem p = r.poll();
			// int k = Global_Variable.K - (totalColumns - edimnum[p.index]);
			int missdimnum = totalColumns - edimnum[p.index];
			ConciseSet Q = new ConciseSet();
			for (Integer j : p.edim) {
				Q = Q.union(bitmaplist.get(j).get(p.data[j]));
			}
			if (Q.isEmpty()) {
				SortPair sp = new SortPair(p.index, 0);
				splist.add(sp);
				// r.offer(p);
				continue;
			} else {
				// boolean isDominant = true;
				int count[] = new int[totalRows];
				int maxcount = 0;
				for (Integer j : p.edim) {
					ConciseSet P_H = new ConciseSet();
					ConciseSet P = bitmaplist.get(j).lowerEntry(p.data[j])
							.getValue();
					ConciseSet H = bitmaplist.get(j).get(-1);
					ConciseSet _H = new ConciseSet();
					_H = H.complemented();
					P_H = _H.union(P);
					ConciseSet X = new ConciseSet();
					X = Q.intersection(P_H);
					// int xsize = X.size();
					// int[] xarray = new int[xsize];
					IntIterator ite = X.iterator();
					while (ite.hasNext()) {
						int _i = ite.next();
						count[_i]++;
						if (count[_i] > maxcount) {
							maxcount = count[_i];
						}
					}
				}
				int maxkdom = maxcount + missdimnum;
				SortPair sp = new SortPair(p.index, maxkdom);
				splist.add(sp);
			}
		}
		Collections.sort(splist);
		int spsize = splist.size();
		for (int i = 0; i < Global_Variable.delta && i < spsize; i++) {
			r.add(sets[splist.get(i).pos]);
		}
		long endTime = System.currentTimeMillis(); // --------------------------
		totaltime = endTime - startTime;
		System.out.println("counttime:" + totaltime);
		return r;

	}

	public LinkedList<DataItem> BitmapIndexBased_TopDelta() {
		long totaltime = 0;
		int edimnum[] = new int[totalRows];
		ArrayList<TreeMap<Integer, ConciseSet>> bitmaplist = new ArrayList<TreeMap<Integer, ConciseSet>>();
		for (int i = 0; i < totalColumns; i++) {
			TreeMap<Integer, ConciseSet> tm = new TreeMap<Integer, ConciseSet>();
			ConciseSet cs = new ConciseSet();
			cs.add(totalRows);
			tm.put(-1, cs);
			bitmaplist.add(tm);
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			p.edim = new ArrayList<Integer>();
			for (int j = 0; j < totalColumns; j++) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				if (p.bitmap.contains(j)) {
					p.edim.add(j);
					edimnum[i]++;
					ConciseSet bitset = bitsetmap.get(p.data[j]);
					if (bitset == null) {
						bitset = new ConciseSet();
						bitsetmap.put(p.data[j], bitset);
					}
				}
			}
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			for (Integer j : p.edim) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				Iterator<Entry<Integer, ConciseSet>> it = bitsetmap.entrySet()
						.iterator();
				while (true) {
					Map.Entry<Integer, ConciseSet> entry = (Map.Entry<Integer, ConciseSet>) it
							.next();
					Integer key = entry.getKey();
					ConciseSet set = entry.getValue();
					if (key == p.data[j])
						break;
					else {
						set.add(p.index);
					}
				}
			}
		}
		long startTime = System.currentTimeMillis();// ---------------------------
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		// for (int i = 0; i < totalRows; i++) {
		// DataItem p = sets[i];
		// if (p.bitmap.isEmpty())
		// continue;
		// ConciseSet Q = new ConciseSet();
		// for (Integer j : p.edim) {
		// Q = Q.union(bitmaplist.get(j).get(p.data[j]));
		// }
		// for (Integer j : p.edim) {
		// ConciseSet P_H = bitmaplist.get(j).get(-1).complemented();
		// ConciseSet P = bitmaplist.get(j).lowerEntry(p.data[j])
		// .getValue();
		// P_H = P_H.union(P);
		// // long startTime = System.currentTimeMillis();//
		// // ---------------------------
		// Q = Q.intersection(P_H);
		// // long endTime = System.currentTimeMillis(); //
		// // --------------------------
		// // totaltime += endTime - startTime;
		// }
		// if (Q.isEmpty())
		// r.add(p); // skyline point
		// }

		// System.out.println("counttime:" + totaltime);
		class SortPair implements Comparable<SortPair> {
			public int pos;
			public int value;

			public SortPair(int _pos, int _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value < value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		// int rsize = r.size();
		ArrayList<SortPair> splist = new ArrayList<SortPair>();
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			// int k = Global_Variable.K - (totalColumns - edimnum[p.index]);
			int missdimnum = totalColumns - edimnum[p.index];
			ConciseSet Q = new ConciseSet();
			for (Integer j : p.edim) {
				Q = Q.union(bitmaplist.get(j).get(p.data[j]));
			}
			if (Q.isEmpty()) {
				SortPair sp = new SortPair(p.index, 0);
				splist.add(sp);
				// r.offer(p);
				continue;
			} else {
				// boolean isDominant = true;
				int count[] = new int[totalRows];
				int maxcount = 0;
				for (Integer j : p.edim) {
					ConciseSet P_H = new ConciseSet();
					ConciseSet P = bitmaplist.get(j).lowerEntry(p.data[j])
							.getValue();
					ConciseSet H = bitmaplist.get(j).get(-1);
					ConciseSet _H = new ConciseSet();
					_H = H.complemented();
					P_H = _H.union(P);
					ConciseSet X = new ConciseSet();
					X = Q.intersection(P_H);
					// int xsize = X.size();
					// int[] xarray = new int[xsize];
					IntIterator ite = X.iterator();
					while (ite.hasNext()) {
						int _i = ite.next();
						count[_i]++;
						if (count[_i] > maxcount) {
							maxcount = count[_i];
						}
					}
				}
				int maxkdom = maxcount + missdimnum;
				if (maxkdom < totalColumns) {
					SortPair sp = new SortPair(p.index, maxkdom);
					splist.add(sp);
				}
			}
		}
		Collections.sort(splist);
		int spsize = splist.size();
		for (int i = 0; i < Global_Variable.delta && i < spsize; i++) {
			r.add(sets[splist.get(i).pos]);
		}
		long endTime = System.currentTimeMillis(); // --------------------------
		totaltime = endTime - startTime;
		System.out.println("counttime:" + totaltime);
		return r;

	}

	public LinkedList<DataItem> DominantAbilityBased_TopDelta() {
		class SortPair implements Comparable<SortPair> {
			public int pos;
			public double value;

			public SortPair(int _pos, double _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value > value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		int dim_count[] = new int[totalColumns];
		long dim_sum[] = new long[totalColumns];
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			p.edim = new ArrayList<Integer>();
			for (int j = 0; j < totalColumns; j++) {
				if (p.bitmap.contains(j)) {
					dim_count[j]++;
					dim_sum[j] += p.data[j];
					p.edim.add(j);
				}
			}
		}
		double dim_aver[] = new double[totalColumns];
		for (int i = 0; i < totalColumns; i++) {
			dim_aver[i] = (double) dim_sum[i] / (double) dim_count[i];
		}
		LinkedList<SortPair> d = new LinkedList<SortPair>();
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			double ability = 1;
			for (Integer j : p.edim) {
				ability *= (double) p.data[j] / (double) dim_sum[j];
			}
			SortPair sp = new SortPair(i, ability);
			d.add(sp);
		}
		Collections.sort(d);
		int dsize = d.size();
		LinkedList<SortPair> lowd = new LinkedList<SortPair>();
		for (int i = 0; i < dsize; i++) {
			SortPair sp = d.poll();
			DataItem p = sets[sp.pos];
			boolean isgreater = false;
			for (Integer j : p.edim) {
				if (p.data[j] > dim_aver[j]) {
					isgreater = true;
					break;
				}
			}
			if (isgreater)
				d.offer(sp);
			else
				lowd.add(sp);
		}
		d.addAll(lowd);
		LinkedList<DataItem> R = new LinkedList<DataItem>();
		int Kmin = 1;
		int Kmax = totalColumns;
		ArrayList<Integer> prunelist = new ArrayList<Integer>();
		do {
			Global_Variable.K = (Kmin + Kmax) / 2;
			System.out.println("K:" + Global_Variable.K);
			LinkedList<DataItem> r = new LinkedList<DataItem>();
			int size = 0;
			for (int i = 0; i < totalRows; i++) {
				if (!sets[i].bitmap.isEmpty()) {
					r.add(sets[i]);
					size++;
				}
			}
			int i;
			for (i = 0; i < dsize; i++) {
				SortPair sp = d.get(i);
				DataItem p = sets[sp.pos];
				int tempsize = size;
				for (int j = 0; j < tempsize; j++) {
					DataItem q = r.poll();
					if (!p.IsKDominate(q))
						r.offer(q);
					else
						size--;
				}
				if (r.isEmpty()) {
					break;
				}
			}
			prunelist.add(totalRows - i);
			if (size == Global_Variable.delta) {
				R = r;
				Kmin = Kmax + 1;
			} else if (size > Global_Variable.delta) {
				R = r;
				Kmax = Global_Variable.K - 1;
			} else {
				Kmin = Global_Variable.K + 1;
			}
		} while (Kmin <= Kmax);
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		int co = Global_Variable.delta;
		while (co-- > 0) {
			DataItem p = R.poll();
			r.add(p);
		}
		for (Integer prune : prunelist) {
			System.out.println("prune off point number:" + prune);// 因结果集r为空而退出，使得排序后d中未参与K支配比较的点的个数
		}
		return r;
	}

	public LinkedList<DataItem> SortRetrival_TopDelta() {
		class SortPair implements Comparable<SortPair> {
			public int pos;
			public int value;

			public SortPair(int _pos, int _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value > value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		ArrayList<ArrayList<SortPair>> existlist = new ArrayList<ArrayList<SortPair>>();
		// ArrayList<ArrayList<Integer>> misslist = new
		// ArrayList<ArrayList<Integer>>();
		int edimnum[] = new int[totalRows];
		for (int i = 0; i < totalColumns; i++) {
			existlist.add(new ArrayList<SortPair>());
			// misslist.add(new ArrayList<Integer>());
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			for (int j = 0; j < totalColumns; j++) {
				if (p.bitmap.contains(j)) {
					SortPair sp = new SortPair(i, p.data[j]);
					existlist.get(j).add(sp);
					edimnum[i]++;
				} else {
					// misslist.get(j).add(i);
				}
			}
		}
		for (int i = 0; i < totalColumns; i++) {
			Collections.sort(existlist.get(i));
		}
		int arraypos[] = new int[totalColumns];
		int count[] = new int[totalRows];
		boolean removed[] = new boolean[totalRows];
		int[] maxkdom = new int[totalRows];
		int[] numsum = new int[totalColumns + 1];
		numsum[0] = totalRows;// maxkdom=0的个数
		for (int j = 0; j < totalRows; j++) {
			DataItem q = sets[j];
			if (q.bitmap.isEmpty()) {
				numsum[0]--;
			}
		}
		// int judge = totalColumns - Global_Variable.K + 1;
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		int minpos;
		int minarray;
		int deltanum = 0;
		int joincount = 0;
		while (deltanum != Global_Variable.delta) {
			minpos = arraypos[0];
			minarray = 0;
			// FindNextDimension
			for (int i = 1; i < totalColumns; i++) {
				if (arraypos[i] < minpos) {
					minarray = i;
					minpos = arraypos[i];
				}
			}
			// 最后一轮迭代之后T可能非空，此时直接跳出循环
			if (minpos == totalRows)
				break;
			ArrayList<Integer> m = new ArrayList<Integer>();
			ArrayList<SortPair> splist = existlist.get(minarray);
			SortPair sp = splist.get(minpos);
			m.add(sp.pos);
			int value = sp.value;
			SortPair nextsp = splist.get(++minpos);
			while (nextsp.value == value) {
				m.add(nextsp.pos);
				if (minpos < splist.size() - 1)
					nextsp = splist.get(++minpos);
				else
					break;
			}
			if (minpos != splist.size() - 1) {
				arraypos[minarray] = minpos;
			} else {
				// 对应数组中所有元素都已拿出过一次，则将下标置为最大
				arraypos[minarray] = totalRows;
			}
			ArrayList<Integer> Realmaxkdomsort = new ArrayList<Integer>();
			// ArrayList<Integer> maxkdomsort = new ArrayList<Integer>();

			for (Integer i : m) {
				DataItem p = sets[i];
				if (count[i] == 0) {
					joincount++;
					for (int j = 0; j < totalRows; j++) {
						DataItem q = sets[j];
						if (q.bitmap.isEmpty()) {
							continue;
						}
						if (count[q.index] == 0) {
							if (!removed[p.index]) {
								int qdomp = q.maxDom(p);
								if (qdomp > maxkdom[p.index]) {
									numsum[maxkdom[p.index]]--;
									numsum[qdomp]++;
									maxkdom[p.index] = qdomp;
								}
							}
							int pdomq = p.maxDom(q);
							if (pdomq > maxkdom[q.index]) {
								numsum[maxkdom[q.index]]--;
								numsum[pdomq]++;
								maxkdom[q.index] = pdomq;
							}
							if (Realmaxkdomsort.size() >= Global_Variable.delta) {
								if (maxkdom[q.index] > Realmaxkdomsort
										.get(Global_Variable.delta - 1))
									removed[q.index] = true;
							}
						} else if (!removed[q.index]) {
							int sum = 0;
							int domi;
							for (domi = 0; domi < totalColumns + 1; domi++) {
								sum += numsum[domi];
								if (sum >= Global_Variable.delta)
									break;
							}
							if (maxkdom[q.index] <= domi) {
								r.add(q);
								removed[q.index] = true;
								deltanum++;
								if (deltanum == Global_Variable.delta)
									break;
							}
						}
					}
					count[i] = 1;
					Realmaxkdomsort.add(maxkdom[p.index]);
					Collections.sort(Realmaxkdomsort);
				} else if (!removed[p.index]) {
					int sum = 0;
					int domi;
					for (domi = 0; domi < totalColumns + 1; domi++) {
						sum += numsum[domi];
						if (sum >= Global_Variable.delta)
							break;
					}
					if (maxkdom[p.index] <= domi) {
						r.add(p);
						removed[p.index] = true;
						deltanum++;
						if (deltanum == Global_Variable.delta)
							break;
					}
				}
			}
		}
		System.out.println("prune off point number:" + (totalRows - joincount));// 不曾取出与T中余下点进行k支配比较的点
		return r;
	}

	public LinkedList<DataItem> Baseline_TopDelta() {
		LinkedList<DataItem> tempR = new LinkedList<DataItem>();
		int[] maxkdom = new int[totalRows];
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty()) {
				maxkdom[p.index] = totalColumns;
				continue;
			}
			boolean needinsert = true;
			int Rsize = tempR.size();
			for (int j = 0; j < Rsize; j++) {
				DataItem q = tempR.poll();
				int qdomp = q.maxDom(p);
				if (qdomp > maxkdom[p.index])
					maxkdom[p.index] = qdomp;
				int pdomq = p.maxDom(q);
				if (pdomq > maxkdom[q.index])
					maxkdom[q.index] = pdomq;
				if (pdomq == totalColumns) {
					if (!p.bitmap.containsAll(q.bitmap)) {
						tempR.offer(q);
					}
				} else
					tempR.offer(q);
				if (qdomp == totalColumns) {
					if (q.bitmap.containsAll(p.bitmap)) {
						needinsert = false;
						break;
					}
				}
			}
			if (needinsert)
				tempR.offer(p);
		}
		class SortPair implements Comparable<SortPair> {
			public int pos;
			public int value;

			public SortPair(int _pos, int _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value < value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		ArrayList<SortPair> splist = new ArrayList<SortPair>();
		for (int i = 0; i < totalRows; i++) {
			if (maxkdom[i] < totalColumns) {
				SortPair sp = new SortPair(i, maxkdom[i]);
				splist.add(sp);
			}
		}
		Collections.sort(splist);
		int spsize = splist.size();
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < Global_Variable.delta && i < spsize; i++) {
			r.add(sets[splist.get(i).pos]);
		}
		return r;
	}

	public LinkedList<DataItem> BitmapIndexBased_Weighted() {
		long totaltime = 0;
		int edimnum[] = new int[totalRows];
		ArrayList<TreeMap<Integer, ConciseSet>> bitmaplist = new ArrayList<TreeMap<Integer, ConciseSet>>();
		for (int i = 0; i < totalColumns; i++) {
			TreeMap<Integer, ConciseSet> tm = new TreeMap<Integer, ConciseSet>();
			ConciseSet cs = new ConciseSet();
			cs.add(totalRows);
			tm.put(-1, cs);
			bitmaplist.add(tm);
		}
		int missweight[] = new int[totalRows];
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			p.edim = new ArrayList<Integer>();
			for (int j = 0; j < totalColumns; j++) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				if (p.bitmap.contains(j)) {
					p.edim.add(j);
					edimnum[i]++;
					ConciseSet bitset = bitsetmap.get(p.data[j]);
					if (bitset == null) {
						bitset = new ConciseSet();
						bitsetmap.put(p.data[j], bitset);
					}
				} else {
					missweight[i] += Global_Variable.W[j]; // 统计每个点在缺失维度上的权重总和
				}
			}
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			for (Integer j : p.edim) {
				TreeMap<Integer, ConciseSet> bitsetmap = bitmaplist.get(j);
				Iterator<Entry<Integer, ConciseSet>> it = bitsetmap.entrySet()
						.iterator();
				while (true) {
					Map.Entry<Integer, ConciseSet> entry = (Map.Entry<Integer, ConciseSet>) it
							.next();
					Integer key = entry.getKey();
					ConciseSet set = entry.getValue();
					if (key == p.data[j])
						break;
					else {
						set.add(p.index);
					}
				}
			}
		}
		long startTime = System.currentTimeMillis();// ---------------------------
		int prune1 = 0, prune2 = 0, prune3 = 0;
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty())
				continue;
			// int k = Global_Variable.K - (totalColumns - edimnum[i]);
			ConciseSet Q = new ConciseSet();
			for (Integer j : p.edim) {
				Q = Q.union(bitmaplist.get(j).get(p.data[j]));
			}
			if (Q.isEmpty()) {
				r.add(p);
				continue;
			} else if (Global_Variable.w - missweight[i] <= 1) { // 这意味着再加上1个维度上的权重就等于w，被w支配,对应k-j<=1
				prune1++;
				continue;
			} else {
				boolean isDominant = true;
				int weight[] = new int[totalRows];
				int maxweight = 0, maxindex = 0;
				for (Integer j : p.edim) {
					ConciseSet P_H = new ConciseSet();
					ConciseSet P = bitmaplist.get(j).lowerEntry(p.data[j])
							.getValue();
					ConciseSet H = bitmaplist.get(j).get(-1);
					ConciseSet _H = new ConciseSet();
					_H = H.complemented();
					P_H = _H.union(P);
					ConciseSet X = new ConciseSet();
					X = Q.intersection(P_H);
					// int xsize = X.size();
					// int[] xarray = new int[xsize];
					IntIterator ite = X.iterator();
					while (ite.hasNext()) {
						int _i = ite.next();
						weight[_i] += Global_Variable.W[j];
						if (weight[_i] > maxweight) {
							maxweight = weight[_i];
							maxindex = _i;
						}
					}
					if (maxweight == Global_Variable.w - missweight[i]) { // 对应count==k-j
						prune2++;
						isDominant = false;
						break;
					} else if (sets[maxindex].IsWDominate(p)) {
						prune3++;
						isDominant = false;
						break;
					}
					// for (int ii = 0; ii < xsize; ii++) {
					// int _i = xarray[ii];
					// if (++count[_i] == k) {
					// isDominant = false;
					// break;
					// }
					// }
					// if (!isDominant)
					// break;
				}
				if (isDominant)
					r.add(p);
			}
		}
		long endTime = System.currentTimeMillis(); // ---------------------------
		totaltime = endTime - startTime;
		System.out.println("counttime:" + totaltime);
		System.out.println("counttime(without preprocess):" + totaltime);
		System.out.println("prune1(k-j<=1): " + prune1);
		System.out.println("prune2(count=k-j): " + prune2);
		System.out.println("prune3(maxcount point k-dominate): " + prune3);
		return r;
	}

	public LinkedList<DataItem> DominantAbilityBased_Weighted() {
		class SortPair implements Comparable<SortPair> {
			public int pos;
			public double value;

			public SortPair(int _pos, double _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value > value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		int dim_count[] = new int[totalColumns];
		long dim_sum[] = new long[totalColumns];
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			p.edim = new ArrayList<Integer>();
			for (int j = 0; j < totalColumns; j++) {
				if (p.bitmap.contains(j)) {
					dim_count[j]++;
					dim_sum[j] += p.data[j];
					p.edim.add(j);
				}
			}
		}
		double dim_aver[] = new double[totalColumns];
		for (int i = 0; i < totalColumns; i++) {
			dim_aver[i] = (double) dim_sum[i] / (double) dim_count[i];
		}
		LinkedList<SortPair> d = new LinkedList<SortPair>();
		int emptypoint = 0;
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			if (p.bitmap.isEmpty()) {
				emptypoint++;
				continue;
			}
			double ability = 1;
			for (Integer j : p.edim) {
				// 不能直接乘权重，因为权重是个整数，直接乘使得在很多维度上值缺失的点的支配能力相对下降
				ability *= (double) p.data[j] / (double) dim_sum[j]
						* (Global_Variable.W[j] / Global_Variable.wsum);
			}
			SortPair sp = new SortPair(i, ability);
			d.add(sp);
		}
		Collections.sort(d);
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			if (!sets[i].bitmap.isEmpty()) {
				r.add(sets[i]);
			}
		}
		boolean issecond[] = new boolean[totalRows];
		int comppoint = 0;
		while (!d.isEmpty() && !r.isEmpty()) {
			SortPair sp = d.poll();
			DataItem p = sets[sp.pos];
			boolean isgreater = false;
			if (!issecond[sp.pos]) {
				for (Integer i : p.edim) {
					if (p.data[i] > dim_aver[i]) {
						isgreater = true;
						break;
					}
				}
				if (!isgreater) {
					issecond[sp.pos] = true;
					d.offer(sp);
					continue;
				}
			}
			comppoint++;
			int rsize = r.size();
			for (int i = 0; i < rsize; i++) {
				DataItem q = r.poll();
				if (!p.IsWDominate(q))
					r.offer(q);
			}
		}
		System.out.println("prune off point number:"
				+ (totalRows - comppoint - emptypoint));// 因结果集r为空而退出，使得排序后d中未参与W支配比较的点的个数
		return r;

	}

	public LinkedList<DataItem> SortRetrival_Weighted() {

		class SortPair implements Comparable<SortPair> {
			public int pos;
			public int value;

			public SortPair(int _pos, int _value) {
				pos = _pos;
				value = _value;
			}

			public int compareTo(SortPair o) {
				if (o.value > value)
					return 1;
				else if (o.value == value)
					return 0;
				else
					return -1;
			}
		}
		ArrayList<ArrayList<SortPair>> existlist = new ArrayList<ArrayList<SortPair>>();
		ArrayList<ArrayList<Integer>> misslist = new ArrayList<ArrayList<Integer>>();
		int edimnum[] = new int[totalRows];
		int pruning = 0;
		for (int i = 0; i < totalColumns; i++) {
			existlist.add(new ArrayList<SortPair>());
			misslist.add(new ArrayList<Integer>());
		}
		for (int i = 0; i < totalRows; i++) {
			DataItem p = sets[i];
			for (int j = 0; j < totalColumns; j++) {
				if (p.bitmap.contains(j)) {
					SortPair sp = new SortPair(i, p.data[j]);
					existlist.get(j).add(sp);
					edimnum[i]++;
				} else {
					misslist.get(j).add(i);
				}
			}
		}
		for (int i = 0; i < totalColumns; i++) {
			Collections.sort(existlist.get(i));
		}
		int arraypos[] = new int[totalColumns];
		int weight[] = new int[totalRows];
		int count[] = new int[totalRows];
		boolean removed[] = new boolean[totalRows];
		// int judge = totalColumns - Global_Variable.K + 1;
		LinkedList<DataItem> t = new LinkedList<DataItem>();
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			if (!sets[i].bitmap.isEmpty())
				t.add(sets[i]);
		}
		int minpos;
		int minarray;
		while (!t.isEmpty()) {
			minpos = arraypos[0]; // 对应每个维度数组的下标
			minarray = 0; // 对应维度
			// FindNextDimension
			for (int i = 1; i < totalColumns; i++) {
				if (arraypos[i] < minpos) {
					minarray = i;
					minpos = arraypos[i];
				}
			}
			// 最后一轮迭代之后T可能非空，此时直接跳出循环
			if (minpos == totalRows)
				break;
			ArrayList<Integer> m = new ArrayList<Integer>();
			ArrayList<SortPair> splist = existlist.get(minarray);
			SortPair sp = splist.get(minpos);
			m.add(sp.pos);
			int value = sp.value;
			SortPair nextsp = splist.get(++minpos);
			while (nextsp.value == value) {
				m.add(nextsp.pos);
				if (minpos < splist.size() - 1)
					nextsp = splist.get(++minpos);
				else
					break;
			}
			if (minpos != splist.size() - 1) {
				arraypos[minarray] = minpos;
			} else {
				// 对应数组中所有元素都已拿出过一次，则将下标置为最大
				arraypos[minarray] = totalRows;
			}
			for (Integer i : m) {
				DataItem p = sets[i];
				if (weight[i] == 0) {
					int tsize = t.size();
					for (int j = 0; j < tsize; j++) {
						DataItem q = t.poll();
						// 在将被pk支配的点从T中移除的同时，将上一轮中发现被支配的以及移入R中的点p也从T中移除
						if ((!removed[q.index]) && (!p.IsWDominate(q))) {
							t.offer(q);
						} else {
							removed[q.index] = true;
						}
					}
				}
				count[i]++;
				if (count[i] == 1)
					pruning++;
				weight[i] += Global_Variable.W[minarray];
				if (!removed[i]) {
					for (Integer j : misslist.get(minarray)) {
						if (weight[j] == 0) {
							DataItem q = sets[j];
							if (q.IsWDominate(p)) {
								removed[p.index] = true;
							}
						}
					}
				}
			}
			for (Integer i : m) {
				// Global_Variable.wsum-weight[i]<Global_Variable.w
				// 对应d-(d-k+1)=k-1<k
				if ((!removed[i])
						&& ((Global_Variable.wsum - weight[i] < Global_Variable.w) || count[i] == edimnum[i])) {
					removed[i] = true;
					r.add(sets[i]);
				}
			}
		}
		System.out.println("prune off point number:" + (totalRows - pruning));// 不曾取出与T中余下点进行W支配比较的点
		return r;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LinkedList<DataItem> Baseline_Weighted() {
		HashMap<String, LinkedList<DataItem>> map = new HashMap<String, LinkedList<DataItem>>();
		LinkedList<DataItem> r = new LinkedList<DataItem>();
		for (int i = 0; i < totalRows; i++) {
			boolean isUniqueSkyline = true;
			boolean isLocalDominant = true;
			DataItem p = sets[i];
			if (p.bitmap.isEmpty()) {
				continue;
			}
			LinkedList<DataItem> t = map.get(p.bitstr);
			if (t == null) {
				t = new LinkedList<DataItem>();
				map.put(p.bitstr, t);
			}
			int tsize = t.size();
			for (int j = 0; j < tsize; j++) {
				DataItem q = t.poll();
				if (!q.bitstr.equals(p.bitstr) || !p.IsDominate(q)) { // 是虚拟点或者不被支配
					t.offer(q);
					if (!q.bitstr.equals(p.bitstr) && q.IsWDominate(p))
						isUniqueSkyline = false;
				} else if (q.bitstr.equals(p.bitstr) && q.IsDominate(p)) { // 不是虚拟点同时支配
					isUniqueSkyline = false;
					isLocalDominant = false;
					break;
				}
			}
			if (isUniqueSkyline) {
				boolean isDominant = true;
				int rsize = r.size();
				for (int j = 0; j < rsize; j++) {
					DataItem q = r.poll();
					if (q.IsWDominate(p)) {
						isDominant = false;
						if (q.bitstr.equals(p.bitstr) && q.IsDominate(p))
							isLocalDominant = false;
						if (!q.bitstr.equals(p.bitstr))
							t.offer(q);
					}
					if (!p.IsWDominate(q))
						r.offer(q);
					else {
						LinkedList<DataItem> qt = map.get(q.bitstr);
						qt.offer(q);
						qt.offer(p);
					}
				}
				if (isDominant)
					r.offer(p);
			}
			if (isLocalDominant)
				t.offer(p);
		}
		int rsize = r.size();
		for (int i = 0; i < rsize; i++) {
			DataItem p = r.poll();
			boolean iswdominant = true;
			Iterator mapiter = map.entrySet().iterator();
			while (mapiter.hasNext()) {
				Map.Entry<String, LinkedList<DataItem>> entry = (Map.Entry<String, LinkedList<DataItem>>) mapiter
						.next();
				String bitstr = entry.getKey();
				LinkedList<DataItem> t = entry.getValue();
				if (!p.bitstr.equals(bitstr)) {
					Iterator<DataItem> listiter = t.listIterator();
					while (listiter.hasNext()) {
						DataItem q = listiter.next();
						if (q.bitstr.equals(bitstr) && q.IsWDominate(p)) {
							iswdominant = false;
							break;
						}
					}
				}
				if (!iswdominant)
					break;
			}
			if (iswdominant)
				r.offer(p);
		}
		return r;
	}
}
