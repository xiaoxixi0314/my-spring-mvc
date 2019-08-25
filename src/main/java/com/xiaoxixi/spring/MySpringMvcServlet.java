package com.xiaoxixi.spring;

import com.xiaoxixi.spring.annotation.Autowired;
import com.xiaoxixi.spring.annotation.RequestMapping;
import com.xiaoxixi.spring.annotation.RestController;
import com.xiaoxixi.spring.annotation.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * my spring mvc servlet
 */
public class MySpringMvcServlet extends HttpServlet {

    private static List<String> classNames = new ArrayList<String>();

    private static Map<String, Class> iocMap = new HashMap<String, Class>();

    private static Map<String, Method> settingMap = new HashMap<String, Method>();
    @Override
    public void init() throws ServletException {
        super.init();
        // 1. 扫描包中的所有类
        scanPackage("com.xiaoxixi.spring");
        // 2. ioc，实例化对象
        ioc();
        // 3. set mappings
        setMappings();
    }

    /**
     *
     * @param packageName
     */
    private void scanPackage(String packageName) {
        // 把所有的.替换成/
        packageName = packageName.replaceAll("\\.", "/");
        URL url = this.getClass().getClassLoader().getResource(packageName);
        File dir = new File(url.getFile());
        for(File file : dir.listFiles()) {
            if (file.isDirectory()) {
                packageName = packageName + "." + dir.getName();
                scanPackage(packageName);
            } else {
                classNames.add(packageName + "." + file.getName());
            }
        }
    }

    /**
     * ioc, 设置controller和service
     */
    private void ioc() {
        try {
            for(String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(RestController.class)) {
                    RestController restController = clazz.getAnnotation(RestController.class);
                    iocMap.put(restController.value(), clazz);
                }
                if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);
                    iocMap.put(service.value(), clazz);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setMappings() {
        for(Map.Entry<String, Class> entry : iocMap.entrySet()) {
            String key = entry.getKey();
            Class<?> clazz = entry.getValue();
            if (clazz.isAnnotationPresent(RestController.class)) {
                // 获取controller中的所有方法
                Method[] methods = clazz.getMethods();
                Field[] fileds = clazz.getFields();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        String mapping = requestMapping.value();
                        settingMap.put(mapping, method);
                    }
                }
                for (Field field : fileds) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Autowired.class)) {
                        Autowired autowired = field.getAnnotation(Autowired.class);
                        try {
                            field.set(iocMap.get(autowired.value()), iocMap.get(autowired.value()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
