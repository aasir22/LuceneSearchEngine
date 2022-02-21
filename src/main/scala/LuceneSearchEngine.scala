import java.io.File

class LuceneSearchEngine extends Indexer {
  /**
   * get the directory of data
   * @param filePath path of the directory
   */
  def getDataDirectory(filePath:String): Unit ={
    val logger = new Logger
    logger.logWritter("info","Entered in to getDataDirectory function in LuceneSearchEngine class")
    logger.startTime()
    val dataFile = new File(filePath)
    if(checkFiles(dataFile)){
      indexString(dataFile)
    }
    else {
      logger.logWritter("error","Please check the file path")
    }
    logger.stopTime()
    logger.logWritter("info","Execution time for getDataDirectory is  " + logger.getTime)
    logger.logWritter("info","Exiting from getDataDirectory function in LuceneSearchEngine class")
  }

  /**
   * check the file permission and the file exists or not
   * @param file as File
   * @return true or false
   */
  private def checkFiles(file:File):Boolean={
    val logger = new Logger
    logger.logWritter("info","Entered in to checkFiles function in LuceneSearchEngine class")
    logger.startTime()
    if(!file.isHidden && file.exists() && file.canRead){
      logger.stopTime()
      logger.logWritter("info","Execution time for checkFiles is  " + logger.getTime)
      logger.logWritter("info","Exiting from checkFiles function in LuceneSearchEngine class")
      true
    }
    else {
      logger.stopTime()
      logger.logWritter("info","Execution time for checkFiles is  " + logger.getTime)
      logger.logWritter("info","Exiting from checkFiles function in LuceneSearchEngine class")
      false
    }

  }
}
