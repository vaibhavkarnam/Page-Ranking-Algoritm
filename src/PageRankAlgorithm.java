import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PageRankAlgorithm {
	public static final float d = (float) 0.85;
	public static int N;
	public static int counter = 0;
	public static final String PERPLEXITY_FILE_PATH = System.getProperty("user.dir") + File.separator
			+ "perplexity.txt";
	public static final String OUTPUT_FILE = System.getProperty("user.dir") + File.separator + "Task2_links.txt";
	public static double prev_perplexity = 0.0;

	public static void main(String args[]) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter input graph file path: ");
			String input_file_path = br.readLine();
			init(input_file_path);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void init(String inputFile) {
		File file = new File(inputFile);
		List<String> pages = getPagesFromGraph(file);
		Map<String, List<String>> inlinks = getInlinksFromGraph(file);
		Map<String, List<String>> outlinks = getOutlinksFromInlinkMap(inlinks);
		Map<String, Integer> L = getLFromOutlinkMap(outlinks);
		List<String> sinkNodes = getSinkNodes(outlinks);

		Map<String, Double> pageRank = rankPages(pages, sinkNodes, inlinks, L);
		List<PageRank> sortedList = sortLinks(pageRank);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_FILE));
			int ctr = 0;
			for (PageRank page : sortedList) {
				ctr++;
				if (ctr > 50) {
					break;
				}
				bw.write(String.format("%-65s", page.getDocID()));
				bw.write(String.format("%-65s", page.getPageRank()));
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<String> getPagesFromGraph(File file) {
		List<String> pages = new LinkedList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String tmp[] = line.split(" ");
				pages.add(tmp[0]);
				N++;
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pages;
	}

	public static Map<String, List<String>> getInlinksFromGraph(File file) {
		Map<String, List<String>> inlinkMap = new HashMap<String, List<String>>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				String tmp[] = line.split(" ");
				if (inlinkMap.containsKey(tmp[0])) {
					List<String> values = new LinkedList<String>();
					for (int i = 1; i < tmp.length; i++) {
						values.add(tmp[i]);
					}
					inlinkMap.put(tmp[0], values);
				} else {
					List<String> nodeList = new LinkedList<String>();
					for (int i = 1; i < tmp.length; i++) {
						nodeList.add(tmp[i]);
					}
					inlinkMap.put(tmp[0], nodeList);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return inlinkMap;
	}

	public static Map<String, List<String>> getOutlinksFromInlinkMap(Map<String, List<String>> inlinks) {
		Map<String, List<String>> outlinks = new HashMap<String, List<String>>();
		for (String node : inlinks.keySet()) {
			for (String link : inlinks.get(node)) {
				if (outlinks.containsKey(link)) {
					List<String> list = outlinks.get(link);
					list.add(node);
					outlinks.put(link, list);
				} else {
					List<String> newList = new LinkedList<String>();
					newList.add(node);
					outlinks.put(link, newList);
				}
			}
		}
		return outlinks;
	}

	public static Map<String, Integer> getLFromOutlinkMap(Map<String, List<String>> outlinks) {
		Map<String, Integer> L = new HashMap<String, Integer>();
		for (String key : outlinks.keySet()) {
			List<String> values = outlinks.get(key);
			L.put(key, values.size());
		}
		return L;
	}

	public static List<String> getSinkNodes(Map<String, List<String>> outlinks) {
		List<String> sinkNodes = new LinkedList<String>();
		for (String key : outlinks.keySet()) {
			if (outlinks.get(key).size() == 0) {
				sinkNodes.add(key);
			}
		}
		return sinkNodes;
	}

	public static Map<String, Double> rankPages(List<String> pages, List<String> sinkNodes,
			Map<String, List<String>> inlinks, Map<String, Integer> L) {
		Map<String, Double> PR = new HashMap<String, Double>();
		Map<String, Double> newPR = new HashMap<String, Double>();
		for (String p : pages) {
			PR.put(p, (double) (1 / N));
		}

		while (checkConverged(PR)) {
			int sinkPR = 0;
			for (String s : sinkNodes) {
				sinkPR += PR.get(s);
			}

			for (String p : pages) {
				newPR.put(p, (double) ((1 - d) / N));
				newPR.put(p, (double) (newPR.get(p) + (d * sinkPR / N)));
				for (String q : inlinks.get(p)) {
					newPR.put(p, (double) (newPR.get(p) + (d * PR.get(q) / L.get(q))));
				}
			}

			for (String p : pages) {
				PR.put(p, newPR.get(p));
			}
		}
		return PR;
	}

	public static boolean checkConverged(Map<String, Double> PR) {

		double entropy = 0;
		for (String p : PR.keySet()) {
			entropy += (double) (PR.get(p) * Math.log(PR.get(p)) / Math.log(2));
		}
		entropy = entropy * -1;
		double perplexity = Math.pow(2, entropy);

		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(PERPLEXITY_FILE_PATH, true));
			bw.write(Double.toString(perplexity));
			bw.newLine();
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		double perplexity_diff = Math.abs(perplexity - prev_perplexity);
		if (perplexity_diff < 1) {
			counter++;
		} else {
			counter = 0;
		}

		prev_perplexity = perplexity;

		if (counter < 4) {
			return true;
		} else {
			return false;
		}
	}

	public static List<PageRank> sortLinks(Map<String, Double> pageRank) {
		List<PageRank> sortedList = new LinkedList<PageRank>();
		for (String page : pageRank.keySet()) {
			PageRank pr = new PageRank(page, pageRank.get(page));
			sortedList.add(pr);
		}
		quicksort(0, sortedList.size() - 1, sortedList);
		return sortedList;
	}

	private static void quicksort(int low, int high, List<PageRank> sortedList) {
		int i = low, j = high;
		double pivot = sortedList.get(low + (high - low) / 2).getPageRank();
		while (i <= j) {
			while (sortedList.get(i).getPageRank() > pivot) {
				i++;
			}
			while (sortedList.get(j).getPageRank() < pivot) {
				j--;
			}
			if (i <= j) {
				PageRank tmp = new PageRank(sortedList.get(i).getDocID(), sortedList.get(i).getPageRank()); 
				sortedList.set(i, sortedList.get(j)); 
				sortedList.set(j, tmp); 
				i++;
				j--;
			}
		}
		if (low < j)
			quicksort(low, j, sortedList);
		if (i < high)
			quicksort(i, high, sortedList);
	}
}
