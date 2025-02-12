package com.abc.inc.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@JsonDeserialize(builder = Statement.Builder.class)
public class Statement {

    @JsonIgnore
    private static final String ACTION = "execute-api:Invoke";
    @JsonIgnore
    private final String effect;
    @JsonIgnore
    private final List<String> resources;

    private Statement(Builder builder) {
        this.effect = builder.effect;
        this.resources = builder.resources;
    }

    @JsonProperty("Action")
    public String getAction() {
        return ACTION;
    }

    @JsonProperty("Effect")
    public String getEffect() {
        return effect;
    }

    @JsonProperty("Resource")
    public List<String> getResource() {
        return List.copyOf(resources);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String effect = "Deny";
        private List<String> resources = new ArrayList<>(List.of("*"));

        private Builder() {
        }

        public Builder effect(String effect) {
            this.effect = "Allow".equals(effect) ? effect : "Deny";
            return this;
        }

        public Builder resource(String... resource) {
            if (resource != null && resource.length > 0) {
                resources.clear();
                Collections.addAll(this.resources, resource);
            }
            return this;
        }

        public Builder resource(Collection<String> resource) {
            if (resource != null && !resource.isEmpty()) {
                resources.clear();
                resources.addAll(resource);
            }
            return this;
        }

        public Statement build() {
            return new Statement(this);
        }
    }
}