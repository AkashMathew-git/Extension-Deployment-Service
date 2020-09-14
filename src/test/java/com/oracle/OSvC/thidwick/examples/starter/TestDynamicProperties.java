package com.oracle.OSvC.thidwick.examples.starter;

import com.netflix.hystrix.strategy.properties.HystrixDynamicProperties;
import com.netflix.hystrix.strategy.properties.HystrixDynamicProperty;

public class TestDynamicProperties implements HystrixDynamicProperties {

    @Override
    public HystrixDynamicProperty<String> getString(String name, String fallback) {
        return new ConsulDynamicProperty<>(name, fallback);
    }

    @Override
    public HystrixDynamicProperty<Integer> getInteger(String name, Integer fallback) {
        return new ConsulDynamicProperty<>(name, fallback);
    }

    @Override
    public HystrixDynamicProperty<Long> getLong(String name, Long fallback) {
        return new ConsulDynamicProperty<>(name, fallback);
    }

    @Override
    public HystrixDynamicProperty<Boolean> getBoolean(String name, Boolean fallback) {
        return new ConsulDynamicProperty<>(name, fallback);
    }

    private class ConsulDynamicProperty<T> implements HystrixDynamicProperty<T> {
        private String name;
        private T value;

        ConsulDynamicProperty(String name, T value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public void addCallback(Runnable callback) { }

        @Override
        public T get() {
            return value;
        }

    }
}
