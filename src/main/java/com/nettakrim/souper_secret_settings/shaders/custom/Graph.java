package com.nettakrim.souper_secret_settings.shaders.custom;

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

    private int changed;
    private long version;

    public final Identifier graphId;
    private static long graphIdCounter;

    protected T lastCompiled = null;
    public ShaderManager.CompilationException lastError = null;
    private OrganisedGraph lastOrganised;

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

    public void makeChange(boolean topological) {
        changed = Math.max(changed, topological ? 2 : 1);
    }

    public long getVersion() {
        return version;
    }

    public abstract CreationCategory getCreationRoot();

    protected abstract String getType();

    public T getOrCompile() {
        if (changed > 0 || lastCompiled == null) {
            lastError = null;
            try {
                lastCompiled = compile();
                version++;
            } catch (ShaderManager.CompilationException compilationException) {
                SouperSecretSettingsClient.log("Failed to compile",graphId,compilationException.getMessage());
                lastError = compilationException;
            }
            changed = 0;
        }
        return lastCompiled;
    }

    protected abstract T compile() throws ShaderManager.CompilationException;

    public void clearCompileCaches() {
        lastOrganised = null;
        for (Node node : nodes) {
            node.clearCompileCaches(false);
        }
    }

    protected String getElapsedTime(long start, long end) {
        long micros = (end-start)/1000;
        return (micros / 1000)+"."+String.format("%3d",(micros % 1000)).replace(' ', '0')+"ms";
    }

    protected OrganisedGraph organise() throws ShaderManager.CompilationException {
        if (changed > 1 || lastOrganised == null) {
            lastOrganised = new OrganisedGraph(this);
        }
        return lastOrganised;
    }

    // gets all the nodes into a format where they can be traversed easily (since its stored as a loose pile of nodes and wires for editing)
    // the resulting graph is ordered such that a node will *always* be before anything that uses it
    protected static class OrganisedGraph {
        public final ArrayList<OrganisedNode> organisedNodes;

        private OrganisedGraph(Graph<?> graph) throws ShaderManager.CompilationException {
            organisedNodes = new ArrayList<>(graph.nodes.size());

            for (Node node : graph.nodes) {
                node.clearCompileCaches(true);
            }

            // find all ends of the graph to make sure every relevant bit is visited, but excess nodes aren't
            // this does mean weird fragments with a separate input/output chain will be included, which are probably often incorrect. but thats fine
            Node main = null;
            for (Node node : graph.nodes) {
                Node.RootType rootType = node.rootType(graph.wires);
                if (rootType == Node.RootType.MAIN) {
                    if (main != null) {
                        throw new GraphCompilationException("Only one main output node is allowed", node);
                    }
                    main = node;
                } else if (rootType == Node.RootType.ALTERNATE) {
                    AddNode(node, graph.wires);
                }
            }

            if (main == null) {
                throw new ShaderManager.CompilationException("No main output");
            }

            // add the main output last, so that the resulting code is ordered slightly nicer
            AddNode(main, graph.wires);
        }

        private void AddNode(Node node, HashMap<InputPort,Wire> wires) throws ShaderManager.CompilationException {
            // node already added to list
            if (node.compileState == 2) {
                return;
            }

            // if a node is reached again before it's been added to the list, then that means theres a loop
            if (node.compileState == 1) {
                throw new GraphCompilationException("Loop in graph", node);
            }

            node.compileState = 1;
            OrganisedNode organisedNode = new OrganisedNode(node, wires);

            // recursively add all sources of the node
            // it is desirable that this is depth first, since it means chains are likely to be continuous in memory
            // this means any long chains will be able to swap targets back and forth with decent efficiency
            for (Source source : organisedNode.inputSources) {
                if (source == null || source.node == null) {
                    continue;
                }

                AddNode(source.node, wires);
            }

            // adding nodes to the list only after recursively adding their children ensures all their dependencies are before them
            node.compileState = 2;
            organisedNodes.add(organisedNode);
        }
    }

    public static class OrganisedNode {
        public final Node node;
        public final Source[] inputSources;

        public OrganisedNode(Node node, HashMap<InputPort,Wire> wires) throws ShaderManager.CompilationException {
            this.node = node;
            this.inputSources = new Source[node.inputPorts.size()];

            for (int i = 0; i < inputSources.length; i++) {
                InputPort port = node.inputPorts.get(i);

                // prioritise wire connection over docked connection
                Wire wire = wires.get(port);
                if (wire == null) {
                    inputSources[i] = new Source(port.docked, 0);
                } else {
                    inputSources[i] = new Source(wire.source.node, wire.source.node.outputPorts.indexOf(wire.source));
                }

                if (inputSources[i].node == null && port.portType != PortType.UNUSED) {
                    throw new GraphCompilationException("Input \""+port.name+"\" is empty", node);
                }
            }
        }

        public void calculateOutputData(Supplier<String> uuid) throws GraphCompilationException {
            node.putOutputData(this, uuid);
        }

        public @Nullable Object getMainObject() throws GraphCompilationException {
            return node.getMainObject(this);
        }

        public Object getInputData(int index) {
            return inputSources[index].getPort().outputData;
        }

        public String getVectorInput(int input, PortType output) throws GraphCompilationException {
            OutputPort outputPort = inputSources[input].getPort();

            if (outputPort.portType == PortType.VECN) {
                throw new GraphCompilationException("Using unknown value", outputPort.node);
            }
            if (output == PortType.VECN) {
                throw new GraphCompilationException("Using unknown value", node);
            }

            return PortType.getVector((String)outputPort.outputData, outputPort.portType, output);
        }
    }

    public record Source(Node node, int index) {
        public OutputPort getPort() {
            return node.outputPorts.get(index);
        }
    }
}
