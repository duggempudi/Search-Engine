import java.io.*;
import org.apache.lucene.analysis.standard.StandardAnalyser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.Directory;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDDirectory;
import org.apache.lucene.util.Version;
public class FileIndexer{
	IndexWriter iwriter;
	public FileIndexer(String indexdir) throws Exception{
		Directory id= FSDDirectory.open(new File(indexdir));
		iwriter = new IndexWriter(id,new StandardAnalyser(Version.LUCENE_30),ture,
									IndexWriter.MaxFieldLength.UNLIMITED);

	}
	public void closeindexer() throws Exception
	{
		iwriter.close()
	}
	public void indexdocument(Document doc) throws Exception{
		writer.addDocument(doc);
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
		Field data= new Field("data",new FileReader(file),
			Field.Store.NO, Filed.Index.ANALYZED);
		Field name= new Field("name",File.getName(),
			Field.Store.YES, Field.Index.NOT_ANALYZED);
		Filed path =new Field("path",File.getCanonicalPath(),
			Filed.Store.YES, Filed.Index.NOT_ANALYZED);
		doc.add(data);
		doc.add(name);
		doc.add(path);
		return doc;

	}
	public int numofdocs() throws Exception
	{
		return writer.numofdocs();
	}
}