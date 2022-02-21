import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexNotFoundException}
import org.apache.lucene.search.{FuzzyQuery, IndexSearcher, PhraseQuery, PrefixQuery, Query, TermQuery, TopDocs, WildcardQuery}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.QueryBuilder
import org.apache.lucene.index.Term

import java.io.File
import java.nio.file.Paths

class LuceneSearchEngine extends Indexer {

  private final val MAXHITS = 100

  /**
   * get the directory of data
   * @param filePath path of the directory
   */
  def getDataDirectory(filePath: String): String = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to getDataDirectory function in LuceneSearchEngine class")
    logger.startTime ()
    if (filePath.nonEmpty) {
      val dataFile = new File (filePath)
      if (checkFiles (dataFile)) {
        indexString (dataFile)
      }
      else {
        removeIndexDir ()
        logger.logWritter ("error", "Please check the directory")
      }
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for getDataDirectory is  " + logger.getTime)
      logger.logWritter ("info", "Exiting from getDataDirectory function in LuceneSearchEngine class")
      "index file created"
    }
    else {
      removeIndexDir ()
      logger.logWritter ("error", "file path is empty")
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for getDataDirectory is  " + logger.getTime)
      logger.logWritter ("info", "Exiting from getDataDirectory function in LuceneSearchEngine class")
      try {
        throw new NullPointerException
      }
      catch {
        case _: NullPointerException => "file path is empty"
      }
    }
  }


  /**
   * check the file permission and the file exists or not
   * @param file as File
   * @return true or false
   */
  private def checkFiles(file: File): Boolean = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to checkFiles function in LuceneSearchEngine class")
    logger.startTime ()
    if (!file.isHidden && file.exists () && file.canRead && file.listFiles ().length != 0) {
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for checkFiles is  " + logger.getTime)
      logger.logWritter ("info", "Exiting from checkFiles function in LuceneSearchEngine class")
      true
    }
    else {
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for checkFiles is  " + logger.getTime)
      logger.logWritter ("info", "Exiting from checkFiles function in LuceneSearchEngine class")
      false
    }
  }

  /**
   * search the text with it index and returns the files containing the text
   * @param queryStr query text for searching
   * @return
   */
  def searchIndex(queryStr: String, searchType: String): String = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to searchIndex function in LuceneSearchEngine class")
    logger.startTime ()
    val directory = FSDirectory.open (Paths.get(INDEXDIRECTORYPATH))
    var indexReader: DirectoryReader = null
    try {
      indexReader = DirectoryReader.open (directory)
    }
    catch {
      case _: IndexNotFoundException => return "Index file not found"
    }
    val searcher = new IndexSearcher (indexReader)
    val analyzer = new StandardAnalyzer ()
    val builder= new QueryBuilder (analyzer)
    val query = getQuery(searchType.toLowerCase,queryStr,builder)
    var topDocs: TopDocs = null
    try {
      topDocs = searcher.search (query, MAXHITS)
    }
    catch {
      case _: NullPointerException => return "search query is empty"
    }
    val hits = topDocs.scoreDocs
    var searchedFiles = ""
    for (i <- 0 until hits.length) {
      val docId = hits (i).doc
      val d = searcher.doc (docId)
      searchedFiles += s"${i + 1}" + d.get ("fileName") + " Score :" + hits (i).score + "\n"
    }
    logger.stopTime ()
    logger.logWritter ("info", "Execution time for searchIndex is  " + logger.getTime)
    logger.logWritter ("info", "Exiting from searchIndex function in LuceneSearchEngine class")
    logger.logWritter ("info", "total hits founded is " + hits.length)
    searchedFiles
  }

  private def getQuery(searchType:String,queryStr:String,builder : QueryBuilder): Query ={
    var query : Query = null
    searchType match {
      case "termquery" =>
        val term = new Term ("content", queryStr)
        query = new TermQuery(term)
        query
      case "phrasequery" =>
        query = builder.createPhraseQuery ("content", queryStr)
        query
      case "wildcardquery" =>
        val term = new Term ("content", queryStr)
        query = new WildcardQuery(term)
        query
      case "prefixquery" =>
        val term = new Term ("content", queryStr)
        query = new PrefixQuery(term)
        query
      case "fuzzyquery" =>
        val term = new Term ("content", queryStr)
        query = new FuzzyQuery(term)
        query
    }
  }
}

object LuceneSearchEngineObj extends App {
  val lucene = new LuceneSearchEngine
  println (lucene.getDataDirectory ("dataFiles"))
  println (lucene.searchIndex ("pain", "FuzzyQuery"))
}
