package com.project.souklab.filestorage.controller;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Test configuration providing a synchronous task executor for async MVC support.
 * Scoped strictly to FileServingControllerTest to eliminate race conditions against
 * MockHttpServletResponse's non-thread-safe header map during MockMvc StreamingResponseBody dispatch.
 */
@TestConfiguration
public class FileServingAsyncTestConfig implements WebMvcConfigurer {

    /**
     * Configures a synchronous task executor so that StreamingResponseBody executions
     * run on the dispatch thread during MockMvc execution without background thread races.
     */
    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(new TaskExecutorAdapter(Runnable::run));
    }
}
