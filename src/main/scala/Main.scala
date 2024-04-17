package org.tss.application

import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.functions.lag
import play.api.libs.json.Json

import scala.io.Source

//TODO add logging instead of printing

object Main extends sparkInterface {
  import spark.implicits._

  private def readApiAmount(url: String): Double = {
    val source = Source.fromURL(url)
    val jsonParsed = Json.parse(source.mkString)
    source.close()
    val result = (jsonParsed \ "data" \ "amount").as[String].toDouble
    result
//    None => throw new RuntimeException("Error parsing JSON or missing 'amount'")
  }

  private def createEmptyDataFrame(): DataFrame = {
    spark.createDataFrame(
      Seq.empty[(Long, Double)]
    ).toDF("timestamp", "price")
  }

  private def computeMovingAverage(df: DataFrame, windowSize: Int): DataFrame = {
    val shortWindowSize = windowSize
    val shortWindowSpec = Window.orderBy("timestamp").rowsBetween(-(shortWindowSize - 1), 0)

    val longWindowSize = (windowSize + windowSize * 0.5).toInt
    val longWindowSpec = Window.orderBy("timestamp").rowsBetween(-(longWindowSize - 1), 0)
    var movingAverageDF = df
      .withColumn(s"ma_$shortWindowSize", avg($"price").over(shortWindowSpec))
      .withColumn(s"ma_$longWindowSize", avg($"price").over(longWindowSpec))
    val lagWindow = Window.orderBy("timestamp").rowsBetween(-1, -1)

    movingAverageDF = movingAverageDF
      .withColumn("signal", when(col(s"ma_$shortWindowSize") > col(s"ma_$longWindowSize"), 1).otherwise(0))
      .withColumn("position", $"signal" - coalesce(lag($"signal", 1).over(lagWindow), lit(0)))
      .withColumn("decision", when($"position" === -1, "BUY")
        .when($"position" === -1, "SELL").otherwise(null))
    movingAverageDF
  }

  private def add_technical_columns(df: DataFrame): DataFrame = {
    df.withColumn("id", monotonically_increasing_id() + 1)
      .withColumn("uuid", uuid())
      .withColumn("load_timestamp", current_timestamp())
  }

  def main(args: Array[String]): Unit = {

    val url: String = "https://api.coinbase.com/v2/prices/btc-usd/spot"
    var count: Int = 0
    val windowSize: Int = 5
    var df: DataFrame = createEmptyDataFrame()

    while(true) {
      val acurrentAmount: Double = readApiAmount(url)
//      println(s"Wartosc BTC w USD to: $acurrentAmount")
      val currentTimestamp = System.currentTimeMillis()
      val dfRow = spark.createDataFrame(Seq((currentTimestamp, acurrentAmount))).toDF("timestamp", "price")
      df = df.union(dfRow)
      if (count >= windowSize) {
        val dfMA = computeMovingAverage(df, windowSize)
        dfMA.show()
        count = 0
      }
      Thread.sleep(3000)
      count += 1
    }
  }
}