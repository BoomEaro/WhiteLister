package ru.boomearo.whitelister.object;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

//По сути точно такая же реализация фабрики как по умолчанию, однако здесь принимаются больше параметров
public class ExtendedThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final String namePrefix;

    private final String factoryName;
    private final int priority;

    public ExtendedThreadFactory(String factoryName, int priority) {
        this.factoryName = factoryName;
        this.priority = priority;

        SecurityManager s = System.getSecurityManager();
        this.group = (s != null) ? s.getThreadGroup() :
                Thread.currentThread().getThreadGroup();
        this.namePrefix = this.factoryName + "-pool-" +
                poolNumber.getAndIncrement() +
                "-thread-";
    }

    public String getFactoryName() {
        return this.factoryName;
    }

    public int getPriority() {
        return this.priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(this.group, r,
                this.namePrefix + this.threadNumber.getAndIncrement(),
                0);

        t.setPriority(this.priority);

        return t;
    }

}
