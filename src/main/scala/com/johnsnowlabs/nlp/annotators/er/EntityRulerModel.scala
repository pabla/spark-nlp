package com.johnsnowlabs.nlp.annotators.er

import com.johnsnowlabs.nlp.AnnotatorType.{CHUNK, DOCUMENT, TOKEN}
import com.johnsnowlabs.nlp.annotators.common.TokenizedWithSentence
import com.johnsnowlabs.nlp.{Annotation, AnnotatorModel, HasPretrained, HasSimpleAnnotate}
import com.johnsnowlabs.storage.Database.{ENTITY_PATTERNS, Name}
import com.johnsnowlabs.storage.{Database, HasStorageModel, RocksDBConnection, StorageReadable, StorageReader}
import org.apache.spark.ml.util.Identifiable

class EntityRulerModel(override val uid: String) extends AnnotatorModel[EntityRulerModel]
  with HasSimpleAnnotate[EntityRulerModel] with HasStorageModel {

  def this() = this(Identifiable.randomUID("ENTITY_RULER"))

  /** Annotator reference id. Used to identify elements in metadata or to refer to this annotator type */
  override val inputAnnotatorTypes: Array[String] = Array(DOCUMENT, TOKEN)
  override val outputAnnotatorType: AnnotatorType = CHUNK

  /**
   * takes a document and annotations and produces new annotations of this annotator's annotation type
   *
   * @param annotations Annotations that correspond to inputAnnotationCols generated by previous annotators if any
   * @return any number of annotations processed for every input annotation. Not necessary one to one relationship
   */
  override def annotate(annotations: Seq[Annotation]): Seq[Annotation] = {
    val patternsReader = getReader(Database.ENTITY_PATTERNS).asInstanceOf[PatternsReader]
    val tokenizedWithSentences = TokenizedWithSentence.unpack(annotations)

    val entities = tokenizedWithSentences.map{ tokenizedWithSentence =>
     tokenizedWithSentence.indexedTokens.flatMap{ indexedToken =>
       val entity = patternsReader.lookup(indexedToken.token)
       val annotation = if (entity.isDefined) {
         Some(Annotation(CHUNK, indexedToken.begin, indexedToken.end, indexedToken.token,
           Map("entity" -> entity.get, "sentence" -> tokenizedWithSentence.sentenceIndex.toString)))
       } else None
       annotation
     }
    }

    Seq()
  }

  override protected val databases: Array[Name] = EntityRulerModel.databases

  override protected def createReader(database: Name, connection: RocksDBConnection): StorageReader[_] = {
    new PatternsReader(connection)
  }
}

trait ReadablePretrainedEntityRuler extends StorageReadable[EntityRulerModel] with HasPretrained[EntityRulerModel] {

  override val databases: Array[Name] = Array(ENTITY_PATTERNS)

  override val defaultModelName: Option[String] = None

  override def pretrained(): EntityRulerModel = super.pretrained()

  override def pretrained(name: String): EntityRulerModel = super.pretrained(name)

  override def pretrained(name: String, lang: String): EntityRulerModel = super.pretrained(name, lang)

  override def pretrained(name: String, lang: String, remoteLoc: String): EntityRulerModel = super.pretrained(name, lang, remoteLoc)

}

object EntityRulerModel extends ReadablePretrainedEntityRuler