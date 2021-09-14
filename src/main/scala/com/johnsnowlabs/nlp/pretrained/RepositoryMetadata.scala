/*
 * Copyright 2017-2021 John Snow Labs
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

package com.johnsnowlabs.nlp.pretrained

import java.sql.Timestamp

/**
  * Describes state of repository
  * Repository could be any s3 folder that has metadata.json describing list of resources inside
  */
case class RepositoryMetadata
(
  // Path to repository metadata file
  metadataFile: String,
  // Path to repository folder
  repoFolder: String,
  // Aws file metadata.json version
  version: String,
  // Last time metadata was downloaded
  lastMetadataDownloaded: Timestamp,
  // List of all available resources in repository
  metadata: List[ResourceMetadata]
)