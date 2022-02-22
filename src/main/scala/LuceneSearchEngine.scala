import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexNotFoundException}
import org.apache.lucene.search.{FuzzyQuery, IndexSearcher, PrefixQuery, Query, TermQuery, TopDocs, WildcardQuery}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.QueryBuilder
import org.apache.lucene.index.Term
import scala.io.StdIn.readInt
import java.io.File
import java.nio.file.Paths
import java.util.InputMismatchException

class LuceneSearchEngine extends Indexer {

  private final val MAXHITS = 100

  /**
   * get the directory of data
   *
   * @param filePath path of the directory
   */
  def createIndexFiles(filePath: String): String = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to getDataDirectory function in LuceneSearchEngine class")
    logger.startTime ()
    if (filePath.nonEmpty) {
      val dataFile = new File (filePath)
      if (checkFiles (dataFile)) {
        index(dataFile)
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
    // removes the stop words
    val analyzer = new StandardAnalyzer ()
    // creates query from the analyzer
    val builder = new QueryBuilder (analyzer)
    var query: Query = null
    val searchType = getQueryType
    try {
      query = getQuery (searchType, queryStr.toLowerCase, builder)
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
      searchedFiles += s"${i + 1}" + d.get ("fileName") + " Score :" + hits (i).score + "\n"
    }
    logger.stopTime ()
    logger.logWritter ("info", "Execution time for searchIndex is  " + logger.getTime)
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
   * @param builder   query builder
   * @return
   */
  private def getQuery(queryType: String, queryStr: String, builder: QueryBuilder): Query = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to getQuery function in LuceneSearchEngine class")
    logger.startTime ()
    var query: Query = null
    queryType.toLowerCase match {
      case "termquery" =>
        // search for the text it is case sensitive
        val term = new Term ("content", queryStr)
        query = new TermQuery (term)
      case "phrasequery" =>
        // search documents which contain a particular sequence of terms
        query = builder.createPhraseQuery ("content", queryStr)
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
    logger.logWritter ("info", "Execution time for getQuery is  " + logger.getTime)
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
    println (" 1. TermQuery\n 2. phrasequery\n 3. wildcardquery\n 4. prefixquery\n 5. fuzzyquery")
    println ("give a number between 1 to 5 : ")
    var input: String = null
    try {
      input = readInt () match {
        case 1 => "termquery"
        case 2 => "phrasequery"
        case 3 => "wildcardquery"
        case 4 => "prefixquery"
        case 5 => "fuzzyquery"
        case _ =>
          println ("please give the number within the range of 1 to 5")
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
  println (lucene.createIndexFiles ("dataFiles"))
  println (lucene.searchIndex ("aasi"))
}
