package com.nettakrim.souper_secret_settings.shaders.custom;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.ShaderManager;

import java.util.*;
import java.util.function.Supplier;

public abstract class Graph {
    public final List<Node> nodes = new ArrayList<>();
    public final HashMap<InputPort,Wire> wires = new HashMap<>();

    public boolean changed;

    public void addWire(Wire wire) {
        wires.put(wire.destination, wire);
    }

    public void makeChange() {
        changed = true;
    }

    protected OrganisedGraph organise() throws ShaderManager.CompilationException {
        return new OrganisedGraph(this);
    }

    // gets all the nodes into a format where they can be traversed easily (since its stored as a loose pile of nodes and wires for editing)
    // the resulting graph is ordered such that a node will *always* be before anything that uses it
    protected static class OrganisedGraph {
        public final ImmutableList<OrganisedNode> organisedNodes;

        private OrganisedGraph(Graph graph) throws ShaderManager.CompilationException {
            ArrayList<OrganisedNode> backwardsNodes = new ArrayList<>(graph.nodes.size());

            // find all ends of the graph to make sure every relevant bit is visited, but excess nodes arent
            // this does mean weird fragments with a separate input/output chain will be included, which are probably often incorrect. but thats fine
            for (Node node : graph.nodes) {
                if (node.isEnd()) {
                    AddRoot(node, backwardsNodes, graph.wires);
                }
            }

            this.organisedNodes = ImmutableList.copyOf(backwardsNodes.reversed());
        }

        public void AddRoot(Node node, ArrayList<OrganisedNode> backwardsNodes, HashMap<InputPort,Wire> wires) throws ShaderManager.CompilationException {
            ArrayList<OrganisedNode> block = new ArrayList<>();
            int index = AddNode(node, backwardsNodes, block, wires);

            // insert block before its earliest source, so that the list *always* places nodes before things they need to use (this is flipped later)
            if (index == Integer.MAX_VALUE) {
                index = 0;
            }
            backwardsNodes.addAll(index, block);
        }

        private int AddNode(Node node, ArrayList<OrganisedNode> backwardsNodes, List<OrganisedNode> block, HashMap<InputPort,Wire> wires) throws ShaderManager.CompilationException {
            // dont allow loops
            for (OrganisedNode entry : block) {
                if (entry.node == node) {
                    throw new ShaderManager.CompilationException("Loop in graph");
                }
            }

            // but allow connections from previous roots
            for (int i = 0; i < backwardsNodes.size(); i++) {
                if (backwardsNodes.get(i).node == node) {
                    return i;
                }
            }

            OrganisedNode organisedNode = new OrganisedNode(node, wires);
            block.add(organisedNode);

            // recursively add all sources of the node
            // it is desirable that this is depth first, since it means chains are likely to be continuous in memory
            int max = Integer.MAX_VALUE;
            for (Source source : organisedNode.inputSources) {
                if (source == null) {
                    continue;
                }

                int current = AddNode(source.node, backwardsNodes, block, wires);
                // find the earliest dependency
                if (current < max) {
                    max = current;
                }
            }

            return max;
        }
    }

    public static class OrganisedNode {
        public final Node node;
        public final Source[] inputSources;

        public OrganisedNode(Node node, HashMap<InputPort,Wire> wires) throws ShaderManager.CompilationException {
            this(node, new Source[node.inputPorts.size()]);

            for (int i = 0; i < inputSources.length; i++) {
                InputPort port = node.inputPorts.get(i);

                // prioritise wire connection over docked connection
                Wire wire = wires.get(port);
                if (wire == null) {
                    inputSources[i] = new Source(port.docked, 0);
                } else {
                    inputSources[i] = new Source(wire.source.node, wire.source.node.outputPorts.indexOf(wire.source));
                }

                if (inputSources[i].node == null) {
                    throw new ShaderManager.CompilationException("null source in input "+i+" of node \""+node+"\"");
                }
            }
        }

        protected OrganisedNode(Node node, Source[] inputSources) {
            this.node = node;
            this.inputSources = inputSources;
        }

        public void calculateOutputData(Supplier<String> uuid) {
            node.putOutputData(this, uuid);
        }
    }

    public record Source(Node node, int index) {
        public OutputPort getPort() {
            return node.outputPorts.get(index);
        }
    }
}
