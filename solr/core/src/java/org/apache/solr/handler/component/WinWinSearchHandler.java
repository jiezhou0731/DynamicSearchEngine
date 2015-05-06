package org.apache.solr.handler.component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.index.ExitableDirectoryReader;
import org.apache.lucene.util.Version;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrQueryTimeoutImpl;
import org.apache.solr.util.RTimer;
import org.apache.solr.util.SolrPluginUtils;

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

public class WinWinSearchHandler extends SearchHandler {
  
  public WinWinSearchHandler() {
    super();
  }

  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp)
      throws Exception {
    ResponseBuilder rb = new ResponseBuilder(req, rsp, components);
    if (rb.requestInfo != null) {
      rb.requestInfo.setResponseBuilder(rb);
    }
    
    // User just want to clear history
    SolrParams params = req.getParams();
    if (params.get("clearHistory")!=null && params.get("clearHistory").equals("true")){
      for (SearchComponent c : components) {
        if (c instanceof WinWinQueryComponent){
         ( (WinWinQueryComponent)c).session=new HashMap<String,Object>();
        }
      }
      return;
    }
    
    for (SearchComponent c : components) {
      c.prepare(rb);
    }
    
    for (SearchComponent c : components) {
      c.process(rb);
    }
    
    for (SearchComponent c : components) {
      c.finishStage(rb);
    }
    
    if (!rb.isDistrib
        && req.getParams().getBool(ShardParams.SHARDS_INFO, false)
        && rb.shortCircuitedURL != null) {
      NamedList<Object> shardInfo = new SimpleOrderedMap<Object>();
      SimpleOrderedMap<Object> nl = new SimpleOrderedMap<Object>();
      if (rsp.getException() != null) {
        Throwable cause = rsp.getException();
        if (cause instanceof SolrServerException) {
          cause = ((SolrServerException) cause).getRootCause();
        } else {
          if (cause.getCause() != null) {
            cause = cause.getCause();
          }
        }
        nl.add("error", cause.toString());
        StringWriter trace = new StringWriter();
        cause.printStackTrace(new PrintWriter(trace));
        nl.add("trace", trace.toString());
      } else {
        nl.add("numFound", rb.getResults().docList.matches());
        nl.add("maxScore", rb.getResults().docList.maxScore());
      }
      nl.add("shardAddress", rb.shortCircuitedURL);
      nl.add("time", req.getRequestTimer().getTime()); // elapsed time of this
                                                       // request so far
      
      int pos = rb.shortCircuitedURL.indexOf("://");
      String shardInfoName = pos != -1 ? rb.shortCircuitedURL
          .substring(pos + 3) : rb.shortCircuitedURL;
      shardInfo.add(shardInfoName, nl);
      rsp.getValues().add(ShardParams.SHARDS_INFO, shardInfo);
    }
  }
  
}
