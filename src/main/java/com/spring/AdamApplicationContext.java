package com.spring;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AdamApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>(); // singleton pool
    private ConcurrentHashMap<String, BeanDefiniton> beanDefinitionMap = new ConcurrentHashMap<>();


    public AdamApplicationContext(Class configClass) {
        this.configClass = configClass;
        //analysis config class
        //ComponentScan Annotation  --> Scan path --> Scan  --> BeanDefinition --> BeanDefinitionMap
        sacn(configClass);

        for (Map.Entry<String, BeanDefiniton> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefiniton beanDefiniton = entry.getValue();
            if (beanDefiniton.getScope().equals("singleton")) {
                Object bean = creatBean(beanDefiniton); // singleton bean
                singletonObjects.put(beanName, bean); //put the singleton bean into singleton pool
            }
        }

    }

    private Object creatBean(BeanDefiniton beanDefiniton) {
        Class clazz = beanDefiniton.getClazz();
        try {
            Object instance = clazz.getDeclaredConstructor().newInstance();

            //依赖注入
            for (Field declaredField : clazz.getDeclaredFields()) {
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Object bean = getBean(declaredField.getName());
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void sacn(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getDeclaredAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.Value();//Scan path
        path = path.replace(".", "/");
        //scan class under path "com.adam.service"
        // classloader  :  BootStrap->jre/lib  Ext->jre/ext/lib App->classpath
        ClassLoader classLoader = AdamApplicationContext.class.getClassLoader(); //App
        URL resource = classLoader.getResource(path);
        File file = new File(resource.getFile());
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File file1 : files) {
                String fileName = file1.getAbsolutePath();
                String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                className = className.replace("\\",".");

                try {
                    Class<?> clazz = classLoader.loadClass(className);
                    if (clazz.isAnnotationPresent(Component.class)) {
                        //Current class is one Bean object
                        //analysis class is singleton bean or prototype bean  --> generate one beanDefinition object
                        Component componentAnnotation = clazz.getDeclaredAnnotation(Component.class);
                        String beanName = componentAnnotation.Value();
                        //BeanDefinition
                        BeanDefiniton beanDefiniton = new BeanDefiniton();
                        beanDefiniton.setClazz(clazz);
                        if (clazz.isAnnotationPresent(Scope.class)) {
                            Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                            beanDefiniton.setScope(scopeAnnotation.Value());
                        }else {
                            beanDefiniton.setScope("singleton");
                        }
                        beanDefinitionMap.put(beanName, beanDefiniton);
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    public Object getBean(String beanName){
        //get class (bean) by beanName
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefiniton beanDefiniton = beanDefinitionMap.get(beanName);
            if (beanDefiniton.getScope().equals("singleton")) {
                Object o = singletonObjects.get(beanName);
                return o;
            } else {
                //create bean object
                Object bean = creatBean(beanDefiniton);
                return bean;
            }
        } else {
            throw new NullPointerException();
        }
    }

}