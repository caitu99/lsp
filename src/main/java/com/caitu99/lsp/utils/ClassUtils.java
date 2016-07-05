package com.caitu99.lsp.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class ClassUtils {

    private static final Logger logger = LoggerFactory.getLogger(ClassUtils.class);

    /**
     * 获取继承接口的类
     * @param c
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<Class> getAllClassByInterface(Class c) {
        List returnClassList = new ArrayList<Class>();
        //判断是不是接口,不是接口不作处理
        if (c.isInterface()) {
            String packageName = c.getPackage().getName();    //获得当前包名
            try {
                List<Class> allClass = getClasses(packageName);//获得当前包以及子包下的所有类
                //判断是否是一个接口
                for (int i = 0; i < allClass.size(); i++) {
                    if (c.isAssignableFrom(allClass.get(i))) {
                        if (!c.equals(allClass.get(i))) {
                            returnClassList.add(allClass.get(i));
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
        return returnClassList;
    }

    public static List<Class> getAllClassInPackageByInterface(Package p, Class c) throws IOException, ClassNotFoundException {
        ArrayList<Class> classes = new ArrayList<Class>();
        if (c.isInterface()) {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String packageName = c.getPackage().getName();    //获得当前包名
            String path = packageName.replace(".", "/");
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File file = new File(resource.getFile());
                if (file.getName().endsWith(".class")) {
                    Class<?> aClass = Class.forName(packageName + "." + file.getName().substring(0, file.getName().length() - 6));
                    if (!c.equals(aClass)) {
                        classes.add(aClass);
                    }
                }
            }
        }
        return classes;
    }

    /**
     * 获取包下的类
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static List<Class> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace(".", "/");
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClass(directory, packageName));
        }
        return classes;
    }

    /**
     * 获取目录下的类
     * @param directory
     * @param packageName
     * @return
     * @throws ClassNotFoundException
     */
    private static List<Class> findClass(File directory, String packageName)
            throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClass(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                try {
                    String clzName = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                    logger.info("load class: {}", clzName);
                    classes.add(Class.forName(clzName, false, classLoader));
                } catch (Exception e) {
                    logger.error("load class error", e);
                }
            }
        }
        return classes;
    }

    /**
     * 获取类的Field
     * @param name
     * @param clazz
     * @param searchParent
     * @return
     */
    public static Field getField(Class clazz, String name, boolean searchParent) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
        } catch (NoSuchFieldException ignored) { }

        if (field != null) return field;

        if (clazz.getSuperclass() != null && searchParent)
            return getField(clazz.getSuperclass(), name, true);

        return null;
    }

    /**
     * 获取类的Field
     * @param type
     * @param clazz
     * @param searchParent
     * @return
     */
    public static List<Field> getField(Class clazz, Class type, boolean searchParent) {
        List<Field> fieldList = new ArrayList<>();
        try {
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                if (!type.isPrimitive() && type.isAssignableFrom(field.getClass())) {
                    fieldList.add(field);
                }
            }
        } catch (Exception ignored) {
        }

        if (clazz.getSuperclass() != null && searchParent)
            getField(fieldList, clazz.getSuperclass(), type);

        return fieldList;
    }

    private static void getField(List<Field> fieldList, Class clazz, Class type) {
        try {
            Field[] fields = clazz.getFields();
            for (Field field : fields) {
                if (!type.isPrimitive() && type.isAssignableFrom(field.getClass())) {
                    fieldList.add(field);
                }
            }
        } catch (Exception ignored) {
        }

        if (clazz.getSuperclass() != null)
            getField(fieldList, clazz.getSuperclass(), type);
    }

    /**
     * 获取类实例的方法
     *
     * @param clazz
     * @param includeParentClass
     * @return
     */
    public static List<Method> getMothds(Class clazz, boolean includeParentClass) {
        List<Method> list = new ArrayList<Method>();
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            list.add(method);
        }
        if (includeParentClass) {
            getParentClassMothds(list, clazz.getSuperclass());
        }
        return list;
    }

    /**
     * 获取类实例的父类的方法
     *
     * @param list
     * @param clazz
     * @return
     */
    private static List<Method> getParentClassMothds(List<Method> list, Class clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        Collections.addAll(list, methods);
        if (clazz.getSuperclass() == Object.class) {
            return list;
        }
        getParentClassMothds(list, clazz.getSuperclass());
        return list;
    }
}
