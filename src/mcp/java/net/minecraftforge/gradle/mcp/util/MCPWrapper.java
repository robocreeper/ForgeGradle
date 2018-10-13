package net.minecraftforge.gradle.mcp.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.zip.ZipFile;

import org.gradle.api.Project;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import net.minecraftforge.gradle.common.config.Config;
import net.minecraftforge.gradle.common.config.MCPConfigV1;
import net.minecraftforge.gradle.common.util.HashFunction;
import net.minecraftforge.gradle.common.util.Utils;

public class MCPWrapper {
    private final String hash;
    private final File data;
    private final File root;
    private final MCPConfigV1 config;
    protected final Map<String, MCPRuntime> runtimes = Maps.newHashMap();

    public MCPWrapper(File data, File root) throws IOException {
        this(HashFunction.SHA1.hash(data), data, root);
    }

    public MCPWrapper(String hash, File data, File root) throws IOException {
        this.hash = hash;
        this.data = data;
        this.root = root;
        byte[] cfg_data = Utils.getZipData(data, "config.json");
        int spec = Config.getSpec(cfg_data);
        if (spec != 1)
            throw new IllegalStateException("Could not load MCP config, Unknown Spec: " + spec + " File: " + data);
        this.config = MCPConfigV1.get(cfg_data);
    }

    public MCPRuntime getRuntime(Project project, String side) {
        MCPRuntime ret = runtimes.get(side);
        if (ret == null) {
            ret = new MCPRuntime(project, data, config, side, new File(root, side), Collections.emptyMap());
            runtimes.put(side, ret);
        }
        return ret;
    }

    public File getZip() {
        return data;
    }

    public String getHash() {
        return this.hash;
    }

    public MCPConfigV1 getConfig() {
        return this.config;
    }

    public File getRoot() {
        return this.root;
    }

    public void extractData(File target, String... path) throws IOException {
        String name = config.getData(path);
        if (name == null)
            throw new IOException("Unknown MCP Entry: " + Joiner.on("/").join(path));

        try (ZipFile zip = new ZipFile(data)) {
            Utils.extractFile(zip, name, target);
        }
    }

    public byte[] getData(String... path) throws IOException {
        String name = config.getData(path);
        if (name == null)
            throw new IOException("Unknown MCP Entry: " + Joiner.on("/").join(path));
        return Utils.getZipData(data, name);
    }
}
