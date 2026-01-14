package com.example.trolyyte.domain.ai;

import com.example.trolyyte.domain.model.NlpResult;

public interface NlpService {

    NlpResult processText(String text);
}
