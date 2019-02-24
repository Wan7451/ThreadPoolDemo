# ThreadPoolDemo
Android标准线程池的使用方式

***大部分的异步操作都可以使用***

CORE_POOL_SIZE 核心线程数
MAXIMUM_POOL_SIZE 最大线程数量
KEEP_ALIVE 1s闲置回收
TimeUnit.SECONDS 时间单位
sPoolWorkQueue 异步任务队列
sThreadFactory 线程工厂

如果当前线程池中的数量小于corePoolSize，创建并添加的任务。
如果当前线程池中的数量等于corePoolSize，缓冲队列 workQueue未满，那么任务被放入缓冲队列、等待任务调度执行。
如果当前线程池中的数量大于corePoolSize，缓冲队列workQueue已满，并且线程池中的数量小于maximumPoolSize，新提交任务会创建新线程执行任务。
如果当前线程池中的数量大于corePoolSize，缓冲队列workQueue已满，并且线程池中的数量等于maximumPoolSize，新提交任务由Handler处理。
当线程池中的线程大于corePoolSize时，多余线程空闲时间超过keepAliveTime时，会关闭这部分线程。


```
public class TaskManager {
    /**
     * 线程池
     */
    private ThreadPoolExecutor executor;
    /**
     * 任务队列
     */
    private final LinkedBlockingQueue<Runnable> queue;
    private final RejectedExecutionHandler handler;
    private final ThreadFactory factory;

    public TaskManager() {
        queue = new LinkedBlockingQueue<>();
        handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                try {
                    queue.put(r);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, "net");
            }
        };
        int coreSize = Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(coreSize,
                coreSize * 2,
                1, TimeUnit.SECONDS,
                queue,
                factory,
                handler);
        this.executor.execute(runnable);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                Runnable take = null;
                //阻塞，等待队列
                try {
                    take = queue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (take != null) {
                    executor.execute(take);
                }
            }
        }
    };

    /**
     * 执行任务
     * @param runnable
     */
    public void execute(@NonNull Runnable runnable) {
        try {
            //添加队列
            queue.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
```
