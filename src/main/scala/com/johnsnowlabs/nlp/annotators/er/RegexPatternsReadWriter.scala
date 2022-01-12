/*
 * Copyright 2017-2022 John Snow Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.johnsnowlabs.nlp.annotators.er

import com.johnsnowlabs.storage.{RocksDBConnection, StorageReadWriter}

import java.io.{ByteArrayOutputStream, ObjectOutputStream}

class RegexPatternsReadWriter(protected override val connection: RocksDBConnection)
  extends RegexPatternsReader(connection) with StorageReadWriter[Seq[String]] {

  protected def writeBufferSize: Int = 10000

  def toBytes(content: Seq[String]): Array[Byte] = {
    val stream: ByteArrayOutputStream = new ByteArrayOutputStream()
    val objectOutputStream = new ObjectOutputStream(stream)
    objectOutputStream.writeObject(content)
    objectOutputStream.close()
    stream.toByteArray
  }

}
