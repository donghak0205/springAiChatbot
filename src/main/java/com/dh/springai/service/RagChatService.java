package com.dh.springai.service;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Optional;

@Service
public class RagChatService {

    private final ChatClient chatClient;

    public RagChatService(ChatClient.Builder chatClientBuilder, Advisor[] advisors) {
        this.chatClient = chatClientBuilder.defaultOptions(ChatOptions.builder().temperature(0.0).build())
                .defaultAdvisors(advisors).build();
    }

    public Flux<String> stream(Prompt prompt, String conversationId, Optional<String> filterExpressionAsOpt) {
        return buildChatClientRequestSpec(prompt, conversationId, filterExpressionAsOpt).stream().content();
    }

    private ChatClient.ChatClientRequestSpec buildChatClientRequestSpec(Prompt prompt, String conversationId,
                                                                        Optional<String> filterExpressionAsOpt) {
        ChatClient.ChatClientRequestSpec chatClientRequestSpec =
                chatClient.prompt(prompt).advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID,
                        conversationId));
        filterExpressionAsOpt.ifPresent(filterExpression -> chatClientRequestSpec.advisors(advisorSpec -> advisorSpec.param(VectorStoreDocumentRetriever.FILTER_EXPRESSION, filterExpression)));
        return chatClientRequestSpec;
    }

    public ChatResponse call(Prompt prompt, String conversationId, Optional<String> filterExpressionAsOpt){
        return buildChatClientRequestSpec(prompt, conversationId, filterExpressionAsOpt).call().chatResponse();
    }

}
