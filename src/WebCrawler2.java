package webcrawler;

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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.plaf.synth.SynthSeparatorUI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

public class WebCrawler2 {
	/**
	 * @author Vaibhav
	 */
	static int linkCount = 0;
	private static String currDir = System.getProperty("user.dir");
	private static String TASK1_LINK_FILE_NAME;
	private static final String TASK1_INLINK_FILENAME = "Task1_inlinks.txt";
	private static final String TASK1_OUTLINK_FILENAME = "Task1_outlinks.txt";
	private static Map<String, List<String>> inLinkMap = new HashMap<String, List<String>>();
	private static Map<String, List<String>> outLinkMap = new HashMap<String, List<String>>();

	public static void main(String[] args) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Enter path to page list file:");
			TASK1_LINK_FILE_NAME = br.readLine();
			WebCrawler2 crawler = new WebCrawler2();
			crawler.task1();
		} catch (NumberFormatException nfe) {
			System.out.println("ERROR: Incorrect input\nExiting");
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Function to execute Task1 BFS crawling of all URLs
	 */
	public void task1() {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			List<String> pages = new LinkedList<String>();
			File file = new File(TASK1_LINK_FILE_NAME);
			br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				pages.add(line);
			}
			br.close();
			crawl_task1(pages);
			printMap(inLinkMap, currDir + File.separator + TASK1_INLINK_FILENAME);
			printMap(outLinkMap, currDir + File.separator + TASK1_OUTLINK_FILENAME);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Task 1 implementation
	 * 
	 * @param url
	 * @param localAnchors
	 */
	public static void crawl_task1(List<String> pages) {
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			e.printStackTrace();
		}

		File linkFile = new File(TASK1_LINK_FILE_NAME);
		for (String page : pages) {
			try {
				List<String> outLinks = new LinkedList<String>();
				if (!page.contains("%")) {
					org.jsoup.nodes.Document doc = Jsoup.connect(page).timeout(10000).validateTLSCertificates(false)
							.get();

					String[] tmp = page.split("/wiki/");
					String urlID = tmp[1];

					// Get HTML content
					Element elem = doc.getElementById("content");
					org.jsoup.select.Elements links = elem.getElementsByTag("a");
					for (Element link : links) {
						String linkHref = link.attr("href");
						String absUrl = link.absUrl("href");
						// Filtering administrative links and links pointing to
						// the same page
						if (pages.contains(absUrl)) {
							if (!linkHref.contains(":") && !linkHref.contains("#")) {
								if (linkHref.contains("/wiki/")) {
									String[] tmp1 = absUrl.split("/wiki/");
									String anchorId = tmp1[1];
									if (inLinkMap.containsKey(anchorId)) {
										List<String> linkElems = inLinkMap.get(anchorId);
										if (!linkElems.contains(urlID)) {
											linkElems.add(urlID);
										}
										inLinkMap.put(anchorId, linkElems);
									} else {
										List<String> newList = new LinkedList<String>();
										newList.add(urlID);
										inLinkMap.put(anchorId, newList);
									}
									outLinks.add(anchorId);
									outLinkMap.put(urlID, outLinks);
								}
							}
						}
					}
				}
			}

			catch (Exception ex) {
				Logger.getLogger(WebCrawler2.class.getName()).log(Level.SEVERE, null, ex);
				continue;
			}
		}
	}

	public static void printMap(Map<String, List<String>> hm, String fileName) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName), true));
			for (String str : hm.keySet()) {
				bw.write(str + " ");
				List<String> inlinks = hm.get(str);
				if (inlinks != null) {
					for (String link : inlinks) {
						bw.write(link + " ");
					}
				}
				bw.newLine();
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
