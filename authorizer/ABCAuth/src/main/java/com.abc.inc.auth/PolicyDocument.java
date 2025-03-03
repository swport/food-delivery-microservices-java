package com.abc.inc.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@JsonDeserialize(builder = PolicyDocument.Builder.class)
public class PolicyDocument {

    @JsonIgnore
    private static final String VERSION = "2012-10-17";
    @JsonIgnore
    private final List<Statement> statements;

    private PolicyDocument(Builder builder) {
        this.statements = builder.statements;
    }

    @JsonProperty("Version")
    public String getVersion() {
        return VERSION;
    }

    @JsonProperty("Statement")
    public List<Statement> getStatement() {
        return List.copyOf(statements);
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private List<Statement> statements = new ArrayList<>(List.of(Statement.builder().build()));

        private Builder() {
        }

        public Builder statement(Statement... statement) {
            if (statement != null && statement.length > 0) {
                statements.clear();
                Collections.addAll(this.statements, statement);
            } else {
                this.statements = new ArrayList<>();
            }
            return this;
        }

        public PolicyDocument build() {
            return new PolicyDocument(this);
        }
    }
}