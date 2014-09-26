import java.util.ArrayList;

import it.uniroma3.mat.extendedset.intset.ConciseSet;

public class DataItem {

	public int index;
	public int[] data;
	public ConciseSet bitmap;
	// public boolean isvirtual;
	public String bitstr;
	public ArrayList<Integer> edim;

	// public int edimnum;
	// public ConciseSet com_bitmap;///—È÷§
	// public int[] theKeyColumn;
	// public int dcount;
	// public int dominatedC;
	public DataItem() {
		// TODO Auto-generated constructor stub
		data = new int[Global_Variable.dimension];
		// theKeyColumn=new int[dimension];
		bitmap = new ConciseSet();
		// edim = new ArrayList<Integer>();
		// isvirtual = false;
		// com_bitmap=new ConciseSet();
	}
	public boolean IsWDominate(DataItem p){
		Global_Variable.comparsionNo++;
		boolean isgreater = false;
		int weight = 0;
		// if(p.bitmap.isEmpty())
		// return true;
		for (int i = 0; i < Global_Variable.dimension; i++) {
			if ((this.bitmap.contains(i) == false)
					|| (p.bitmap.contains(i) == false))
				weight+=Global_Variable.W[i];
			else if (this.data[i] > p.data[i]) {
				isgreater = true;
				weight+=Global_Variable.W[i];
			} else if (this.data[i] == p.data[i])
				weight+=Global_Variable.W[i];
			if (weight >= Global_Variable.w && isgreater)
				return true;
		}
		return false;
	}

	public boolean IsKDominate(DataItem p) {
		Global_Variable.comparsionNo++;
		boolean isgreater = false;
		int count = 0;
		// if(p.bitmap.isEmpty())
		// return true;
		for (int i = 0; i < Global_Variable.dimension; i++) {
			if ((this.bitmap.contains(i) == false)
					|| (p.bitmap.contains(i) == false))
				count++;
			else if (this.data[i] > p.data[i]) {
				isgreater = true;
				count++;
			} else if (this.data[i] == p.data[i])
				count++;
			if (count >= Global_Variable.K && isgreater)
				return true;
		}
		return false;
	}

	public boolean IsDominate(DataItem p) {
		Global_Variable.comparsionNo++;
		boolean isgreater = false;
		// if(p.bitmap.isEmpty())
		// return true;
		for (int i = 0; i < Global_Variable.dimension; i++) {
			if ((this.bitmap.contains(i) == false)
					|| (p.bitmap.contains(i) == false))
				continue;
			else if (this.data[i] > p.data[i])
				isgreater = true;
			else if (this.data[i] < p.data[i])
				return false;
		}
		if (isgreater)
			return true;
		else
			return false;
	}

	public int maxDom(DataItem p) {
		boolean isgreater = false;
		int count = 0;
		for (int i = 0; i < Global_Variable.dimension; i++) {
			if ((this.bitmap.contains(i) == false)
					|| (p.bitmap.contains(i) == false))
				count++;
			else if (this.data[i] > p.data[i]) {
				isgreater = true;
				count++;
			} else if (this.data[i] == p.data[i])
				count++;
		}
		if (isgreater)
			return count;
		else
			return 0;
	}
}
