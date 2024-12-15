package com.example.kitchensink.config;

import com.example.kitchensink.model.User;
import com.mongodb.client.MongoClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.domain.Sort;

@Configuration
public class MongoConfig {
    
    @Bean
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        return new MongoTemplate(mongoClient, "KitchenSink_DB");
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initIndicesAfterStartup(ApplicationReadyEvent event) {
        MongoTemplate mongoTemplate = event.getApplicationContext().getBean(MongoTemplate.class);
        
        IndexOperations indexOps = mongoTemplate.indexOps(User.class);
        
        // Create unique index for username
        Index usernameIndex = new Index()
            .on("username", Sort.Direction.ASC)
            .unique();
        indexOps.ensureIndex(usernameIndex);
        
        // Create unique index for email
        Index emailIndex = new Index()
            .on("email", Sort.Direction.ASC)
            .unique();
        indexOps.ensureIndex(emailIndex);
    }
}
