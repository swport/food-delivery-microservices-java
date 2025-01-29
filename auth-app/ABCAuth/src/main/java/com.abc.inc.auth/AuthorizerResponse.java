package com.abc.inc.auth;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.HashMap;
import java.util.Map;

@JsonDeserialize(builder = AuthorizerResponse.Builder.class)
public class AuthorizerResponse {

    private final String principalId;
    private final PolicyDocument policyDocument;
    private final Map<String, String> context;

    private AuthorizerResponse(Builder builder) {
        this.principalId = builder.principalId;
        this.policyDocument = builder.policyDocument;
        this.context = builder.context;
    }

    public String getPrincipalId() {
        return principalId;
    }

    public PolicyDocument getPolicyDocument() {
        return policyDocument;
    }

    public Map<String, String> getContext() {
        return context != null ? Map.copyOf(context) : null;
    }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
        private String principalId;
        private PolicyDocument policyDocument = PolicyDocument.builder().build();
        private Map<String, String> context = new HashMap<>();

        private Builder() {
        }

        public Builder principalId(String principalId) {
            this.principalId = principalId;
            return this;
        }

        public Builder policyDocument(PolicyDocument policyDocument) {
            this.policyDocument = policyDocument != null ? policyDocument : PolicyDocument.builder().build();
            return this;
        }

        public Builder context(Map<String, String> context) {
            this.context = context != null ? context : new HashMap<>();
            return this;
        }

        public AuthorizerResponse build() {
            return new AuthorizerResponse(this);
        }
    }
}