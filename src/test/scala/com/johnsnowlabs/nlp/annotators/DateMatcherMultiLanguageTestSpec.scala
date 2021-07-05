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

package com.johnsnowlabs.nlp.annotators

import com.johnsnowlabs.nlp.{Annotation, DataBuilder}
import com.johnsnowlabs.tags.FastTest
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.{Dataset, Row}
import org.scalatest._

import java.time.LocalDate
import java.time.format.DateTimeFormatter


class DateMatcherMultiLanguageTestSpec extends FlatSpec with DateMatcherBehaviors {

  /** ITALIAN **/

  "a DateMatcher" should "be catching formatted italian dates" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Sono arrivato in Italia il 15/9/2012.")

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat("MM/dd/yyyy")
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    assert(annotations.head.result == "09/15/2012")
  }

  "a DateMatcher" should "be catching unformatted italian dates" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Sono arrivato in Italia il 15 Settembre 2012.")

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat("MM/dd/yyyy")
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    assert(annotations.head.result == "09/15/2012")
  }
  "a DateMatcher" should "be catching unformatted italian language dates" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Sono arrivato in Italia il 15 Settembre 2012.")

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat("MM/dd/yyyy")
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    assert(annotations.head.result == "09/15/2012")
  }

  "a DateMatcher" should "be catching relative unformatted italian language dates yearly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Sono arrivato in Italia 2 anni fa.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusYears(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted italian language dates weekly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Sono arrivato in Italia 2 settimane fa.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusWeeks(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted italian language dates daily" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Sono arrivato in Italia 2 giorni fa.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusDays(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted italian language future dates yearly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Il prossimo anno tornerò in Italia.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusYears(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted italian language future dates monthly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Il mese prossimo tornerò in Italia.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusMonths(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted italian language future dates weekly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("La settimana prossima tornerò in Italia.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusWeeks(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted italian language future dates daily" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Domani andrò in Italia.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("it")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusDays(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  /** FRENCH **/

  "a DateMatcher" should "be catching formatted french dates" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Je suis arrivé en France le 23/5/2019.")

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat("MM/dd/yyyy")
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    assert(annotations.head.result == "05/23/2019")
  }

  "a DateMatcher" should "be catching unformatted french dates" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Je suis arrivé en France le 23 avril 2019.")

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat("MM/dd/yyyy")
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    assert(annotations.head.result == "04/23/2019")
  }

  "a DateMatcher" should "be catching unformatted french language dates" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Je suis arrivé en France le 23 février 2019.")

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat("MM/dd/yyyy")
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    assert(annotations.head.result == "02/23/2019")
  }

  "a DateMatcher" should "be catching relative unformatted french language dates yearly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Je suis arrivé en France il y a 2 ans.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusYears(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted french language dates weekly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Je suis arrivé en France il y a 2 semaines.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusWeeks(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted french language dates daily" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Je suis arrivé en France il y a 2 jours.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusDays(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted french language future dates yearly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Je retournerai en Italie l'année prochaine.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusYears(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted french language future dates monthly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Je retournerai en Italie le mois prochain.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusMonths(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted french language future dates weekly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("La semaine prochaine, je retournerai en Italie.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusWeeks(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted french language future dates daily" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Demain j'irai en France.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("fr")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusDays(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  /** PORTUGUESE **/

  "a DateMatcher" should "be catching formatted portuguese dates" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Cheguei à França no dia 23/5/2019.")

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat("MM/dd/yyyy")
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    assert(annotations.head.result == "05/23/2019")
  }

  "a DateMatcher" should "be catching unformatted portuguese dates" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Cheguei à França em 23 de maio de 2019.")

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat("MM/dd/yyyy")
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    assert(annotations.head.result == "05/23/2019")
  }

  "a DateMatcher" should "be catching relative unformatted portuguese language dates yearly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Eu cheguei na França 2 anos atrás.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusYears(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted portuguese language dates weekly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Eu cheguei na França 2 semanas atrás.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusWeeks(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted portuguese language dates daily" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Eu cheguei na França 2 dias atrás.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.minusDays(2L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted portuguese language future dates yearly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("No próximo ano, eu voltarei novamente au Portugal.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusYears(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted portuguese language future dates monthly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("No próximo mês irei novamente a Portugal.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusMonths(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted portuguese language future dates weekly" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Na próxima semana vou para portugal.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusWeeks(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }

  "a DateMatcher" should "be catching relative unformatted portuguese language future dates daily" taggedAs FastTest in {

    val data: Dataset[Row] = DataBuilder.basicDataBuild("Amanhã vou para portugal.")

    val DateFormat = "MM/dd/yyyy"

    val dateMatcher = new DateMatcher()
      .setInputCols("document")
      .setOutputCol("date")
      .setFormat(DateFormat)
      .setSourceLanguage("pt")

    val pipeline = new Pipeline().setStages(Array(dateMatcher))

    val annotated = pipeline.fit(data).transform(data)

    val annotations: Seq[Annotation] =
      Annotation.getAnnotations(
        annotated.select("date").collect().head,
        "date")

    val localDate = LocalDate.now.plusDays(1L)
    val formatter = DateTimeFormatter.ofPattern(DateFormat)
    val formattedDateString = localDate.format(formatter)

    assert(annotations.head.result == formattedDateString)
  }
}
