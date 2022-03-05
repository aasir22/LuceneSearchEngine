package com.lucenesearch

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.{DirectoryReader, IndexNotFoundException}
import org.apache.lucene.search.{BooleanClause, BooleanQuery, FuzzyQuery, IndexSearcher, Matches, MatchesIterator, PhraseQuery, PrefixQuery, Query, TermQuery, TopDocs, WildcardQuery}
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.index.Term
import org.apache.lucene.util.BytesRef
import org.apache.lucene.search.uhighlight.UnifiedHighlighter
import org.apache.lucene.search.highlight.QueryTermExtractor
import java.io.{File, FileNotFoundException}
import java.nio.file.Paths
import java.util
import java.util.InputMismatchException
import scala.collection.mutable.ArrayBuffer
import scala.io.StdIn.readLine

class LuceneSearchEngine extends Indexer {

  /**
   * get the directory of data
   *
   * @param filePath  path of the directory
   * @param indexDir index directory default is indexDir
   */
  def createIndexFiles(filePath: String,indexDir:String="indexDir"): String = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to getDataDirectory function in LuceneSearchEngine class")
    logger.startTime ()
    if (filePath.trim.nonEmpty) {
      val dataFile = new File (filePath)
      if (checkFiles (dataFile)) {
        try {
          index (dataFile, indexDir)
          "indexing completed"
        }
        catch {
          case _: FileNotFoundException => "check the data"
        }
      } else {
        removeIndexDir(indexDir)
        "data files not found"
      }
    }
    else {
      removeIndexDir(indexDir)
      logger.logWritter ("error", "data not found")
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for getDataDirectory is  " + logger.getTime + " ms")
      logger.logWritter ("info", "Exiting from getDataDirectory function in LuceneSearchEngine class")
      "data files empty"
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
    if (!file.isHidden && file.exists () && file.canRead && file.listFiles().length != 0) {
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for checkFiles is  " + logger.getTime + " ms")
      logger.logWritter ("info", "Exiting from checkFiles function in LuceneSearchEngine class")
      true
    }
    else {
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for checkFiles is  " + logger.getTime + " ms")
      logger.logWritter ("error", "check the data directory")
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
  def searchIndex(queryStr: String, queryType: String,indexDir:String="indexDir"): String = {
    val logger = new Logger
    logger.logWritter ("info", "Entered in to searchIndex function in LuceneSearchEngine class")
    logger.startTime ()
    val directory = FSDirectory.open (Paths.get (indexDir))
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
      //      val searchType = getQueryType
      query = getQuery (queryType.toLowerCase, queryStr.toLowerCase)
    }
    catch {
      case _: InputMismatchException => return "Please give the correct search type"
      case _: NullPointerException => return "please check the search type you given"
    }
    var topDocs: TopDocs = null
    try {
      // search the query in the index files
      topDocs = searcher.search (query, 100)
    }
    catch {
      case _: NullPointerException => return "search query is empty"
    }
    val arr = Array("wildcard query","wildcardquery","prefixquery","prefix query","phrase query","phrasequery")
    val hits = topDocs.scoreDocs
    var searchedFiles = ""
    for (i <- 0 until hits.length) {
      val docId = hits (i).doc
      val explanation = searcher.explain (query, docId)
//      println(explanation)
      val d = searcher.doc(docId)
//      val regExplain = """content:[A-Za-z]+""".r
//      val regExQueryExtract = """content:""".r
//      val queryValues = regExplain.findAllMatchIn(explanation.toString).toArray
//      val matchedQuery = queryValues.map(x=>regExQueryExtract.replaceAllIn(x.toString(),""))
//      searchedFiles += s"${i + 1}." + d.get ("fileName") + " Score :" + hits (i).score + " " + matchedQuery.mkString ("Array(", ", ", ")") + "\n"

      if(arr.contains(queryType)){
        val analyzer = new StandardAnalyzer ()
        val highlighter = new UnifiedHighlighter(searcher, analyzer)
        val fragments = highlighter.highlight("content",query,topDocs)
        val arrBuffer = new ArrayBuffer[String]
        val regExOfFragments = """<[/b]+>""".r
        for (f <- fragments) {
          arrBuffer.addOne(regExOfFragments.replaceAllIn(f,"").toArray.mkString)
        }
        searchedFiles += s"${i + 1}." + d.get ("fileName") + " Score :" + hits (i).score + " " + arrBuffer.distinct.toArray.mkString ("Array(", ", ", ")") + "\n"
      }
      else {
        val regExplain = """content:[A-Za-z]+""".r
        val regExQueryExtract = """content:""".r
        val queryValues = regExplain.findAllMatchIn(explanation.toString).toArray
        println(queryValues.mkString(" "))
        val matchedQuery = queryValues.map(x=>regExQueryExtract.replaceAllIn(x.toString(),""))
        searchedFiles += s"${i + 1}." + d.get ("fileName") + " Score :" + hits (i).score + " " + matchedQuery.mkString ("Array(", ", ", ")") + "\n"
      }
    }
    logger.stopTime ()
    logger.logWritter ("info", "Execution time for searchIndex is  " + logger.getTime + " ms")
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
   *
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
      case "termquery" | "term query" =>
        // search for the term in the file
        val term = new Term ("content", queryStr)
        query = new TermQuery (term)
      case "wildcardquery" | "wildcard query" =>
        // wildcard characters are *,?
        val term = new Term ("content", queryStr)
        query = new WildcardQuery(term)
      case "prefixquery" | "prefix query" =>
        // searches the query which matches the given prefix
        val term = new Term ("content", queryStr)
        query = new PrefixQuery (term)
      case "phrasequery" | "phrase query" =>
        val queryArr = queryStr.split ("~")
        if (queryArr.length > 1) {
          query = new PhraseQuery (1, "content", new BytesRef (queryArr (0)), new BytesRef (queryArr (1)))
        }
      case "fuzzyquery" | "fuzzy query" =>
        // search based on the similarities
        val term = new Term ("content", queryStr)
        query = new FuzzyQuery (term, 2, 0, 3, true)
      case "andquery" | "and query" =>
        // search query based on the two or more quries must present in the file like AND operator
        val queryArr = queryStr.split ("@@")
        query = getAndOrQuery (queryArr, isAndQuery = true)
      case "orquery" | "or query" =>
        // search any one of query presents in the file
        val queryArr = queryStr.split ("##")
        query = getAndOrQuery (queryArr)
      case _ => throw new InputMismatchException ()
    }
    logger.stopTime ()
    logger.logWritter ("info", "Execution time for getQuery is  " + logger.getTime + " ms")
    logger.logWritter ("info", "Exiting from getQuery function in LuceneSearchEngine class")
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
    logger.logWritter ("info", "Entered in to getAndOrQuery function in LuceneSearchEngine class")
    logger.startTime ()
    val builder = new BooleanQuery.Builder ()
    for (i <- queryArr.indices) {
      val term = new Term ("content", queryArr (i).toLowerCase)
      val query = new TermQuery (term)
      if (isAndQuery) {
        builder.add (query, BooleanClause.Occur.MUST)
      }
      else {
        builder.add (query, BooleanClause.Occur.SHOULD)
      }

    }
    val query: Query = builder.build ()
    logger.stopTime ()
    logger.logWritter ("info", "Execution time for getAndOrQuery is  " + logger.getTime + " ms")
    logger.logWritter ("info", "Exiting from getAndOrQuery function in LuceneSearchEngine class")
    query
  }
}

object LuceneSearchEngineObj extends App {
  val lucene = new LuceneSearchEngine
  var trueOrFalse = true
  while (trueOrFalse) {
    println ("Do you want Search only or Index the data and Search\n1.Index and search\n2.Search\n3.Exit")
    val userInput = readLine ("select any of the given items:\n")
    userInput.trim match {
      case "1" =>
        val dataPath = readLine ("data file path: ")
        val query = readLine ("please give the query: ")
        val queryTpe = readLine ("please give the query type: ")
        lucene.createIndexFiles (dataPath)
        println (lucene.searchIndex (query, queryTpe))
      case "2" =>
        val indexDirectory = readLine("please give the index file or use the Existing directory\n1.New Indexed Directory\n2.Use Existing Directory\n") match {
          case "1" =>
            readLine("Please give the indexed files directory: ")
          case "2" => "indexDir"
          case _ => ""
        }
        val query = readLine ("please give the query: ")
        val queryTpe = readLine ("please give the query type: ")
        println (lucene.searchIndex (query, queryTpe,indexDirectory.trim))
      case "3" =>
        trueOrFalse = false
      case _ => println ("please select any of the given items")
    }
  }
}






