/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.metadata.solr.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.StringUtils;

public class SolrBenchmark extends Thread {

    private static final String SOLR_SERVER_URL = "http://localhost:8983/solr/registry-indexing";
    private static SolrClient solr;
    private static int numberOfMessages;
    private static String content;

    SolrBenchmark() {
        if (solr == null) {
            solr = new HttpSolrClient.Builder(SOLR_SERVER_URL).build();
        }
    }

    /**
     * Creating the solr input document
     *
     * @param content document content
     * @param type    document type
     * @return {@link SolrInputDocument}
     */
    private SolrInputDocument createSolrInputDocument(String content, String type) {
        SolrInputDocument document = new SolrInputDocument();
        document.addField("id", UUID.randomUUID().toString());
        document.addField("docType_s", type);
        document.addField("thread_id_s", Thread.currentThread().getId());
        if (content != null) {
            document.addField("text", content);
        } else {
            for (int i = 0; i < 100; ++i) {
                document.addField(RandomStringUtils.random(10, true, false) + "_s",
                        RandomStringUtils.random(20, true, false));
            }
        }
        return document;
    }

    public static void main(String[] args) {
        String contentPath = StringUtils.isEmpty(args[0]) ? "src/main/resources/payload.txt" : args[0];
        numberOfMessages = StringUtils.isEmpty(args[1]) ? 1000 : Integer.parseInt(args[1]);
        int numberOfThreads = StringUtils.isEmpty(args[2]) ? 1 : Integer.parseInt(args[2]);

        try {
            File file = new File(contentPath);
            content = new String(Files.readAllBytes(Paths.get(contentPath)));

            SolrBenchmark[] array = new SolrBenchmark[numberOfThreads];
            long start = System.currentTimeMillis();
            for (int i = 0; i < numberOfThreads; ++i) {
                array[i] = new SolrBenchmark();
                array[i].start();
            }
            for (int i = 0; i < numberOfThreads; ++i) {
                array[i].join();
            }
            long end = System.currentTimeMillis();
            System.out.println("-------- Results ----------");
            System.out.println("Content Size: " + file.length() / 1024 + "KiB");
            System.out.println("Number of documents: " + numberOfMessages * numberOfThreads);
            System.out.println("Elapsed Time (ms): " + (end - start));
            System.out.println(
                    "Elapsed Time/Transaction (ms): " + (double) (end - start) / (numberOfMessages * numberOfThreads));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform solr add (Soft Commit)
     *
     * @throws IOException
     * @throws SolrServerException
     */
    private void softCommitDocument() throws IOException, SolrServerException {
        int count = numberOfMessages;

        while (count-- > 0) {
            SolrInputDocument document = createSolrInputDocument(content, "softCommit");
            solr.add(document);
        }

    }

    /**
     * Perform solr commit
     *
     * @throws IOException
     * @throws SolrServerException
     */
    private void hardCommitDocument() throws IOException, SolrServerException {
        int count = numberOfMessages;
        while (count-- > 0) {
            SolrInputDocument document = createSolrInputDocument(content, "hardCommit");
            solr.add(document);
            solr.commit();
        }
    }

    @Override
    public void run() {
        SolrBenchmark solrTest = new SolrBenchmark();
        try {
            solrTest.softCommitDocument();
            solrTest.hardCommitDocument();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }

    }
}

