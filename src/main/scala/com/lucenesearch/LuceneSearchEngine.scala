package com.lucenesearch

import org.apache.lucene.index.{DirectoryReader, IndexNotFoundException, Term}
import org.apache.lucene.search._
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.BytesRef

import java.io.{File, FileNotFoundException}
import java.nio.file.Paths
import java.util.InputMismatchException

class LuceneSearchEngine extends Indexer {

  private final val MAXHITS = 10

  /**
   * get the directory of data
   *
   * @param filePath          path of the directory
   * @param canRemoveOldIndex remove index or not
   */
  def createIndexFiles(filePath: String, canRemoveOldIndex: Boolean): String = {
    val logger = new Logger
    logger.logWritter("info", "Entered in to getDataDirectory function in LuceneSearchEngine class")
    logger.startTime()
    if (filePath.nonEmpty) {
      val dataFile = new File(filePath)
      if (checkFiles(dataFile)) {
        try {
          index(dataFile, canRemoveOldIndex)
        }
        catch {
          case _: FileNotFoundException => return "check the data"
        }

      }
      else {
        removeIndexDir()
        logger.logWritter("error", "Please check the directory")
      }
      logger.stopTime()
      logger.logWritter("info", "Execution time for getDataDirectory is  " + logger.getTime + " ms")
      logger.logWritter("info", "Exiting from getDataDirectory function in LuceneSearchEngine class")
      "index file created"
    }
    else {
      removeIndexDir()
      logger.logWritter("error", "file path is empty")
      logger.stopTime()
      logger.logWritter("info", "Execution time for getDataDirectory is  " + logger.getTime + " ms")
      logger.logWritter("info", "Exiting from getDataDirectory function in LuceneSearchEngine class")
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
    logger.logWritter("info", "Entered in to checkFiles function in LuceneSearchEngine class")
    logger.startTime()
    if (!file.isHidden && file.exists() && file.canRead && file.listFiles().length != 0) {
      logger.stopTime()
      logger.logWritter("info", "Execution time for checkFiles is  " + logger.getTime + " ms")
      logger.logWritter("info", "Exiting from checkFiles function in LuceneSearchEngine class")
      true
    }
    else {
      logger.stopTime()
      logger.logWritter("info", "Execution time for checkFiles is  " + logger.getTime + " ms")
      logger.logWritter("info", "Exiting from checkFiles function in LuceneSearchEngine class")
      false
    }
  }

  /**
   * search the text with it index and returns the files containing the text
   *
   * @param queryStr query text for searching
   * @return
   */
  def searchIndex(queryStr: String, queryType: String): String = {
    val logger = new Logger
    logger.logWritter("info", "Entered in to searchIndex function in LuceneSearchEngine class")
    logger.startTime()
    val directory = FSDirectory.open(Paths.get(INDEXDIRECTORYPATH))
    var indexReader: DirectoryReader = null
    try {
      // reads the indexes files from the index directory
      indexReader = DirectoryReader.open(directory)
    }
    catch {
      case _: IndexNotFoundException => return "Index file not found"
    }
    // creating an instance of Index Searcher
    val searcher = new IndexSearcher(indexReader)
    var query: Query = null
    try {
      //      val searchType = getQueryType
      query = getQuery(queryType.toLowerCase, queryStr.toLowerCase)
    }
    catch {
      case _: InputMismatchException => return "Please give the correct search type"
      case _: NullPointerException => return "please check the search type you given"
    }
    var topDocs: TopDocs = null
    try {
      // search the query in the index files
      topDocs = searcher.search(query, MAXHITS)
    }
    catch {
      case _: NullPointerException => return "search query is empty"
    }
    val hits = topDocs.scoreDocs
    var searchedFiles = ""
    for (i <- 0 until hits.length) {
      val docId = hits(i).doc
      val explanation = searcher.explain(query, docId)
      val d = searcher.doc(docId)
      val getQuery = """content:[a-z 0-9]+""".r
      val searchedQuery = getQuery.findAllMatchIn(explanation.toString).toArray
      searchedFiles += s"${i + 1}." + d.get("fileName") + " Score :" + hits(i).score * 100 + " " + searchedQuery.mkString("Array(", ", ", ")") + "\n"
    }
    logger.stopTime()
    logger.logWritter("info", "Execution time for searchIndex is  " + logger.getTime + " ms")
    logger.logWritter("info", "Exiting from searchIndex function in LuceneSearchEngine class")
    logger.logWritter("info", "total hits founded is " + hits.length)
    if (searchedFiles.isEmpty) {
      "No files founded"
    } else {
      searchedFiles
    }
  }

  /**
   * create query from the query type
   *
   * @param queryType type of query
   * @param queryStr  searching string
   * @return
   */
  private def getQuery(queryType: String, queryStr: String): Query = {
    val logger = new Logger
    logger.logWritter("info", "Entered in to getQuery function in LuceneSearchEngine class")
    logger.startTime()
    var query: Query = null
    queryType.toLowerCase match {
      case "termquery" | "term query" =>
        // search for the term in the file
        val term = new Term("content", queryStr)
        query = new TermQuery(term)
      case "wildcardquery" | "wildcard query" =>
        // wildcard characters are *,?
        val term = new Term("content", queryStr)
        query = new WildcardQuery(term)
      case "prefixquery" | "prefix query" =>
        // searches the query which matches the given prefix
        val term = new Term("content", queryStr)
        query = new PrefixQuery(term)
      case "phrasequery" | "phrase query" =>
        val queryArr = queryStr.split("~")
        if (queryArr.length > 1) {
          query = new PhraseQuery(1, "content", new BytesRef(queryArr(0)), new BytesRef(queryArr(1)))
        }
      case "fuzzyquery" | "fuzzy query" =>
        // search based on the similarities
        val term = new Term("content", queryStr)
        query = new FuzzyQuery(term)
        println(query)
      case "andquery" | "and query" =>
        // search query based on the two or more quries must present in the file like AND operator
        val queryArr = queryStr.split("@@")
        query = getAndOrQuery(queryArr, isAndQuery = true)
      case "orquery" | "or query" =>
        // search any one of query presents in the file
        val queryArr = queryStr.split("##")
        query = getAndOrQuery(queryArr)
      case _ => throw new InputMismatchException()
    }
    logger.stopTime()
    logger.logWritter("info", "Execution time for getQuery is  " + logger.getTime + " ms")
    logger.logWritter("info", "Exiting from getQuery function in LuceneSearchEngine class")
    query
  }

  /**
   * create a boolean query of multiple words must present in the file
   *
   * @param queryArr   Array of search query
   * @param isAndQuery true or false
   * @return Query of words
   */
  private def getAndOrQuery(queryArr: Array[String], isAndQuery: Boolean = false): Query = {
    val logger = new Logger
    logger.logWritter("info", "Entered in to getAndOrQuery function in LuceneSearchEngine class")
    logger.startTime()
    val builder = new BooleanQuery.Builder()
    for (i <- queryArr.indices) {
      val term = new Term("content", queryArr(i).toLowerCase)
      val query = new TermQuery(term)
      if (isAndQuery) {
        builder.add(query, BooleanClause.Occur.MUST)
      }
      else {
        builder.add(query, BooleanClause.Occur.SHOULD)
      }

    }
    val query: Query = builder.build()
    logger.stopTime()
    logger.logWritter("info", "Execution time for getAndOrQuery is  " + logger.getTime + " ms")
    logger.logWritter("info", "Exiting from getAndOrQuery function in LuceneSearchEngine class")
    query
  }

  //  /**
  //   * get the input from the user about which query type they want to use
  //   * @return String of queryType
  //   */
  //  private def getQueryType: String = {
  //    println ("Please select any one in the types of query\n")
  //    println(
  //      """1.Term Query        - search for a single word in the indexed files
  //        |2.Wildcard Query    - it accepts wildcard characters(?*) ? matches one character * matches multiple characters ex.
  //        |                      ex: ha?d it can find hand,hard....
  //        |3.Prefix Query      - given query word matches any of the prefix word in the documents
  //        |4.Fuzzy Query       - matches the similar words in the documents
  //        |5.Phrase Query      - matches phrase of words. input should be like sweet~salt ex: Sweet as Salt
  //        |6.And Query         - matches if the given two words present in the file. input should be sweet@@salt
  //        |7.Or Query          - if any of the given inputs matches in file. input should be sweet##salt
  //        |""".stripMargin)
  //    println(" 1. TermQuery\n 2. wildcardquery\n 3. prefixquery\n 4. fuzzyquery \n 5. phrasequery\n 6. andquery\n 7. orquery")
  //    println ("give a number between 1 to 7 : ")
  //    var input: String = null
  //    try {
  //      input = readInt () match {
  //        case 1 => "term query"
  //        case 2 => "wildcardquery"
  //        case 3 => "prefixquery"
  //        case 4 => "fuzzyquery"
  //        case 5 => "phrasequery"
  //        case 6 => "andquery"
  //        case 7 => "orquery"
  //        case _ =>
  //          println ("please give the number within the range of 1 to 7")
  //          throw new InputMismatchException
  //      }
  //    }
  //    catch {
  //      case _: NumberFormatException =>
  //        println ("please give numbers without spaces")
  //      case _: NullPointerException =>
  //        println ("please don't give space while giving your option")
  //      case _: InputMismatchException => println ("Please give the correct search type")
  //    }
  //    input
  //  }
}

object LuceneSearchEngineObj extends App {
  val lucene = new LuceneSearchEngine
  println (lucene.createIndexFiles ("dataFiles",canRemoveOldIndex = true))
  //  println (lucene.searchIndex("aasir","termquery"))
  //  println("term query")
  //  println("*"*50)
  //  println (lucene.searchIndex("tum*","wildcard Query"))
  //  println("wildcard query")
  //  println("*"*50)
  //  println (lucene.searchIndex("Hea","prefix Query"))
  //  println("prefix query")
  println("*"*50)
  println (lucene.searchIndex("love","fuzzy Query"))
  println("fuzzy query")
  println("*"*50)
  //  println (lucene.searchIndex("heart~pain","phrase Query"))
  //  println("phrase query")
  //  println("*"*50)
  //  println (lucene.searchIndex("Head@@Heart","and Query"))
  //  println("and query")
  //  println("*"*50)
  //  println (lucene.searchIndex("leg##stomach","or Query"))
}



