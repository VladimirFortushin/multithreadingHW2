package org.example.schedule;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ComputationScheduler implements Scheduler {

    private final int CPUS = Runtime.getRuntime().availableProcessors();

    private final ExecutorService executor = Executors.newFixedThreadPool(CPUS);
    @Override
    public void execute(Runnable task) {
        executor.submit(task);
    }
}
