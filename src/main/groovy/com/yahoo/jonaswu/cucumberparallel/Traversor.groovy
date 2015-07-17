package com.yahoo.jonaswu.cucumberparallel

import cucumber.runtime.ClassFinder
import cucumber.runtime.Runtime
import cucumber.runtime.RuntimeOptions
import cucumber.runtime.io.MultiLoader
import cucumber.runtime.io.ResourceLoader
import cucumber.runtime.io.ResourceLoaderClassFinder

import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

public class Traversor {

    public static pureTraverse(NodesFactory.Node root, args) {
        def totalNodeCounter = new AtomicInteger()
        totalNodeCounter.set(NodesFactory.getNodeCount() - 1)
        List<NodesFactory.Node> nodes = NodesFactory.getParentsNode(root)
        println 'child size: ' + nodes.size()
        BlockingQueue toVisited = new LinkedBlockingQueue();
        nodes.each {
            NodesFactory.Node node ->
                toVisited << node
        }

        def defaultShortTimeoutSeconds = 10 * 60;

        final Semaphore semph = new Semaphore(Config.max_instance);
        ExecutorService exec = Executors.newCachedThreadPool()
        def classLoader = Thread.currentThread().getContextClassLoader()
        ResourceLoader resourceLoader = new MultiLoader(classLoader)
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);

        while (true) {
            println 'acquire!!'
            def NodesFactory.Node node = toVisited.poll(defaultShortTimeoutSeconds, TimeUnit.SECONDS);
            if (totalNodeCounter.get() == 0)
                break
            semph.acquire();
            println 'Node traverse:' + node.rawTagList
            def res = []
            [['--tags'].multiply(node.rawTagList.size()), node.rawTagList,].transpose().each {
                res += it
            }

            def runnable = { ->
                println "thread id:" + Thread.currentThread().getId()
                CucumberArgumentMapper cucumberArgumentMapper = new CucumberArgumentMapper()
                def mappedArgs = cucumberArgumentMapper.mapper(res + args, node.getId())
                RuntimeOptions runtimeOptions = new RuntimeOptions(mappedArgs)
                println 'begin processing' + node.rawTagList
                Runtime runtime = new Runtime(resourceLoader, classFinder, classLoader, runtimeOptions);
                runtime.run()
                println 'end run'
                NodesFactory.nofifyVisitToParent(node).each { NodesFactory.Node nextVisitNode ->
                    println nextVisitNode.rawTagList
                    toVisited << nextVisitNode
                }
                println "totalNodeCounter:" + totalNodeCounter.decrementAndGet()
                if (totalNodeCounter.get() == 0)
                    toVisited << NodesFactory.creteNode(null, null, '')
                println 'end processing' + node.rawTagList
                semph.release()
                println 'release!!'
            }
            exec.submit(runnable as Callable);
        }
        exec.shutdown();
    }
}
