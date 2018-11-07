package com.johnsnowlabs.nlp.annotators.common
import com.johnsnowlabs.nlp.{Annotation, AnnotatorType}


object LabeledDependency extends Annotated[Conll2009Sentence] {

  val ROOT_HEAD: Int = -1
  val ROOT_INDEX: Int = 1

  override def annotatorType: String = AnnotatorType.POS

  override def unpack(annotations: Seq[Annotation]): Seq[Conll2009Sentence] = {
    val posTagged = annotations.filter(_.annotatorType == annotatorType).toArray
    val tokens = annotations.filter(_.annotatorType == AnnotatorType.TOKEN).toArray
    val unlabeledDependencies = annotations.filter(_.annotatorType == AnnotatorType.DEPENDENCY).toArray

    val conll2009 = unlabeledDependencies.zipWithIndex.map{ case(unlabeledDependency, index) =>
      val form = unlabeledDependency.result
      val lemma = unlabeledDependency.result.toLowerCase
      val pos = posTagged(index).result
      val head = unlabeledDependency.metadata.getOrElse("head", "-1").toInt
      val sentence = tokens(index).metadata.getOrElse("sentence", "0").toInt
      Conll2009Sentence(form, lemma, pos, "_", head, sentence, unlabeledDependency.begin, unlabeledDependency.end)
    }

   conll2009
  }

  override def pack(conll2009Sentences: Seq[Conll2009Sentence]): Seq[Annotation] = {

    val root = conll2009Sentences.last
    val arrangedSentences = moveToFront(root, conll2009Sentences.toList)

    val annotations = arrangedSentences.map{conll2009Sentence =>
      val head = conll2009Sentence.head
      if (head != ROOT_HEAD) {
        val headWord = conll2009Sentences(head).form
        val label = conll2009Sentence.deprel+s"($headWord, ${conll2009Sentence.form})"
        Annotation(AnnotatorType.LABELED_DEPENDENCY, conll2009Sentence.begin, conll2009Sentence.end,
          label, Map(conll2009Sentence.form -> headWord))
      }
    }
    annotations.drop(ROOT_INDEX).asInstanceOf[Seq[Annotation]]
  }

  def moveToFront[A](y: A, xs: List[A]): List[A] = {
    xs.span(_ != y) match {
      case (as, h::bs) => h :: as ++ bs
      case _           => xs
    }
  }

}
