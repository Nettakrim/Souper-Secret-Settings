package com.nettakrim.souper_secret_settings.shaders.custom;

import com.google.common.collect.ImmutableList;
import com.nettakrim.souper_secret_settings.SouperSecretSettingsClient;
import com.nettakrim.souper_secret_settings.gui.custom.CreationCategory;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class Graph<T> {
    public final List<Node> nodes = new ArrayList<>();
    public final HashMap<InputPort,Wire> wires = new HashMap<>();

    private boolean changed;

    public final Identifier graphId;
    private static long graphIdCounter;

    protected T lastCompiled = null;

    public Graph() {
        graphId = Identifier.fromNamespaceAndPath(getType(),String.valueOf(graphIdCounter++));
    }

    public void addWire(Wire wire) {
        wires.put(wire.destination, wire);
        wire.destination.node.updateConnections(wires);
    }

    public static boolean removeWiresIf(HashMap<InputPort, Wire> wires, Predicate<Wire> predicate) {
        boolean cut = false;
        Iterator<Wire> i = wires.values().iterator();
        while (i.hasNext()) {
            Wire wire = i.next();
            if (predicate.test(wire)) {
                i.remove();
                cut = true;
                wire.destination.node.updateConnections(wires);
            }
        }

        return cut;
    }

    public void makeChange() {
        changed = true;
    }

    public abstract CreationCategory getCreationRoot();

    protected abstract String getType();

    public T getOrCompile() {
        if (changed || lastCompiled == null) {
            changed = false;
            try {
                lastCompiled = compile();
            } catch (ShaderManager.CompilationException compilationException) {
                SouperSecretSettingsClient.log("Failed to compile",graphId,compilationException.getMessage());
            }
        }
        return lastCompiled;
    }

    protected abstract T compile() throws ShaderManager.CompilationException;

    protected String getElapsedTime(long start, long end) {
        long micros = (end-start)/1000;
        return (micros / 1000)+"."+String.format("%3d",(micros % 1000)).replace(' ', '0')+"ms";
    }

    protected OrganisedGraph organise() throws ShaderManager.CompilationException {
        return new OrganisedGraph(this);
    }

    // gets all the nodes into a format where they can be traversed easily (since its stored as a loose pile of nodes and wires for editing)
    // the resulting graph is ordered such that a node will *always* be before anything that uses it
    protected static class OrganisedGraph {
        public final ImmutableList<OrganisedNode> organisedNodes;

        private OrganisedGraph(Graph<?> graph) throws ShaderManager.CompilationException {
            ArrayList<OrganisedNode> backwardsNodes = new ArrayList<>(graph.nodes.size());

            for (Node node : graph.nodes) {
                node.clearCompileCaches();
            }

            // find all ends of the graph to make sure every relevant bit is visited, but excess nodes arent
            // this does mean weird fragments with a separate input/output chain will be included, which are probably often incorrect. but thats fine
            for (Node node : graph.nodes) {
                if (node.isEnd()) {
                    AddRoot(node, graph.wires, backwardsNodes);
                }
            }

            this.organisedNodes = ImmutableList.copyOf(backwardsNodes.reversed());
        }

        public void AddRoot(Node node, HashMap<InputPort,Wire> wires, ArrayList<OrganisedNode> backwardsNodes) throws ShaderManager.CompilationException {
            ArrayList<Node> encountered = new ArrayList<>();
            ArrayList<OrganisedNode> block = new ArrayList<>();
            int index = AddNode(node, wires, backwardsNodes, encountered, block, 0);

            // sort block by depth
            block.sort(Comparator.comparingInt(n -> n.depth));

            // insert block before its earliest source, so that the list *always* places nodes before things they need to use (this is flipped later)
            if (index == Integer.MAX_VALUE) {
                index = 0;
            }
            backwardsNodes.addAll(index, block);
        }

        private int AddNode(Node node, HashMap<InputPort,Wire> wires, ArrayList<OrganisedNode> backwardsNodes, List<Node> stack, List<OrganisedNode> block, int depth) throws ShaderManager.CompilationException {
            // dont allow loops
            if (stack.contains(node)) {
                throw new ShaderManager.CompilationException("Loop in graph");
            }
            stack.add(node);

            // but allow connections from previous roots
            for (int i = 0; i < backwardsNodes.size(); i++) {
                if (backwardsNodes.get(i).node == node) {
                    return i;
                }
            }

            // if a path merges, then propagate the depth values
            // the sort() could be avoided by insert()ing organised nodes the correct index to begin with
            // with some effort, this could be calculated here, then incremented along with depth
            // however that would be a bit complicated, and maybe not even more efficient
            // since its one sort vs a lot of insertions into an array (that could be batched though)
            // this needs testing
            for (OrganisedNode blockEntry : block) {
                if (blockEntry.node == node && blockEntry.depth < depth) {
                    int offset = depth - blockEntry.depth;
                    Stack<OrganisedNode> propagate = new Stack<>();
                    propagate.push(blockEntry);
                    while (!propagate.isEmpty()) {
                        OrganisedNode increment = propagate.pop();
                        increment.depth += offset;
                    }
                    return Integer.MAX_VALUE;
                }
            }

            OrganisedNode organisedNode = new OrganisedNode(node, wires, depth);
            block.add(organisedNode);
            node.includedInLastCompile = true;

            // recursively add all sources of the node
            // it is desirable that this is depth first, since it means chains are likely to be continuous in memory
            // ... however the sort will break this
            int min = Integer.MAX_VALUE;
            for (Source source : organisedNode.inputSources) {
                if (source == null) {
                    continue;
                }

                int count = stack.size();
                int current = AddNode(source.node, wires, backwardsNodes, stack, block, depth+1);
                // find the earliest dependency
                if (current < min) {
                    min = current;
                }

                // encountered acts as a stack of the current depth first search
                while (stack.size() > count) {
                    stack.removeLast();
                }
            }

            return min;
        }
    }

    public static class OrganisedNode {
        public final Node node;
        public final Source[] inputSources;
        protected int depth;

        public OrganisedNode(Node node, HashMap<InputPort,Wire> wires, int depth) throws ShaderManager.CompilationException {
            this.node = node;
            this.inputSources = new Source[node.inputPorts.size()];
            this.depth = depth;

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

        public void calculateOutputData(Supplier<String> uuid) {
            node.putOutputData(this, uuid);
        }

        public @Nullable Object getMainObject() {
            return node.getMainObject(this);
        }

        public Object getInputData(int index) {
            return inputSources[index].getPort().outputData;
        }

        public String getVectorInput(int input, PortType output) {
            OutputPort outputPort = inputSources[input].getPort();
            return PortType.getVector((String)outputPort.outputData, outputPort.portType, output);
        }
    }

    public record Source(Node node, int index) {
        public OutputPort getPort() {
            return node.outputPorts.get(index);
        }
    }
}
