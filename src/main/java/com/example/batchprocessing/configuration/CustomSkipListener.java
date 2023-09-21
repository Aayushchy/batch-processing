package com.example.batchprocessing.configuration;

import com.example.batchprocessing.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomSkipListener implements SkipListener<Person, Person> {

   // @Value("#{jobParameters['outputFile']}")  // Example: If you want to log the output file name
   // private String outputFile;

    @Override
    public void onSkipInRead(Throwable t) {
        // Handle read skip (optional)
        // Log or process the skipped exception (t)
        log.error("Error while reading {}", t.getMessage());
    }

    @Override
    public void onSkipInProcess(Person item, Throwable t) {
        // Handle process skip
        // Log or process the skipped item (item) and exception (t)
        // You can use outputFile if needed
        log.error("Error while processing {}", t.getMessage());

    }

    @Override
    public void onSkipInWrite(Person item, Throwable t) {
        // Handle write skip (optional)
        // Log or process the skipped item (item) and exception (t)
        // You can use outputFile if needed
        log.error("Error while writing {}", t.getMessage());

    }
}