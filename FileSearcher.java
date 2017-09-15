import java.io.*;
import org.apache.lucene.analysix.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDDirectory;
import org.apache.lucene.util.Version;
public FileSearcher{
	IndexSearcher isearcher;
	QueryParser queryParser;
	public FileSearcher(String indexdir)throws Exception
	{
		Directory indexdir= FSDDirectory.open(new File(indexdir));
		isearcher= new IndexSearcher(indexdir);
		queryParser= new QueryParser(Version.LUCENE_36,
					"data",new StandardAnalyzer(Version.LUCENE_36));
	}
	public Query buildquery(String q) throws Exception
	{
		return queryParser.parse(q);
	}
	public TopDocs search(String q) throws Exception
	{
		Query qr = buildquery(q);
		return isearcher.search(qr,10);
	}
	public void getdocuments(String q) throws Exception
	{
		TopDocs td=search(q);
		ScoreDoc sd= td.ScoreDocs;
		for (ScoreDoc s : sd)
		{
			System.out.println(isearcher.doc(s.doc).get("path"));
		}
	}
	public void close() throws Exception
	{
		isearcher.close();
	}
}