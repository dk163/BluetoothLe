package com.sharpnow.bluetoothle;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Administrator on 2016/8/16 0016.
 */
public class ReflectCase {

    private String packageName = null;

    public ReflectCase(String packageName) {
        this.packageName = packageName;
    }

    public Class loadClass(String packgeName) throws ClassNotFoundException {
        return Class.forName(packgeName);
    }

    public Object initMethod(Object obj, String methodName, Class[] methodClazzs, Object[] methodVals) {
        try {
            return  getMethod(loadClass(packageName), obj, methodName, methodClazzs, methodVals);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Object initObject(String packgeName, Class[] clazzName,
                             Object[] clazzValues) {
        try {
            return getConstructor(loadClass(packgeName), clazzName, clazzValues);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object getConstructor(Class clazz, Class[] clsName,
                                  Object[] clsValues) throws SecurityException,
            NoSuchMethodException, IllegalArgumentException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException {
        Object obj = null;
        if (null == clsName) {
            Constructor con = clazz.getDeclaredConstructor();
            con.setAccessible(true);
            obj = con.newInstance();
        } else {
            Constructor con = clazz.getDeclaredConstructor(clsName);
            con.setAccessible(true);
            obj = con.newInstance(clsValues);
        }
        return obj;
    }


    private Object getMethod(Class clazz, Object obj, String methodName, Class[] methodClazzs, Object[] clazzValues) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException, InvocationTargetException {
        Method method = null;
        if(null == clazzValues){
            method = clazz.getDeclaredMethod(methodName);
            method.setAccessible(true);
            return method.invoke(obj);
        }else{
            method = clazz.getDeclaredMethod(methodName, methodClazzs);
            method.setAccessible(true);
            return method.invoke(obj, clazzValues);
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }
}
