import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, StoredField, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import org.apache.commons.io.FileUtils
import java.io.{File, FileReader}
import java.nio.file.{Files, Paths}

class Indexer {
  private final val INDEXDIRECTORYPATH = "indexDir"

  /**
   * Indexing the files in the given directory
   * @param dataDir filePath
   */
  def indexString(dataDir:File): Unit ={
    val logger = new Logger
    logger.logWritter("info","Entered in to indexString function in Indexer class")
    logger.startTime()
    removeIndexDir()
    val indexDir = Files.createDirectory(Paths.get(INDEXDIRECTORYPATH))
    val analyzer = new StandardAnalyzer()
    val config = new IndexWriterConfig(analyzer)
    val indexWriter = new IndexWriter(FSDirectory.open(indexDir),config)
    val files = dataDir.listFiles()
    for (f <- files) {
      val doc = new Document ()
      doc.add (new TextField ("content", new FileReader (f)))
      doc.add (new StoredField ("fileName", f.getCanonicalPath))
      indexWriter.addDocument (doc)
    }
    logger.logWritter("info","Index files created")
    indexWriter.close()
    logger.stopTime()
    logger.logWritter("info","Execution time for indexString is  " + logger.getTime)
    logger.logWritter("info","Exiting from indexString function in Indexer class")
  }

  /**
   * removes the indexDir if its exists
   */
  private def removeIndexDir(): Unit ={
    val logger = new Logger
    logger.logWritter("info","Entered in to removeIndexDir function in Indexer class")
    logger.startTime()
    val indexFolder = new File(INDEXDIRECTORYPATH)
    if(indexFolder.exists()){
      FileUtils.deleteDirectory(indexFolder)
      logger.logWritter("info",s"$INDEXDIRECTORYPATH removed")
    }
    logger.stopTime()
    logger.logWritter("info","Execution time for removeIndexDir is  " + logger.getTime)
    logger.logWritter("info","Exiting from removeIndexDir function in Indexer class")
  }
}
