package com.h2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.platform.commons.util.ReflectionUtils.*;

public class Module3_Test {
    private final String classToFind = "com.h2.SavingsCalculator";
    private final InputStream systemIn = System.in;
    private final PrintStream systemOut = System.out;

    private ByteArrayInputStream testIn;
    private ByteArrayOutputStream testOut;

    @BeforeEach
    public void setUpOutput() {
        testOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOut));
    }

    private void provideInput(String data) {
        testIn = new ByteArrayInputStream(data.getBytes());
        System.setIn(testIn);
    }

    private String getOutput() {
        return testOut.toString();
    }

    @AfterEach
    public void restoreSystemInputOutput() {
        System.setIn(systemIn);
        System.setOut(systemOut);
    }

    private static Optional<Class<?>> getClass(final String className) {
        Try<Class<?>> aClass = tryToLoadClass(className);
        return aClass.toOptional();
    }

    public Optional<Class<?>> getSavingsClass() {
        return getClass(classToFind);
    }

    @Test
    public void m03_01_testSavingsCalculatorExists() {
        Optional<Class<?>> maybeClass = getSavingsClass();
        assertTrue(maybeClass.isPresent(), classToFind + " must be created.");
    }

    @Test
    public void m3_02_testExistenceOfPrivateFields() {
        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();
        final Set<String> fieldNames = new HashSet<>(Arrays.asList("credits", "debits"));

        final Field[] declaredFields = savingsCalculator.getDeclaredFields();
        assertEquals(2, declaredFields.length, "2 fields (credits and debits) should be available in " + classToFind);

        for (final Field field : declaredFields) {
            assertTrue(isPrivate(field), field.getName() + " should be declared private");
            assertEquals(float[].class, field.getType(), field.getName() + " should be of type 'float[]'");
            assertTrue(fieldNames.contains(field.getName()), field.getName() + " is not a valid name. It should be either 'credits' or 'debits'");
        }
    }

    @Test
    public void m3_03_testConstructor() {
        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();
        final Constructor<?>[] constructors = savingsCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        final Constructor<?> constructor = constructors[0];
        assertTrue(isPublic(constructor), "constructor must be declared 'public'");
        assertEquals(2, constructor.getParameterCount(), "Constructor should have 2 parameters");

        for (final Parameter parameter : constructor.getParameters()) {
            assertEquals(float[].class, parameter.getType(), "Constructor parameter should be of type 'float[]'");
        }
    }

    @Test
    public void m3_04_testFieldsValueSetWhenConstructorCalled() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();
        final Constructor<?>[] constructors = savingsCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        float[] credits = new float[]{10.0f, 20.0f};
        float[] debits = new float[]{5.0f};
        final Constructor<?> constructor = constructors[0];

        int parameterCount = constructor.getParameterCount();
        assertEquals(2, parameterCount, classToFind + " must have a constructor with 2 parameters - credits and debits, both should be of type 'float[]'");

        Object instance = constructor.newInstance(credits, debits);

        final Field[] fields = savingsCalculator.getDeclaredFields();

        for (final Field field : fields) {
            field.setAccessible(true);
            float[] fieldValues = (float[]) field.get(instance);
            if (field.getName().equals("credits")) {
                assertArrayEquals(credits, fieldValues, "credits parameter should set the value in class field name 'credits'");
            } else if (field.getName().equals("debits")) {
                assertArrayEquals(debits, fieldValues, "debits parameter should set the value in class field name 'debits'");
            }
        }
    }

    @Test
    public void m3_05_testSumOfCreditsExists() {
        final String methodName = "sumOfCredits";

        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();

        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method sumOfCredits = filteredMethod.get(0);
        assertTrue(isPrivate(sumOfCredits), methodName + " must be declared as 'private'");
        assertEquals(float.class, sumOfCredits.getReturnType(), methodName + " method must return a value of type 'float'");
    }

    @Test
    public void m3_06_testSumOfCreditsWorksCorrectly() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();
        final Constructor<?>[] constructors = savingsCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        float[] credits = new float[]{10.0f, 20.0f};
        float[] debits = new float[]{5.0f};
        final Constructor<?> constructor = constructors[0];

        int parameterCount = constructor.getParameterCount();
        assertEquals(2, parameterCount, classToFind + " must have a constructor with 2 parameters - credits and debits, both should be of type 'float[]'");

        Object instance = constructor.newInstance(credits, debits);

        final String methodName = "sumOfCredits";
        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");
        Method method = filteredMethod.get(0);
        final float result = (float) invokeMethod(method, instance);

        assertEquals(30.0f, result, methodName + " is not returning sum of credits.");
    }

    @Test
    public void m3_07_testSumOfDebitsExists() {
        final String methodName = "sumOfDebits";

        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();

        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method sumOfCredits = filteredMethod.get(0);
        assertTrue(isPrivate(sumOfCredits), methodName + " must be declared as 'private'");
        assertEquals(float.class, sumOfCredits.getReturnType(), methodName + " method must return a value of type 'float'");
    }

    @Test
    public void m3_08_testSumOfDebitsWorksCorrectly() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();
        final Constructor<?>[] constructors = savingsCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        float[] credits = new float[]{10.0f, 20.0f};
        float[] debits = new float[]{5.0f, 10.f};
        final Constructor<?> constructor = constructors[0];

        int parameterCount = constructor.getParameterCount();
        assertEquals(2, parameterCount, classToFind + " must have a constructor with 2 parameters - credits and debits, both should be of type 'float[]'");

        Object instance = constructor.newInstance(credits, debits);

        final String methodName = "sumOfDebits";
        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");
        Method method = filteredMethod.get(0);
        final float result = (float) invokeMethod(method, instance);

        assertEquals(15.0f, result, methodName + " is not returning sum of debits.");
    }

    @Test
    public void m3_09_testRemainingDaysInMonthExists() {
        final String methodName = "remainingDaysInMonth";

        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();

        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method method = filteredMethod.get(0);
        assertTrue(isPrivate(method), methodName + " must be declared as 'private'");
        assertTrue(isStatic(method), methodName + " must be declared as 'static'");

        final Class<?>[] parameterTypes = method.getParameterTypes();
        assertEquals(1, parameterTypes.length, methodName + " must accept 1 parameter.");
        assertEquals(LocalDate.class, parameterTypes[0], methodName + " must accept only 1 parameter of type 'LocalDate'");

        assertEquals(int.class, method.getReturnType(), methodName + " method must return a value of type 'int'");
    }

    @Test
    public void m3_10_testRemainingDaysInMonthWorksCorrectly() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final String methodName = "remainingDaysInMonth";

        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();

        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method method = filteredMethod.get(0);

        final LocalDate _1feb2020 = LocalDate.of(2020, 2, 1);
        assertEquals(28, invokeMethod(method, null, _1feb2020), "For Feb 1, 2020, remainingDaysInMonth should return '29'");

        final LocalDate _15Mar2020 = LocalDate.of(2020, 3, 15);
        assertEquals(16, invokeMethod(method, null, _15Mar2020), "For Mar 15, 2020, remainingDaysInMonth should return '16'");

        final LocalDate _2Jun2020 = LocalDate.of(2020, 6, 2);
        assertEquals(28, invokeMethod(method, null, _2Jun2020), "For Jun 02, 2020, remainingDaysInMonth should return '28'");
    }

    @Test
    public void m3_11_testCalculateExists() {
        final String methodName = "calculate";

        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();

        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method method = filteredMethod.get(0);
        assertTrue(isPublic(method), methodName + " must be declared as 'public'");
        assertEquals(float.class, method.getReturnType(), methodName + " method must return a value of type 'float'");
    }

    @Test
    public void m3_12_testCalculateWorksCorrectly() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final String methodName = "calculate";
        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();
        final Constructor<?>[] constructors = savingsCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        float[] credits = new float[]{10.0f, 20.0f};
        float[] debits = new float[]{5.0f};
        final Constructor<?> constructor = constructors[0];

        int parameterCount = constructor.getParameterCount();
        assertEquals(2, parameterCount, classToFind + " must have a constructor with 2 parameters - credits and debits, both should be of type 'float[]'");

        Object instance = constructor.newInstance(credits, debits);
        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method method = filteredMethod.get(0);
        assertEquals(25.0f, invokeMethod(method, instance), "The calculate function should return '25.0' as the return value for credits=[10.0f, 20.0f], debits=[5.0f]");
    }

    @Test
    public void m3_13_testMainExists() {
        final String methodName = "main";

        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();

        final Method[] methods = savingsCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method method = filteredMethod.get(0);
        assertTrue(isPublic(method), methodName + " must be declared as 'public'");
        assertTrue(isStatic(method), methodName + " must be declared as 'static'");
        assertEquals(void.class, method.getReturnType(), methodName + " method must return a value of type 'void'");

        final Class<?>[] parameterTypes = method.getParameterTypes();
        assertEquals(1, parameterTypes.length, methodName + " must accept 1 parameter.");
        assertEquals(String[].class, parameterTypes[0], methodName + " must accept only 1 parameter of type 'String[]'");
    }

    @Test
    public void m3_14_testMainMethodPrintsCorrectOutput() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final String methodName = "main";
        final String remainingDaysInMonthMethodName = "remainingDaysInMonth";

        final Optional<Class<?>> maybeSavingsCalculator = getSavingsClass();
        assertTrue(maybeSavingsCalculator.isPresent(), classToFind + " must exist");
        final Class<?> savingsCalculator = maybeSavingsCalculator.get();

        final Method[] methods = savingsCalculator.getDeclaredMethods();

        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final List<Method> remainingDaysMethod = Arrays.stream(methods).filter(method -> method.getName().equals(remainingDaysInMonthMethodName)).collect(Collectors.toList());
        assertEquals(1, remainingDaysMethod.size(), classToFind + " should contain a method called '" + remainingDaysInMonthMethodName + "'");

        final LocalDate _1feb2020 = LocalDate.now();
        final int remainingDays = (int) invokeMethod(remainingDaysMethod.get(0), null, _1feb2020);

        final String credits = "10.0,20.0";
        final String debits = "5.0,20.0";

        Method method = savingsCalculator.getMethod("main", String[].class);
        method.invoke(null, (Object) new String[]{credits, debits});

        assertEquals("Net Savings = 5.0, remaining days in month = " + remainingDays + System.lineSeparator(), testOut.toString());
    }
}
