import java.io.*;
import java.util.Scanner;
public class GutenbergSearcher{
	String indexdir="/home/cracker/Documents/subjects/IR/lucene/indexdir"
	String datadir="/home/cracker/Documents/subjects/IR/lucene/datadir"
	FileIndexer indexer;
	FileSearcher searcher;
	public static void main(String[] args) throws Exception {
		indexer=FileIndexer(indexdir);
		indexer.indexdirectory(datadir);
		searcher=FileSearcher(indexdir);
		Scanner scan= new Scanner(System.in);
		System.out.println("Enter query");
		String query= scan.next();
		while(query!="Exit")
		{
			searcher.getdocuments();
		}
		indexer.close();
		searcher.close();
	}
}