import java.io.*;
import java.util.Scanner;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Version;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import java.util.ArrayList;
public class Searcher{
	public static void main(String[] args) throws Exception {
		String indexdir="/home/cracker/Documents/subjects/IR/lucene/id";
		String datadir="/home/cracker/Documents/subjects/IR/lucene/meddata";
		FileIndexer indexer;
		FileSearcher searcher;
		indexer=new FileIndexer(indexdir);
		indexer.indexdirectory(datadir);
		System.out.println(indexer.numofdocs());
		indexer.close();
		searcher=new FileSearcher(indexdir);
		String[] queries= read_queries("MED.QRY");
		ArrayList<ArrayList<Integer>> array= read_relations("MED.REL");
		BufferedWriter bw=new BufferedWriter(new FileWriter("medlineprerec.txt"));
		for(int i=0;i<25;i++)
		{
			ArrayList<Integer> brr= (ArrayList<Integer>)array.get(i);
			ArrayList<Integer> arr= searcher.getdocuments(queries[i],1000);
			bw.write("Precision"+find_precision(arr,brr)+"\n");
			bw.write("Recall"+find_recall(arr,brr)+"\n");
		}
		bw.close();	
		searcher.close();
	}
	public static String[] read_queries(String filename) throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		double i=-1.0;
		String[] queries= new String[30];
		String line=br.readLine();
		for(int j=0;j<30;j++)
		{
			queries[j]="";
		}
		while(line!=null)
		{
			if(line.charAt(0)=='.')
			{
				i=i+.5;
			}
			if(line.charAt(0)!='.')
			{
				queries[(int)i]=queries[(int)i]+line;
			}
			line=br.readLine();
		}
		br.close();
		return queries;
	}
	public static double find_precision(ArrayList<Integer> a, ArrayList<Integer> b)
{
	int sizea= a.size();
	double val=0.0;
	for(int i=0;i<sizea;i++)
	{
		if(b.contains(a.get(i)))
		{
			val=val+1;
		}
	}
	return val/sizea;
}
public static double find_recall(ArrayList<Integer> a, ArrayList<Integer> b)
{
	int sizeb= b.size();
	double val=0.0;
	for(int i=0;i<sizeb;i++)
	{
		if(a.contains(b.get(i)))
		{
			val=val+1;
		}
	}
	return val/sizeb;
}
public static ArrayList<ArrayList<Integer>> read_relations(String filename) throws Exception
{
	BufferedReader br = new BufferedReader(new FileReader(filename));
		ArrayList<ArrayList<Integer>> array=new ArrayList<ArrayList<Integer>>();
		for(int i=0;i<30;i++)
		{
			ArrayList<Integer> arr= new ArrayList<Integer>();
			array.add(arr);
		}
		String line= br.readLine();
		while(line!=null)
		{
			String[] relations=line.split("   ");
			int query= Integer.parseInt(relations[0]);
			int doc= Integer.parseInt(relations[1]);
			ArrayList<Integer> a= (ArrayList<Integer>)array.get(query-1);
			a.add(doc);
			line=br.readLine();
		}
		return array;
}
}
class FileSearcher{
	IndexSearcher isearcher;
	QueryParser queryParser;
	int fileno=0;
	public FileSearcher(String idir)throws Exception
	{
		Directory indexdir=FSDirectory.open(new File(idir));
		isearcher= new IndexSearcher(indexdir);
		queryParser= new QueryParser(Version.LUCENE_36,
					"data",new StandardAnalyzer(Version.LUCENE_36));
	}
	public Query buildquery(String q) throws Exception
	{
		return queryParser.parse(q);
	}
	public TopDocs search(String q,int v) throws Exception
	{
		Query qr = buildquery(q);
		return isearcher.search(qr,v);
	}
	public ArrayList<Integer> getdocuments(String q,int v) throws Exception
	{
		ArrayList<Integer> array = new ArrayList<Integer>();
		TopDocs td=search(q,v);
		ScoreDoc[] sd= td.scoreDocs;
		for (ScoreDoc s : sd)
		{
			int id=Integer.parseInt(isearcher.doc(s.doc).get("ID"));
			array.add(id);
		}
		writetofile(array);
		return array;
	}
	public void close() throws Exception
	{
		isearcher.close();
	}
	public void writetofile(ArrayList<Integer> array)throws Exception
	{
		fileno=fileno+1;
		String filename= "out"+fileno+".txt";
		BufferedWriter br = new BufferedWriter(new FileWriter(filename));
		for(int i=0;i<array.size();i++)
		{
			br.write(array.get(i)+",");
		}
		br.close();
	}
}
class FileIndexer{
	IndexWriter iwriter;
	public FileIndexer(String indexdir) throws Exception{
		Directory id= FSDirectory.open(new File(indexdir));
		iwriter = new IndexWriter(id,new StandardAnalyzer(Version.LUCENE_36),true,
									IndexWriter.MaxFieldLength.UNLIMITED);

	}
	public void close() throws Exception
	{
		iwriter.close();
	}
	public void indexdocument(Document doc) throws Exception{
		iwriter.addDocument(doc);
	}
	public void indexdirectory(String  dirpath) throws Exception 
	{
		File[] files = new File(dirpath).listFiles();
		for (File file : files)
		{
			if(!file.isDirectory() && !file.isHidden() && file.getName().endsWith(".txt"))
			{
				Document doc= createdocument(file);
				indexdocument(doc);
			}
		}
	}
	public Document createdocument(File file) throws Exception
	{
		Document doc= new Document();
		String filedata="";
		FileInputStream inp= new FileInputStream(file);
		byte[] bf=new byte[(int)file.length()];
		inp.read(bf);
		filedata=new String(bf,"UTF-8");
		String name= file.getName();
		String firstpart=name.substring(0,name.indexOf("."));
		String number = firstpart.substring(3);
		Field data= new Field("data",filedata,Field.Store.NO,Field.Index.ANALYZED);
		Field nam= new Field("name",name,Field.Store.YES,Field.Index.NOT_ANALYZED);
		Field id= new Field("ID",number,Field.Store.YES,Field.Index.NOT_ANALYZED);
		Field path =new Field("path",file.getCanonicalPath(),Field.Store.YES,Field.Index.NOT_ANALYZED);
		doc.add(data);
		doc.add(nam);
		doc.add(id);
		doc.add(path);
		return doc;

	}
	public int numofdocs() throws Exception
	{
		return iwriter.numDocs();
	}
}
class NewAnalyzer extends Analyzer{
	@Override
  	public TokenStream tokenStream(String fieldName,Reader reader) {
    StandardTokenizer src = new StandardTokenizer(Version.LUCENE_36, reader);
    src.setMaxTokenLength(255);
    src.setReplaceInvalidAcronym(true);
    TokenStream tok = new StandardFilter(Version.LUCENE_36, src);
    tok = new LowerCaseFilter(Version.LUCENE_36, tok);
    tok = new StopFilter(Version.LUCENE_36, tok, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    tok = new PorterStemFilter(tok);
    return tok;
  }
}