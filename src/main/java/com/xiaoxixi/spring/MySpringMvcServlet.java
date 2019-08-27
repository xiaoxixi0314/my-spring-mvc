package com.xiaoxixi.spring;

import com.alibaba.fastjson.JSON;
import com.xiaoxixi.spring.annotation.MyAutowired;
import com.xiaoxixi.spring.annotation.MyRequestMapping;
import com.xiaoxixi.spring.annotation.MyRestController;
import com.xiaoxixi.spring.annotation.MyService;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * my spring mvc servlet
 */
public class MySpringMvcServlet extends HttpServlet {

    private static List<String> classNames = new ArrayList<String>();

    private static Map<String, Object> iocMap = new HashMap<String, Object>();

    private static Map<String, Method> settingMap = new HashMap<String, Method>();

    private static Map<String, Object> mappingClass = new HashMap<String, Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException {
        if (settingMap.isEmpty()) {
            return;
        }
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");
        if (!settingMap.containsKey(url)) {
            response.getWriter().write("404, NOT FOUND");
            return;
        }
        Method method = settingMap.get(url);
        // 获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 获取请求参数
        Map<String, String[]> params = request.getParameterMap();
        Object[] paramValues = new Object[parameterTypes.length];
        for (int i =0; i < parameterTypes.length; i ++) {
            String requestParam = parameterTypes[i].getSimpleName();
            if ("HttpServletRequest".equals(requestParam)) {
                paramValues[i] = request;
                continue;
            }
            if ("HttpServletResponse".equals(requestParam)) {
                paramValues[i] = response;
                continue;
            }
            if ("String".equals(requestParam)) {
                for(Map.Entry<String, String[]> entry: params.entrySet()) {
                    String value = Arrays.toString(entry.getValue());
                    paramValues[i] = value;
                }
            }
        }
        Object result = method.invoke(mappingClass.get(url), paramValues);
        response.getWriter().write(JSON.toJSONString(result));
    }

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
                scanPackage(packageName + "/" + file.getName());
            } else {
                classNames.add((packageName + "/" + file.getName()).replaceAll("/", ".").replace(".class", ""));
            }
        }
        System.out.println("===========");
        for(String className : classNames) {
            System.out.println(className);
        }
    }

    /**
     * ioc, 设置controller和service
     */
    private void ioc() {
        try {
            for(String className : classNames) {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(MyRestController.class)) {
                    MyRestController restController = clazz.getAnnotation(MyRestController.class);
                    iocMap.put(restController.value(), clazz.newInstance());
                }
                if (clazz.isAnnotationPresent(MyService.class)) {
                    MyService service = clazz.getAnnotation(MyService.class);
                    iocMap.put(service.value(), clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void setMappings() {
        for(Map.Entry<String, Object> entry : iocMap.entrySet()) {
            String key = entry.getKey();
            Class<?> clazz = entry.getValue().getClass();
            if (clazz.isAnnotationPresent(MyRestController.class)) {
                // 获取controller中的所有方法
                Method[] methods = clazz.getMethods();
                Field[] fields = clazz.getDeclaredFields();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(MyRequestMapping.class)) {
                        MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                        String mapping = requestMapping.value();
                        settingMap.put(mapping, method);
                        mappingClass.put(mapping, entry.getValue());
                    }
                }
                for (Field field : fields) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(MyAutowired.class)) {
                        MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                        try {
                            field.set(entry.getValue(), iocMap.get(autowired.value()));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

}
