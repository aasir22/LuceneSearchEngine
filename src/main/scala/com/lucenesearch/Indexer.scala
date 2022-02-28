package com.lucenesearch

import org.apache.commons.io.FileUtils
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.{Document, Field, StoredField, StringField, TextField}
import org.apache.lucene.index.{IndexWriter, IndexWriterConfig}
import org.apache.lucene.store.FSDirectory

import java.io.{File, FileNotFoundException, FileReader}
import java.nio.file.{Files, Paths}
import scala.io.Source

class Indexer {
  protected final val INDEXDIRECTORYPATH = "indexDir"

  /**
   * Indexing the files in the given directory
   * @param dataDir filePath
   * @param canRemoveIndex remove index file or not
   */
  protected def index(dataDir:File,canRemoveIndex:Boolean): Unit ={
    val logger = new Logger
    logger.logWritter("info","Entered in to indexString function in com.lucenesearch.Indexer class")
    if(canRemoveIndex) {
      logger.startTime ()
      // removes the index directory
      removeIndexDir ()
      // create a directory in the given path
      val indexDir = Files.createDirectory (Paths.get (INDEXDIRECTORYPATH))
      // removes all the stop words
      val analyzer = new StandardAnalyzer ()
      // contains all the configuration that is used to create Index Writer
      // create a new config with the given analyzer
      val config = new IndexWriterConfig (analyzer)
      // creates indexes during indexing process.
      val indexWriter = new IndexWriter (FSDirectory.open (indexDir), config)
      // list of all files name
      val files = dataDir.listFiles ()
      try {
        for (f <- files) {
          val openFile = Source.fromFile(f)
          //          val fileContents = openFile.getLines
          // virtual documents with Fields
          // Fields is an object contains the content from the physical document
          val doc = new Document ()
          //          for(lines <- fileContents){
          //            lines.split(" ").toList.foreach(v => doc.add(new Field("content",v,TextField.TYPE_STORED)))
          //            doc.add (new Field ("content",lines,TextField.TYPE_STORED))
          ////            splitted = splitted.tail
          //          }
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
    logger.logWritter("info","search will use already presented index file")
  }

  /**
   * removes the indexDir if its exists
   */
  protected def removeIndexDir(): Unit ={
    val logger = new Logger
    logger.logWritter("info","Entered in to removeIndexDir function in com.lucenesearch.Indexer class")
    logger.startTime()
    val indexFolder = new File(INDEXDIRECTORYPATH)
    if(indexFolder.exists()){
      // if index folder already exists. It will delete the directory
      FileUtils.deleteDirectory(indexFolder)
      logger.logWritter("info",s"$INDEXDIRECTORYPATH removed")
    }
    logger.stopTime()
    logger.logWritter("info","Execution time for removeIndexDir is  " + logger.getTime+ " ms")
    logger.logWritter("info","Exiting from removeIndexDir function in com.lucenesearch.Indexer class")
  }
}