package com.nettakrim.souper_secret_settings.shaders.custom;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.ShaderManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Graph {
    protected Set<Node> nodes;
    protected Set<Wire> wires;

    // get every wire that connects to a given node
    public Set<Wire> getSources(Node node) {
        return wires.stream().filter(wire -> wire.destination.node == node).collect(Collectors.toSet());
    }

    // gets all the nodes into a format where they can be traversed easily (since its stored as a loose pile of nodes and wires for editing)
    // the resulting graph is ordered such that a node will *always* be before anything that uses it
    protected static class OrganisedGraph {
        public final ImmutableList<OrganisedNode> organisedNodes;

        public OrganisedGraph(Graph graph) throws ShaderManager.CompilationException {
            ArrayList<OrganisedNode> backwardsNodes = new ArrayList<>(graph.nodes.size());

            // find all ends of the graph to make sure every relevant bit is visited, but excess nodes arent
            // this does mean weird fragments with a separate input/output chain will be included, which are probably often incorrect. but thats fine
            for (Node node : graph.nodes) {
                if (node.isEnd()) {
                    AddRoot(node, backwardsNodes, graph::getSources);
                }
            }

            this.organisedNodes = ImmutableList.copyOf(backwardsNodes.reversed());
        }

        public void AddRoot(Node node, ArrayList<OrganisedNode> backwardsNodes, Function<Node, Set<Wire>> getSources) throws ShaderManager.CompilationException {
            ArrayList<OrganisedNode> block = new ArrayList<>();
            int index = AddNode(node, backwardsNodes, block, getSources);

            // insert block before its earliest source, so that the list *always* places nodes before things they need to use (this is flipped later)
            if (index == Integer.MAX_VALUE) {
                index = 0;
            }
            backwardsNodes.addAll(index, block);
        }

        private int AddNode(Node node, ArrayList<OrganisedNode> backwardsNodes, List<OrganisedNode> block, Function<Node, Set<Wire>> getSources) throws ShaderManager.CompilationException {
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

            OrganisedNode organisedNode = new OrganisedNode(node, getSources);
            block.add(organisedNode);

            // recursively add all sources of the node
            // it is desirable that this is depth first, since it means chains are likely to be continuous in memory
            int max = Integer.MAX_VALUE;
            for (Source source : organisedNode.inputSources) {
                if (source == null) {
                    continue;
                }

                int current = AddNode(source.node, backwardsNodes, block, getSources);
                // find the earliest dependency
                if (current < max) {
                    max = current;
                }
            }

            return max;
        }
    }

    protected static class OrganisedNode {
        public final Node node;
        public final Source[] inputSources;

        public OrganisedNode(Node node, Function<Node, Set<Wire>> getSources) {
            this(node, new Source[node.inputPorts.size()]);

            // get all nodes that are inputs for the given node
            for (Wire wire : getSources.apply(node)) {
                int inputPortIndex = node.inputPorts.indexOf(wire.destination);
                assert inputPortIndex >= 0;

                inputSources[inputPortIndex] = new Source(wire.source.node, wire.source.node.outputPorts.indexOf(wire.source));
            }

            for (int i = 0; i < inputSources.length; i++) {
                if (inputSources[i] == null) {
                    inputSources[i] = new Source(node.inputPorts.get(i).docked, 0);
                }
            }
        }

        protected OrganisedNode(Node node, Source[] inputSources) {
            this.node = node;
            this.inputSources = inputSources;
        }
    }

    protected record Source(Node node, int index) {}
}
