package com.johnsnowlabs.nlp.eval

import com.johnsnowlabs.nlp.annotator.NorvigSweetingApproach
import org.scalatest.FlatSpec

class NorvigSpellEvalTestSpec extends FlatSpec {

  "A Norvig Spell Evaluation" should "display accuracy results" in {

    val trainFile = "./coca2017.txt"
    val dictionaryFile = "src/test/resources/spell/words.txt"
    val testFile = "./misspell.txt"
    val groundTruthFile = "./ground_truth.txt"

    val spell = new NorvigSweetingApproach()
      .setInputCols(Array("token"))
      .setOutputCol("spell")
      .setDictionary(dictionaryFile)

    NorvigSpellEvaluation(trainFile, spell, testFile, groundTruthFile)
  }

}
