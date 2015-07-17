package com.yahoo.jonaswu.cucumberparallel;

import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.concurrent.LinkedBlockingDeque;

public class ResourcesContainer {


    private static LinkedBlockingDeque<Object> availableResources =
            new LinkedBlockingDeque();
    private static ThreadLocal<Object> instances = new ThreadLocal<Object>();


    public static synchronized Object get() {

        Object obj = instances.get();
        if (obj != null) {
            return obj;
        }

        if (availableResources.size() > 0) {
            obj = availableResources.poll();
            instances.set(obj);
            return obj;
        }
        return null;

    }

    public static synchronized void register(Object obj) {
        instances.set(obj);
    }


    public static synchronized void release() {
        System.out.println("release!");
        Object obj = instances.get();
        instances.remove();
        availableResources.push(obj);
    }
}

