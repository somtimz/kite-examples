/**
 * Copyright 2013 Cloudera Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera.cdk.examples.data;

import com.cloudera.cdk.data.Dataset;
import com.cloudera.cdk.data.DatasetDescriptor;
import com.cloudera.cdk.data.DatasetRepository;
import com.cloudera.cdk.data.DatasetWriter;
import com.cloudera.cdk.data.Formats;
import com.cloudera.cdk.data.filesystem.FileSystemDatasetRepository;
import java.net.URI;
import java.util.Random;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.generic.GenericRecordBuilder;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

/**
 * Create a dataset on the local filesystem and write some user objects to it,
 * using Avro generic records, and stored in Parquet format.
 */
public class CreateUserDatasetGenericParquet extends Configured implements Tool {

  @Override
  public int run(String[] args) throws Exception {

    // Construct a local filesystem dataset repository rooted at /tmp/data
    DatasetRepository repo = new FileSystemDatasetRepository.Builder()
        .rootDirectory(new URI("/tmp/data")).configuration(getConf()).get();

    // Create a dataset of users with the Avro schema, and Parquet format in the
    // repository
    DatasetDescriptor descriptor = new DatasetDescriptor.Builder()
        .schemaUri("resource:user.avsc")
        .format(Formats.PARQUET)
        .get();
    Dataset users = repo.create("users", descriptor);

    // Get a writer for the dataset and write some users to it
    DatasetWriter<GenericRecord> writer = users.getWriter();
    try {
      writer.open();
      String[] colors = { "green", "blue", "pink", "brown", "yellow" };
      Random rand = new Random();
      GenericRecordBuilder builder = new GenericRecordBuilder(descriptor.getSchema());
      for (int i = 0; i < 100; i++) {
        GenericRecord record = builder.set("username", "user-" + i)
            .set("creationDate", System.currentTimeMillis())
            .set("favoriteColor", colors[rand.nextInt(colors.length)]).build();
        writer.write(record);
      }
    } finally {
      writer.close();
    }

    return 0;
  }

  public static void main(String... args) throws Exception {
    int rc = ToolRunner.run(new CreateUserDatasetGenericParquet(), args);
    System.exit(rc);
  }
}
