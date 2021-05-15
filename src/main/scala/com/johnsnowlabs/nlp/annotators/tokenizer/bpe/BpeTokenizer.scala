/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.johnsnowlabs.nlp.annotators.tokenizer.bpe

import com.johnsnowlabs.nlp.annotators.common.{IndexedToken, Sentence, TokenPiece}

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
 * A BPE Tokenizer based on GPT2's tokenization scheme.
 * The tokenization can then be used for models based on this scheme (e.g. GPT2, roBERTa, DeBERTa)
 * TODO: truncation assumed?
 */
private[nlp] abstract class BpeTokenizer(
                                          merges: Map[(String, String), Int],
                                          vocab: Map[String, Int],
                                          specialTokens: SpecialTokens
                                        ) {

  protected val bpeRanks: Map[(String, String), Int] = {
    merges
  }

  /**
   * Rankings for the byte pairs. Derived from merges.txt
   */
  protected def getBpeRanking: ((String, String)) => Int =
    (bytePair: (String, String)) => bpeRanks.getOrElse(bytePair, Integer.MAX_VALUE)

  /**
   * cache for already encoded tokens
   */
  protected val cache: mutable.Map[String, Array[String]] = mutable.Map()

  /**
   * Create a sequence of byte-pairs of the word
   */
  protected def getBytePairs(word: Array[String]): Set[(String, String)] = {
    val createPairs = (i: Int) => (word(i), word(i + 1))
    (0 until (word.length - 1)).map(createPairs).toSet
  }

  /**
   * Do the BPE algorithm. Goal is to find the token as the largest words in the known vocabulary.
   * If not possible, the word is split into smaller subwords, until they are known.
   *
   * @return Array of TokenPieces, corresponding to encoded token
   */
  protected def bpe(
                     indToken: IndexedToken,
                     preProcess: String => String = (s: String) => s
                   ): Array[TokenPiece] = {
    val processedToken = preProcess(indToken.token)

    var word: Array[String] = Array[String]()
    // split the word into characters, to be combined into subwords
    word = processedToken.map(_.toString).toArray
    var pairs: Set[(String, String)] = getBytePairs(word)
    if (pairs.isEmpty) word = Array(processedToken) // TODO: check if correct
    else {
      // get highest priority byte-pair first
      var bytePair: (String, String) =
        pairs.toArray.sortWith(getBpeRanking(_) < getBpeRanking(_))(0)
      var done = false
      // while we still have byte-pairs from our vocabulary
      while (bpeRanks.contains(bytePair) && !done) {
        val (first, second) = bytePair
        val newWord: ListBuffer[String] = ListBuffer()
        var i = 0
        var j = 0
        // keep combining characters with the current byte-pair
        while ((i < word.length) && (j != -1)) {
          j = word.indexOf(first, i)
          if (j == -1) newWord ++= word.drop(i)
          else {
            newWord ++= word.slice(i, j)
            i = j
            val bpIsAtIndex =
              (word(i) == first) && (i < word.length - 1) && word(
                i + 1
              ) == second
            if (bpIsAtIndex) {
              newWord += (first + second)
              i += 2
            } else {
              newWord += word(i)
              i += 1
            }
          }
        }
        word = newWord.toArray
        // if we were able to create a whole word that was in the vocabulary, we're done
        if (word.length == 1) {
          done = true
        } else {
          // do it again with the next byte-pair
          pairs = getBytePairs(word)
          bytePair = pairs.toArray.sortWith(getBpeRanking(_) < getBpeRanking(_))(0)
        }
      }
    }

    var currentIndex = indToken.begin
    val wordIndexes = word.map((subWord: String) => {
      val startIndex = currentIndex
      currentIndex = startIndex + subWord.length
      (startIndex, startIndex + subWord.length)
    })
    val result = word
      .zip(wordIndexes)
      .map {
        case (subWord: String, indexes: (Int, Int)) =>
          val isWordStart = indToken.begin == indexes._1
          val subWordId: Int = if (subWord(0) != 'Ġ' && isWordStart)
            vocab.getOrElse("Ġ" + subWord, specialTokens.unk.id) // TODO do this for non roberta case
          else vocab.getOrElse(subWord, specialTokens.unk.id) // Set unknown id

          TokenPiece(subWord, processedToken, subWordId, isWordStart, indexes._1, indexes._2 - 1)
      }
    result
  }

  /**
   * Split the the individual sub texts on special tokens, e.g. masking etc.
   */
  protected def splitOnSpecialToken(
                                     specialToken: SpecialToken,
                                     text: String
                                   ): ListBuffer[String] = {
    val isControl = (c: Char) => {
      if (c == '\t' || c == '\n' || c == '\r') false // count as whitespace
      else c.isControl
    }
    val isPunctuation =
      (c: Char) => raw"""[^[:alnum:]]""".r.findFirstIn(c.toString).isDefined
    val isWordBorder =
      (c: Char) => isControl(c) || isPunctuation(c) || c.isWhitespace

    val isEndOfWord = (text: String) => isWordBorder(text.last)
    val isStartOfWord = (text: String) => isWordBorder(text.head)

    val result: ListBuffer[String] = ListBuffer()
    val tok = specialToken.content
    val splitText = text.split(tok)
    var fullWord = ""
    //    val boolProperty = (property: Map[String, Any], key: String) => property(key).asInstanceOf[Boolean]

    for ((subText, i) <- splitText.zipWithIndex) {
      var done = false
      // Try to avoid splitting on token
      if (specialToken.singleWord) {
        if (
          (i < (splitText.length - 1)) && !isEndOfWord(
            subText
          ) && !isStartOfWord(splitText(i + 1))
        ) fullWord += subText + tok
        else if (fullWord.nonEmpty) {
          fullWord += subText
          result += fullWord
          fullWord = ""
          done = true
        }
      }
      if (!done) {
        // A bit counter-intuitive but we strip the left of the string
        // since rstrip means the special token is eating all white spaces on its right
        var subTextProcessed: String = subText
        if (specialToken.rstrip && i > 0)
          subTextProcessed = subText.stripPrefix(" ")
        if (specialToken.lstrip && i < (splitText.length - 1))
          subTextProcessed = subText.stripSuffix(" ")
        if (i == 0 && subTextProcessed.isEmpty)
          result += tok
        else if (i == (splitText.length - 1)) {
          if (subTextProcessed.nonEmpty) result += subTextProcessed
        } else {
          if (subTextProcessed.nonEmpty) result += subTextProcessed
          result += tok
        }
      }
    }
    result
  }

  def tokenize(sentence: Sentence): Array[IndexedToken]

  def encode(indToken: IndexedToken): Array[TokenPiece]

  def encode(indTokens: Array[IndexedToken]): Array[TokenPiece] = indTokens.flatMap(encode(_))
}

object BpeTokenizer {
  def forModel(
                modelType: String,
                merges: Map[(String, String), Int],
                vocab: Map[String, Int],
                padWithSentenceTokens: Boolean,
                specialTokens: Option[SpecialTokens] = None
              ): BpeTokenizer = {
    val availableModels = Array("roberta")
    require(availableModels.contains(modelType), "Model type \"" + modelType + "\" not supported yet.")

    modelType match {
      case "roberta" =>
        val robertaSpecialTokens = specialTokens match {
          case Some(specialTok) => specialTok
          case None => SpecialTokens(vocab, "<s>", "</s>", "<unk>", "<mask>", "<pad>")
        }
        new RobertaTokenizer(merges, vocab, robertaSpecialTokens, padWithSentenceTokens)
      //      case "xlm" => new XlmTokenizer(merges, vocab, padWithSentenceTokens)
    }
  }
}
