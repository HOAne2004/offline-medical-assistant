package com.example.trolyyte.data.nlu;

import com.example.trolyyte.domain.ai.NlpService;
import com.example.trolyyte.domain.model.NlpResult;

public class RuleBasedNlpServiceImpl implements NlpService {

    private final RuleBasedNlpEngine engine;

    public RuleBasedNlpServiceImpl(RuleBasedNlpEngine engine) {
        this.engine = engine;
    }

    @Override
    public NlpResult processText(String text) {
        return engine.analyze(text);
    }
}