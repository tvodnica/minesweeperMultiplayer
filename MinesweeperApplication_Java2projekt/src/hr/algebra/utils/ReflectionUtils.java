/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 *
 * @author dnlbe
 */
public class ReflectionUtils {

    public static void readClassInfo(Class<?> clazz, StringBuilder classInfo) {
        appendPackage(clazz, classInfo);
        // we can't find imports because the compiler doesn't put them into the object file
        // import is just a shorthand to the compiler    appendModifiers(clazz, classInfo);
        appendModifiers(clazz, classInfo);
        classInfo.append(" ").append(clazz.getSimpleName());
        appendParent(clazz, classInfo, true);
        appendInterfaces(clazz, classInfo);
    }

    private static void appendPackage(Class<?> clazz, StringBuilder classInfo) {
        classInfo
                .append(clazz.getPackage())
                .append("\n\n");
    }

    private static void appendModifiers(Class<?> clazz, StringBuilder classInfo) {
        classInfo.append(Modifier.toString(clazz.getModifiers()));
    }

    private static void appendParent(Class<?> clazz, StringBuilder classInfo, boolean first) {
        Class<?> parent = clazz.getSuperclass();
        if (parent == null) {
            return;
        }
        if (first) {
            classInfo.append("\nextends");
        }
        classInfo
                .append(" ")
                .append(parent.getName());
        appendParent(parent, classInfo, false);
    }

    private static void appendInterfaces(Class<?> clazz, StringBuilder classInfo) {
        if (clazz.getInterfaces().length > 0) {
            classInfo.append("\nimplements ");
            classInfo.append(
                    Arrays.stream(clazz.getInterfaces())
                            .map(Class::getName)
                            .collect(Collectors.joining(" "))
            );
        }
    }

    public static void readClassAndMembersInfo(Class<?> clazz, StringBuilder classAndMembersInfo) {
        readClassInfo(clazz, classAndMembersInfo);
        appendFields(clazz, classAndMembersInfo);
        appendMethods(clazz, classAndMembersInfo);
        appendConstructors(clazz, classAndMembersInfo);
    }

    private static void appendFields(Class<?> clazz, StringBuilder classAndMembersInfo) {
        //Field[] fields = clazz.getFields();
        Field[] fields = clazz.getDeclaredFields();
        classAndMembersInfo.append("\n\n");
        classAndMembersInfo.append(
                Arrays.stream(fields)
                        .map(Objects::toString)
                        .collect(Collectors.joining("\n"))
        );
    }

    private static void appendMethods(Class<?> clazz, StringBuilder classAndMembersInfo) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            classAndMembersInfo.append("\n");
            appendAnnotations(method, classAndMembersInfo);
            classAndMembersInfo
                    .append("\n")
                    .append(Modifier.toString(method.getModifiers()))
                    .append(" ")
                    .append(method.getReturnType())
                    .append(" ")
                    .append(method.getName());
            appendParameters(method, classAndMembersInfo);
            appendExceptions(method, classAndMembersInfo);
        }
    }

    private static void appendAnnotations(Executable executable, StringBuilder classAndMembersInfo) {
        classAndMembersInfo.append(
                Arrays.stream(executable.getAnnotations())
                        .map(Objects::toString)
                        .collect(Collectors.joining("\n")));
    }

    private static void appendParameters(Executable executable, StringBuilder classAndMembersInfo) {
        classAndMembersInfo.append(
                Arrays.stream(executable.getParameters())
                        .map(Objects::toString)
                        .collect(Collectors.joining(", ", "(", ")"))
        );
    }

    private static void appendExceptions(Executable executable, StringBuilder classAndMembersInfo) {
        if (executable.getExceptionTypes().length > 0) {
            classAndMembersInfo.append(" throws ");
            classAndMembersInfo.append(
                    Arrays.stream(executable.getExceptionTypes())
                            .map(Class::getName)
                            .collect(Collectors.joining(" "))
            );
        }
    }

    private static void appendConstructors(Class<?> clazz, StringBuilder classAndMembersInfo) {
        Constructor[] constructors = clazz.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            classAndMembersInfo.append("\n\n");
            appendAnnotations(constructor, classAndMembersInfo);
            classAndMembersInfo
                    .append("\n")
                    .append(Modifier.toString(constructor.getModifiers()))
                    .append(" ")
                    .append(constructor.getName());
            appendParameters(constructor, classAndMembersInfo);
            appendExceptions(constructor, classAndMembersInfo);
        }
    }
}
