package com.johnsnowlabs.storage

import org.apache.spark.ml.param.{BooleanParam, Param, Params}
import org.apache.spark.sql.Dataset

trait HasStorage[A] extends Params with AutoCloseable{

  val includeStorage = new BooleanParam(this, "includeStorage", "whether or not to save indexed storage along this annotator")
  val storageRef = new Param[String](this, "storageRef", "unique reference name for identification")

  @transient private var preloadedConnection: RocksDBConnection = _

  setDefault(includeStorage, true)

  def storageIsReady: Boolean = Option(preloadedConnection).isDefined
  def setStorage(storage: RocksDBConnection): this.type = {
    if (Option(preloadedConnection).isEmpty) preloadedConnection = storage
    this
  }

  def setIncludeStorage(value: Boolean): this.type = set(includeStorage, value)
  def getIncludeStorage: Boolean = $(includeStorage)

  def setStorageRef(value: String): this.type = {
    if (get(storageRef).nonEmpty)
      throw new UnsupportedOperationException(s"Cannot override storage ref on $this. " +
        s"Please re-use current ref: $getStorageRef")
    set(this.storageRef, value)
  }
  def getStorageRef: String = $(storageRef)

  def validateStorageRef(dataset: Dataset[_], inputCols: Array[String], annotatorType: String): Unit = {
    val storageCol = dataset.schema.fields
      .find(f => inputCols.contains(f.name) && f.metadata.getString("annotatorType") == annotatorType)
      .getOrElse(throw new Exception(s"Could not find a valid storage column, make sure the storage is loaded " +
        s"and has the following ref: ${$(storageRef)}")).name

    val storage_meta = dataset.select(storageCol).schema.fields.head.metadata

    require(storage_meta.contains("ref"), "Cannot find storage ref in column schema metadata")

    require(storage_meta.getString("ref") == $(storageRef),
      s"Found storage column, but ref does not match to the ref this model was trained with. " +
        s"Make sure you are using the right storage in your pipeline, with ref: ${$(storageRef)}")
  }

  protected override def close(): Unit = {
    Option(preloadedConnection).foreach(_.close())
  }

  protected def setAndGetStorageConnection: RocksDBConnection = {
    if (Option(preloadedConnection).isDefined && preloadedConnection.getFileName == $(storageRef))
      preloadedConnection
    else {
      close()
      preloadedConnection = new RocksDBConnection($(storageRef))
      preloadedConnection
    }
  }

}
