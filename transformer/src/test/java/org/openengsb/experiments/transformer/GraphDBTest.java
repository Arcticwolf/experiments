/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.experiments.transformer;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.orientechnologies.orient.core.db.graph.OGraphDatabase;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class GraphDBTest {

    private static OGraphDatabase graph;

    @BeforeClass
    public static void init() {
        graph = new OGraphDatabase("memory:graphdb").create();
    }

    @AfterClass
    public static void tearDown() {
        graph.close();
    }

    @Test
    public void checkIfDBNotNull_shouldNotNull() {
        assertThat(graph, notNullValue());
    }

    @Test
    public void test() {
        graph.createVertexType("Models");

        ODocument vertex1 = graph.createVertex("Models").field("class", "this.is.a.test.class");
        vertex1.save();
        ODocument vertex2 = graph.createVertex("Models").field("class", "this.is.a.test.class2");
        vertex2.save();
        ODocument vertex3 = graph.createVertex("Models").field("class", "this.is.a.test.class3");
        vertex3.save();

        ODocument vertex4 = graph.createVertex("Models").field("class", "this.is.a.test.class4");
        vertex4.save();

        ODocument edge = graph.createEdge(vertex1, vertex2);
        edge.field(OGraphDatabase.LABEL, "id1");
        edge.save();
        ODocument edge2 = graph.createEdge(vertex2, vertex3);
        edge2.save();

        ODocument edge3 = graph.createEdge(vertex2, vertex1);
        edge3.field(OGraphDatabase.LABEL, "id2");
        edge3.save();

        ODocument edge4 = graph.createEdge(vertex1, vertex4);
        edge4.field(OGraphDatabase.LABEL, "from1To4");
        edge4.save();

        List<ODocument> result =
            graph.query(new OSQLSynchQuery<ODocument>("select from Models"));
        assertThat(result.size(), is(4));
    }

    @Test
    public void test2() {
        List<ODocument> vertex1 =
            graph.query(new OSQLSynchQuery<ODocument>("select from Models where class = 'this.is.a.test.class'"));
        List<ODocument> vertex2 =
            graph.query(new OSQLSynchQuery<ODocument>("select from Models where class = 'this.is.a.test.class2'"));

        Set<ODocument> edges =
            graph.getEdgesBetweenVertexes(vertex1.get(0), vertex2.get(0), new String[]{ "id1", "id2" });
        edges.retainAll(graph.getOutEdges(vertex1.get(0)));
        assertThat(edges.size(), is(1));
        assertThat((String) ((ODocument) (edges.toArray()[0])).field(OGraphDatabase.LABEL), is("id1"));
    }

    @Test
    public void test3() {
        List<ODocument> result = getEdgesBetweenModels("this.is.a.test.class", "this.is.a.test.class2");
        assertThat((String) result.get(0).field(OGraphDatabase.LABEL), is("id1"));
        System.out.println(result);
    }

    @Test
    public void test4() {
        List<ODocument> neighbors = getNeighborsOfModel("this.is.a.test.class");
        System.out.println(neighbors);
    }

    private List<ODocument> getEdgesBetweenModels(String source, String target) {
        ODocument from = getModel(source);
        ODocument to = getModel(target);
        String query = "select from E where out = ? AND in = ?";
        return graph.query(new OSQLSynchQuery<ODocument>(query), from, to);
    }

    private List<ODocument> getNeighborsOfModel(String model) {
        ODocument from = getModel(model);
        List<ODocument> edges = graph.query(new OSQLSynchQuery<ODocument>("select from E where out = ?"), from);
        List<ODocument> result = new ArrayList<ODocument>();
        for (ODocument edge : edges) {
            result.add(graph.getInVertex(edge));
        }
        return result;
    }

    private ODocument getModel(String model) {
        String query = "select from Models where class = '%s'";
        List<ODocument> from = graph.query(new OSQLSynchQuery<ODocument>(String.format(query, model)));
        return from.get(0);
    }
}
