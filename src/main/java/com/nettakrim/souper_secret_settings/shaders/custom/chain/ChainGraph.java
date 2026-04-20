package com.nettakrim.souper_secret_settings.shaders.custom.chain;

import com.nettakrim.souper_secret_settings.shaders.custom.Graph;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderManager;

public class ChainGraph extends Graph {
    public boolean changed;

    public PostChain compile() throws ShaderManager.CompilationException {
        OrganisedGraph organisedGraph = new OrganisedGraph(this);

        // TODO:
        //  loop through graph nodes
        //  assign a UUID for each output of a given node (in this case thats a target identifier)
        //  when an OrganisedNode has inputs, reference the corresponding UUID

        changed = false;
        return PostChain.load(null, null, null, null, null);
    }
}
