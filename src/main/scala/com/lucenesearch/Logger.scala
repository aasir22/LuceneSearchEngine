package com.lucenesearch

import com.typesafe.scalalogging
import com.typesafe.scalalogging.Logger
import org.apache.commons.lang3.time.StopWatch

class Logger {

  val logger: scalalogging.Logger = Logger("log")
  val stopWatch = new StopWatch("")

  /**
   * write the given log messages
   * @param logType log Level
   * @param logMessage log message
   */
  def logWritter(logType:String, logMessage:String): Unit ={
    logType match{
      case "info" => logger.info(logMessage)
      case "error" => logger.error(logMessage)
      case "warn" => logger.warn(logMessage)
      case "trace" => logger.trace(logMessage)
      case "debug" => logger.debug(logMessage)
    }
  }

  /**
   * starts the stopwatch
   */
  def startTime():Unit={
    stopWatch.start()
  }

  /**
   * stops the stopwatch
   */
  def stopTime():Unit={
    stopWatch.stop()
  }

  /**
   * get the time between start and stop
   * @return time between start and stop
   */
  def getTime:Long={
    stopWatch.getTime()
  }
}
