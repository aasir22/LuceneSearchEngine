import org.apache.lucene.index.{DirectoryReader, IndexNotFoundException}
import org.apache.lucene.search.{FuzzyQuery, IndexSearcher, PrefixQuery, Query, TermQuery, TopDocs, WildcardQuery}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.index.Term

import scala.io.StdIn.readInt
import java.io.{File, FileNotFoundException}
import java.nio.file.Paths
import java.util.InputMismatchException

class LuceneSearchEngine extends Indexer {

  private final val MAXHITS = 100

  /**
   * get the directory of data
   * @param filePath path of the directory
   * @param canRemoveOldIndex remove index or not
   */
  def createIndexFiles(filePath: String,canRemoveOldIndex:Boolean): String = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to getDataDirectory function in LuceneSearchEngine class")
    logger.startTime ()
    if (filePath.nonEmpty) {
      val dataFile = new File (filePath)
      if (checkFiles (dataFile)) {
        try{
          index(dataFile,canRemoveOldIndex)
        }
        catch {
          case _:FileNotFoundException => return "check the data"
        }

      }
      else {
        removeIndexDir ()
        logger.logWritter ("error", "Please check the directory")
      }
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for getDataDirectory is  " + logger.getTime + " ms")
      logger.logWritter ("info", "Exiting from getDataDirectory function in LuceneSearchEngine class")
      "index file created"
    }
    else {
      removeIndexDir ()
      logger.logWritter ("error", "file path is empty")
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for getDataDirectory is  " + logger.getTime+ " ms")
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
   *
   * @param file as File
   * @return true or false
   */
  private def checkFiles(file: File): Boolean = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to checkFiles function in LuceneSearchEngine class")
    logger.startTime ()
    if (!file.isHidden && file.exists () && file.canRead && file.listFiles ().length != 0) {
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for checkFiles is  " + logger.getTime+ " ms")
      logger.logWritter ("info", "Exiting from checkFiles function in LuceneSearchEngine class")
      true
    }
    else {
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for checkFiles is  " + logger.getTime+ " ms")
      logger.logWritter ("info", "Exiting from checkFiles function in LuceneSearchEngine class")
      false
    }
  }

  /**
   * search the text with it index and returns the files containing the text
   *
   * @param queryStr query text for searching
   * @return
   */
  def searchIndex(queryStr: String): String = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to searchIndex function in LuceneSearchEngine class")
    logger.startTime ()
    val directory = FSDirectory.open (Paths.get (INDEXDIRECTORYPATH))
    var indexReader: DirectoryReader = null
    try {
      // reads the indexes files from the index directory
      indexReader = DirectoryReader.open (directory)
    }
    catch {
      case _: IndexNotFoundException => return "Index file not found"
    }
    // creating an instance of Index Searcher
    val searcher = new IndexSearcher (indexReader)
    var query: Query = null
    try {
      val searchType = getQueryType
      query = getQuery (searchType, queryStr.toLowerCase)
    }
    catch {
      case _: InputMismatchException => return "Please give the correct search type"
      case _: NullPointerException => return "please check the search type you given"
    }
    var topDocs: TopDocs = null
    try {
      // search the query in the index files
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
      println("*"*50)
      val a = searcher.explain(query,docId)
      println(a)
      println("*"*50)
      searchedFiles += s"${i + 1}." + d.get("fileName") + " Score :" + hits(i).score + "\n"
    }
    logger.stopTime ()
    logger.logWritter ("info", "Execution time for searchIndex is  " + logger.getTime+ " ms")
    logger.logWritter ("info", "Exiting from searchIndex function in LuceneSearchEngine class")
    logger.logWritter ("info", "total hits founded is " + hits.length)
    if (searchedFiles.isEmpty) {
      "No files founded"
    } else {
      searchedFiles
    }
  }

  /**
   * create query from the query type
   * @param queryType type of query
   * @param queryStr  searching string
   * @return
   */
  private def getQuery(queryType: String, queryStr: String): Query = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to getQuery function in LuceneSearchEngine class")
    logger.startTime ()
    var query: Query = null
    queryType.toLowerCase match {
      case "termquery" =>
        // search for the text it is case sensitive
        val term = new Term ("content", queryStr)
        query = new TermQuery (term)
      case "wildcardquery" =>
        // wildcard characters are *,?
        val term = new Term ("content", queryStr)
        query = new WildcardQuery (term)
      case "prefixquery" =>
        // searches the query which matches the given prefix
        val term = new Term ("content", queryStr)
        query = new PrefixQuery (term)
      case "fuzzyquery" =>
        // search based on the similarities
        val term = new Term ("content", queryStr)
        query = new FuzzyQuery (term)
      case _ => throw new InputMismatchException ()
    }
    logger.stopTime ()
    logger.logWritter ("info", "Execution time for getQuery is  " + logger.getTime+ " ms")
    logger.logWritter ("info", "Exiting from getQuery function in LuceneSearchEngine class")
    query
  }

  /**
   * get the input from the user about which query type they want to use
   *
   * @return String of queryType
   */
  private def getQueryType: String = {
    println ("Please select any one in the types of query")
    println (" 1. TermQuery\n 2. wildcardquery\n 3. prefixquery\n 4. fuzzyquery")
    println ("give a number between 1 to 4 : ")
    var input: String = null
    try {
      input = readInt () match {
        case 1 => "termquery"
        case 2 => "wildcardquery"
        case 3 => "prefixquery"
        case 4 => "fuzzyquery"
        case _ =>
          println ("please give the number within the range of 1 to 4")
          throw new InputMismatchException
      }
    }
    catch {
      case _: NumberFormatException =>
        println ("please give numbers without spaces")
      case _: NullPointerException =>
        println ("please don't give space while giving your option")
      case _: InputMismatchException => println ("Please give the correct search type")
    }
    input
  }
}

object LuceneSearchEngineObj extends App {
  val lucene = new LuceneSearchEngine
  println (lucene.createIndexFiles ("dataFiles",canRemoveOldIndex = false))
  println (lucene.searchIndex ("diabetes"))
}
