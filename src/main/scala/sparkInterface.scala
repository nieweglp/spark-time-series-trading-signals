package org.tss.application

import org.apache.spark.sql.SparkSession

trait sparkInterface {

  lazy val spark: SparkSession = SparkSession.builder
    .master("local")
    .appName("tss")
    .getOrCreate()
}
