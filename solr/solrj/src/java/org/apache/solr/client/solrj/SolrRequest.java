/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.client.solrj;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 
 *
 * @since solr 1.3
 */
public abstract class SolrRequest<T extends SolrResponse> implements Serializable {

  public enum METHOD {
    GET,
    POST,
    PUT
  };

  private METHOD method = METHOD.GET;
  private String path = null;

  private ResponseParser responseParser;
  private StreamingResponseCallback callback;
  private Set<String> queryParams;
  
  //---------------------------------------------------------
  //---------------------------------------------------------

  public SolrRequest( METHOD m, String path )
  {
    this.method = m;
    this.path = path;
  }

  //---------------------------------------------------------
  //---------------------------------------------------------

  public METHOD getMethod() {
    return method;
  }
  public void setMethod(METHOD method) {
    this.method = method;
  }

  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  /**
   *
   * @return The {@link org.apache.solr.client.solrj.ResponseParser}
   */
  public ResponseParser getResponseParser() {
    return responseParser;
  }

  /**
   * Optionally specify how the Response should be parsed.  Not all server implementations require a ResponseParser
   * to be specified.
   * @param responseParser The {@link org.apache.solr.client.solrj.ResponseParser}
   */
  public void setResponseParser(ResponseParser responseParser) {
    this.responseParser = responseParser;
  }

  public StreamingResponseCallback getStreamingResponseCallback() {
    return callback;
  }

  public void setStreamingResponseCallback(StreamingResponseCallback callback) {
    this.callback = callback;
  }

  /**
   * Parameter keys that are sent via the query string
   */
  public Set<String> getQueryParams() {
    return this.queryParams;
  }

  public void setQueryParams(Set<String> queryParams) {
    this.queryParams = queryParams;
  }

  public abstract SolrParams getParams();

  public abstract Collection<ContentStream> getContentStreams() throws IOException;

  /**
   * Create a new SolrResponse to hold the response from the server
   * @param client the {@link SolrClient} the request will be sent to
   */
  protected abstract T createResponse(SolrClient client);

  /**
   * Send this request to a {@link SolrClient} and return the response
   *
   * @param client the SolrClient to communicate with
   * @param collection the collection to execute the request against
   *
   * @return the response
   *
   * @throws SolrServerException if there is an error on the Solr server
   * @throws IOException if there is a communication error
   */
  public final T process(SolrClient client, String collection) throws SolrServerException, IOException {
    long startTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    T res = createResponse(client);
    res.setResponse(client.request(this, collection));
    long endTime = TimeUnit.MILLISECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    res.setElapsedTime(endTime - startTime);
    return res;
  }

  /**
   * Send this request to a {@link SolrClient} and return the response
   *
   * @param client the SolrClient to communicate with
   *
   * @return the response
   *
   * @throws SolrServerException if there is an error on the Solr server
   * @throws IOException if there is a communication error
   */
  public final T process(SolrClient client) throws SolrServerException, IOException {
    return process(client, null);
  }

}
