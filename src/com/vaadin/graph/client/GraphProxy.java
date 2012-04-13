/*
 * Copyright 2011 Vaadin Ltd.
 *
 * Licensed under the GNU Affero General Public License, Version 3.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/agpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.graph.client;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.vaadin.terminal.gwt.client.VConsole;

/**
 * Data structure consisting of nodes with relationships between them.
 * 
 * @author Marlon Richert @ <a href="http://vaadin.com/">Vaadin</a>
 */
public class GraphProxy {

    private final Map<String, NodeProxy> vertices = new HashMap<String, NodeProxy>();
    private final Map<String, ArcProxy> edges = new HashMap<String, ArcProxy>();
    private final Map<NodeProxy, Set<ArcProxy>> inEdgeSets = new HashMap<NodeProxy, Set<ArcProxy>>();
    private final Map<NodeProxy, Set<ArcProxy>> outEdgeSets = new HashMap<NodeProxy, Set<ArcProxy>>();
    private final Map<ArcProxy, NodeProxy> sourceVertices = new HashMap<ArcProxy, NodeProxy>();
    private final Map<ArcProxy, NodeProxy> destVertices = new HashMap<ArcProxy, NodeProxy>();

    public boolean addEdge(ArcProxy e, NodeProxy source, NodeProxy dest) {
        if (edges.containsKey(e.id)) {
            return false;
        }
        edges.put(e.id, e);
        inEdgeSets.get(dest).add(e);
        outEdgeSets.get(source).add(e);
        sourceVertices.put(e, source);
        destVertices.put(e, dest);
        return true;
    }

    public boolean addVertex(NodeProxy v) {
        if (vertices.containsKey(v.id)) {
            return false;
        }
        vertices.put(v.id, v);
        inEdgeSets.put(v, new HashSet<ArcProxy>());
        outEdgeSets.put(v, new HashSet<ArcProxy>());
        return true;
    }

    public boolean containsEdge(String id) {
        return edges.containsKey(id);
    }

    public boolean containsVertex(String id) {
        return vertices.containsKey(id);
    }

    public int degree(NodeProxy v) {
        int degree = 0;
        if (inEdgeSets.containsKey(v)) {
            degree += inEdgeSets.get(v).size();
        }
        if (outEdgeSets.containsKey(v)) {
            degree += outEdgeSets.get(v).size();
        }
        return degree;
    }

    public NodeProxy getDest(ArcProxy e) {
        return destVertices.get(e);
    }

    public ArcProxy getEdge(String id) {
        return edges.get(id);
    }

    public Collection<ArcProxy> getInEdges(NodeProxy v) {
        Set<ArcProxy> set = inEdgeSets.get(v);
        if (set == null) {
            set = new HashSet<ArcProxy>();
        }
        return Collections.unmodifiableCollection(set);
    }

    public Collection<NodeProxy> getNeighbors(NodeProxy node) {
        Set<NodeProxy> neighbors = new HashSet<NodeProxy>();
        if (inEdgeSets.containsKey(node)) {
            for (ArcProxy e : inEdgeSets.get(node)) {
                neighbors.add(getSource(e));
            }
        }
        if (outEdgeSets.containsKey(node)) {
            for (ArcProxy e : outEdgeSets.get(node)) {
                neighbors.add(getDest(e));
            }
        }
        return neighbors;
    }

    public Collection<ArcProxy> getOutEdges(NodeProxy v) {
        Set<ArcProxy> set = outEdgeSets.get(v);
        if (set == null) {
            set = new HashSet<ArcProxy>();
        }
        return Collections.unmodifiableCollection(set);
    }

    public NodeProxy getSource(ArcProxy e) {
        return sourceVertices.get(e);
    }

    public NodeProxy getVertex(String id) {
        return vertices.get(id);
    }

    public Collection<NodeProxy> getVertices() {
        return Collections.unmodifiableCollection(vertices.values());
    }

    public void removeEdge(ArcProxy e) {
        removeEdge(e.getId());
    }

    public boolean removeEdge(String id) {
        boolean success = edges.containsKey(id);
        if (success) {
            ArcProxy e = edges.remove(id);

            VConsole.log("remove " + getSource(e).id + " " + e.getType() + " "
                    + getDest(e).id);

            outEdgeSets.get(sourceVertices.remove(e)).remove(e);
            inEdgeSets.get(destVertices.remove(e)).remove(e);
        }
        return success;
    }

    public boolean removeVertex(String id) {

        VConsole.log("removeVertex(" + id + ")");

        boolean success = vertices.containsKey(id);
        if (success) {
            NodeProxy v = vertices.remove(id);
            for (ArcProxy e : inEdgeSets.remove(v)) {
                removeEdge(e);
            }
            for (ArcProxy e : outEdgeSets.remove(v)) {
                removeEdge(e);
            }
        }
        return success;
    }

    public boolean removeVertex(NodeProxy v) {
        return removeVertex(v.getId());
    }
}