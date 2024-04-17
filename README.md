Install spark locally to get Dockerfiles to build on kubernetes cluster

$SPARK_HOME/bin/docker-image-tool.sh -t apache-spark-k8s build

Java 11, sbt builde

spark-submit \
--master k8s://https://192.168.49.2:8443 \
--deploy-mode cluster \
--name spark-pi \
--class org.apache.spark.examples.SparkPi \
--conf spark.executor.instances=2 \
--conf spark.kubernetes.container.image=bitnami/spark:3 \
--conf spark.kubernetes.container.image.pullPolicy=IfNotPresent \
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
local:///$SPARK_HOME/spark/examples/jars/spark-examples_2.12-3.4.0.jar 100

