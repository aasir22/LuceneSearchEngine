package com.lucenesearch

import org.apache.commons.io.FileUtils
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, StoredField, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory
import java.io.{File, FileNotFoundException, FileReader}
import java.nio.file.{Files, Paths}
import scala.io.Source
import org.apache.lucene.analysis.shingle.ShingleAnalyzerWrapper


class Indexer {

  /**
   * Indexing the files in the given directory
   * @param dataDir filePath
   */
  protected def index(dataDir:File, indexFilePath:String): Unit ={
    val logger = new Logger
    logger.logWritter("info","Entered in to indexString function in com.lucenesearch.Indexer class")
      logger.startTime ()
      // removes the index directory
      removeIndexDir(indexFilePath)
      // create a directory in the given path
      val indexDir = Files.createDirectory (Paths.get(indexFilePath))
      // removes all the stop words
      val analyzer = new StandardAnalyzer ()
      val analyze = new ShingleAnalyzerWrapper(analyzer)
      // contains all the configuration that is used to create Index Writer
      // create a new config with the given analyzer
      val config = new IndexWriterConfig (analyze)
      // creates indexes during indexing process.
      val indexWriter = new IndexWriter (FSDirectory.open (indexDir), config)
      // list of all files name
      val files = dataDir.listFiles ()
      try {
        for (f <- files) {
          val openFile = Source.fromFile(f)
          // virtual documents with Fields
          // Fields is an object contains the content from the physical document
          val doc = new Document ()
          // Field that contains text
          doc.add (new TextField("content", new FileReader(f)))
          // Stored Fields are not indexed so it can't searchable
          doc.add (new StoredField ("fileName", f.getName))
          indexWriter.addDocument (doc)
          openFile.close()
        }
      }
      catch {
        case f: FileNotFoundException => throw f
      }
      logger.logWritter ("info", "Index files created")
      indexWriter.close ()
      logger.stopTime ()
      logger.logWritter ("info", "Execution time for indexString is  " + logger.getTime + " ms")
      logger.logWritter ("info", "Exiting from indexString function in com.lucenesearch.Indexer class")
    }



  /**
   * removes the indexDir if its exists
   */
  protected def removeIndexDir(indexDir:String): Unit ={
    val logger = new Logger
    logger.logWritter("info","Entered in to removeIndexDir function in com.lucenesearch.Indexer class")
    logger.startTime()
    val indexFolder = new File(indexDir)
    if(indexFolder.exists()){
      // if index folder already exists. It will delete the directory
      FileUtils.deleteDirectory(indexFolder)
      logger.logWritter("info",s"$indexDir removed")
    }
    logger.stopTime()
    logger.logWritter("info","Execution time for removeIndexDir is  " + logger.getTime+ " ms")
    logger.logWritter("info","Exiting from removeIndexDir function in com.lucenesearch.Indexer class")
  }
}