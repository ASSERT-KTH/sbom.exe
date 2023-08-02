package io.github.algomaster99.terminator.commons.fingerprint.provenance;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.github.algomaster99.terminator.commons.fingerprint.classfile.ClassFileAttributes;
import java.io.IOException;

@JsonDeserialize(using = ProvenanceDeserializer.class)
public interface Provenance {
    ClassFileAttributes classFileAttributes();
}

class ProvenanceDeserializer extends JsonDeserializer<Provenance> {
    @Override
    public Provenance deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        ObjectNode root = mapper.readTree(p);
        Class<? extends Provenance> instanceClass = null;
        if (root.has("path")) {
            instanceClass = Jar.class;
        } else {
            instanceClass = Maven.class;
        }
        if (instanceClass == null) {
            return null;
        }
        return mapper.convertValue(root, instanceClass);
    }
}
