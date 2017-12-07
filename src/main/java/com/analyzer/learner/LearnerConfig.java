package com.analyzer.learner;

import org.deeplearning4j.ui.api.UIServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LearnerConfig {

    @Bean
    public UIServer getUIServer(){
        return UIServer.getInstance();
    }
}
