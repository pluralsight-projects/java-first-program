package com.h2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.function.Try;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.ReflectionUtils.*;

public class Module2_Test {
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

    public Optional<Class<?>> getAppClass() {
        final String classToFind = "com.h2.App";
        return getClass(classToFind);
    }

    public Optional<Class<?>> getBestLoanRatesClass() {
        final String classToFind = "com.h2.BestLoanRates";
        return getClass(classToFind);
    }


    @Test
    public void m02_01_testDoubleTheNumber() {
        for (int i = 1; i < 10; i++) {
            assertEquals(2 * i, App.doubleTheNumber(i), i + " should be " + 2 * i);
        }
    }

    @Test
    public void m02_02_assertPrivateMethodExistence() {

        final String methodName = "add";
        final Optional<Class<?>> maybeClass = getAppClass();
        assertTrue(maybeClass.isPresent());
        Class<?> aClass = maybeClass.get();
        Optional<Method> maybeMethod = findMethod(aClass, methodName, int[].class);
        assertTrue(maybeMethod.isPresent(), methodName + " should be present in " + aClass.getCanonicalName());

        final Method method = maybeMethod.get();
        assertTrue(isPrivate(method), methodName + " should be private");

        assertEquals(int.class, method.getReturnType(), methodName + " should return type should be 'int'");

        Parameter[] parameters = method.getParameters();
        assertEquals(1, parameters.length, methodName + " should have 1 parameter");
        assertEquals(int[].class, parameters[0].getType(), methodName + " parameter should be of type 'int[]'");

        assertTrue(isStatic(method), methodName + "should be static method");
        assertTrue(isPrivate(method), methodName + "should be private method");
    }

    @Test
    public void m02_03_assertPrivateMethodCorrectness() throws InvocationTargetException, IllegalAccessException {
        final String methodName = "add";
        final Optional<Class<?>> maybeClass = getAppClass();
        assertTrue(maybeClass.isPresent());
        Class<?> aClass = maybeClass.get();
        Optional<Method> maybeMethod = findMethod(aClass, methodName, int[].class);
        assertTrue(maybeMethod.isPresent(), methodName + " should be present in " + aClass.getCanonicalName());

        final Method method = maybeMethod.get();
        method.setAccessible(true);

        @SuppressWarnings("PrimitiveArrayArgumentToVarargsMethod")
        int sum123 = (int) method.invoke(aClass, new int[]{1, 2, 3});
        assertEquals(6, sum123, "1 + 2 + 3 should be 6, got " + sum123);
    }

    @Test
    public void m02_04_assertBestLoanRatesExistence() {
        final String classToFind = "com.h2.BestLoanRates";
        final Optional<Class<?>> maybeClass = getBestLoanRatesClass();
        assertTrue(maybeClass.isPresent(), classToFind + " should be present");

        Class<?> aClass = maybeClass.get();
        assertEquals(classToFind, aClass.getCanonicalName());
        assertTrue(isPublic(aClass));
    }

    @Test
    public void m2_05_assertMainMethodExistence() {
        final String main = "main";
        final Optional<Class<?>> maybeClass = getBestLoanRatesClass();
        assertTrue(maybeClass.isPresent(), "com.h2.BestLoanRates class must be present");
        Class<?> c = maybeClass.get();
        List<Method> methods = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.getName().equals(main))
                .collect(Collectors.toList());

        assertEquals(1, methods.size(), main + " must be defined as a method in " + c.getCanonicalName());

        final Method method = methods.get(0);
        assertEquals(void.class, method.getReturnType(), main + " must return 'void' as the return type");
        assertTrue(isStatic(method), main + " must be a static method");
        assertTrue(isPublic(method), main + " must be a public method");

        Class<?>[] parameterTypes = method.getParameterTypes();
        assertEquals(1, parameterTypes.length, main + " should accept 1 parameter");
        assertEquals(String[].class, parameterTypes[0], main + " parameter type should be of 'String[]' type");
    }

    @Test
    public void m2_06_assertMainMethodPromptsName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        final String name = "H2";
        final int age = 32;
        final String testString = name + "\n" + age;
        provideInput(testString);

        final Optional<Class<?>> maybeClass = getBestLoanRatesClass();
        assertTrue(maybeClass.isPresent(), "com.h2.BestLoanRates class must be present");
        Class<?> c = maybeClass.get();

        Method main = c.getMethod("main", String[].class);
        //noinspection JavaReflectionInvocation
        main.invoke(null, (Object) null);

        List<String> outputList = Arrays.stream(getOutput().split("\\R")).collect(Collectors.toList());
        assertEquals("Enter your name", outputList.get(0));
        assertEquals("Hello " + name, outputList.get(1));
    }

    @Test
    public void m2_07_testBestRatesExistence() throws IllegalAccessException {
        String bestRates = "bestRates";
        final Optional<Class<?>> maybeClass = getBestLoanRatesClass();
        assertTrue(maybeClass.isPresent(), "com.h2.BestLoanRates class must be present");
        Class<?> c = maybeClass.get();
        List<Field> fields = Arrays.stream(c.getFields())
                .filter(f -> f.getName().equals(bestRates))
                .collect(Collectors.toList());

        assertEquals(1, fields.size(), bestRates + " must be defined as a field in CommandLineApp.");

        Field field = fields.get(0);
        assertEquals(java.util.Map.class, field.getType(), bestRates + " must be of type 'Map'");

        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<Integer, Float> fieldValues = (Map<Integer, Float>) field.get(c);

        assertEquals(3, fieldValues.size(), bestRates + " should have 3 entries");
        assertEquals(5.50f, fieldValues.get(1), bestRates + " should return '5.50f' for key '1'");
        assertEquals(3.45f, fieldValues.get(2), bestRates + " should return '3.45f' for key '2'");
        assertEquals(2.67f, fieldValues.get(3), bestRates + " should return '2.67f' for key '3'");
    }

    @Test
    public void m2_08_testGetRatesMethodExistence() {
        String getRates = "getRates";
        final Optional<Class<?>> maybeClass = getBestLoanRatesClass();
        assertTrue(maybeClass.isPresent(), " com.h2.BestLoanRates should be present");
        Class<?> c = maybeClass.get();
        List<Method> methods = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.getName().equals(getRates))
                .collect(Collectors.toList());

        assertEquals(1, methods.size(), getRates + " must be defined as a method in " + c.getCanonicalName());

        final Method method = methods.get(0);
        assertEquals(float.class, method.getReturnType(), getRates + " must return 'float' as the return type");
        assertTrue(isStatic(method), getRates + " must be a static method");
        assertTrue(isPublic(method), getRates + " must be a public method");

        Class<?>[] parameterTypes = method.getParameterTypes();
        assertEquals(1, parameterTypes.length, getRates + " should accept 1 parameter");
        assertEquals(int.class, parameterTypes[0], getRates + " parameter type should be of 'int' type");
    }

    @Test
    public void m2_09_testGetRatesMethodCorrectness() throws InvocationTargetException, IllegalAccessException {
        String getRates = "getRates";
        final Optional<Class<?>> maybeClass = getBestLoanRatesClass();
        assertTrue(maybeClass.isPresent(), " com.h2.BestLoanRates should be present");
        Class<?> c = maybeClass.get();
        List<Method> methods = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.getName().equals(getRates))
                .collect(Collectors.toList());

        assertEquals(1, methods.size(), getRates + " must be defined as a method in " + c.getCanonicalName());

        final Method method = methods.get(0);
        float bestRate1Yr = (float) method.invoke(null, 1);
        assertEquals(5.50f, bestRate1Yr, "best rate for '1 year' must be '5.50f'");

        float bestRate2Yr = (float) method.invoke(null, 2);
        assertEquals(3.45f, bestRate2Yr, "best rate for '2 year' must be '3.45f'");

        float bestRate3Yr = (float) method.invoke(null, 3);
        assertEquals(2.67f, bestRate3Yr, "best rate for '3 year' must be '2.67f'");

        float bestRate100Yr = (float) method.invoke(null, 100);
        assertEquals(0.0f, bestRate100Yr, "best rate for '100 year' must be '0.0ff'");
    }

    @Test
    public void m2_10_testNameAndLoanTerm() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String main = "main";
        final Optional<Class<?>> maybeClass = getBestLoanRatesClass();
        assertTrue(maybeClass.isPresent(), " com.h2.BestLoanRates should be present");
        Class<?> c = maybeClass.get();

        List<Method> methods = Arrays.stream(c.getDeclaredMethods())
                .filter(m -> m.getName().equals(main))
                .collect(Collectors.toList());

        assertEquals(1, methods.size(), main + " must be defined as a method in " + c.getCanonicalName());
        {
            final String name = "H2";
            final int loanTermInYears = 2;
            final String testString = name + "\n" + loanTermInYears;
            provideInput(testString);

            Method method = c.getMethod("main", String[].class);
            method.invoke(null, (Object) new String[0]);

            List<String> outputList = Arrays.stream(getOutput().split("\n")).collect(Collectors.toList());

            assertEquals(4, outputList.size(), "There must be 4 statements on console - 1 for asking name, 1 for printing name back, 1 for asking loan term, 1 for printing best available rates (strictly in this order!)");

            assertEquals("Enter your name", outputList.get(0));
            assertEquals("Hello " + name, outputList.get(1));

            assertEquals("Enter the loan term (in years)", outputList.get(2));
            assertEquals("Best Available Rate: 3.45%", outputList.get(3));

        }
        {
            setUpOutput(); // reset the output stream!

            final String name = "H2";
            final int loanTermInYears = 20;
            final String testString = name + "\n" + loanTermInYears;
            provideInput(testString);

            Method method = c.getMethod("main", String[].class);
            method.invoke(null, (Object) new String[0]);


            List<String> outputList = Arrays.stream(getOutput().split("\n")).collect(Collectors.toList());

            assertEquals(4, outputList.size(),  "There must be 4 statements on console - 1 for asking name, 1 for printing name back, 1 for asking loan term, 1 for printing no available rates for term (strictly in this order!)");

            assertEquals("Enter your name", outputList.get(0));
            assertEquals("Hello " + name, outputList.get(1));

            assertEquals("Enter the loan term (in years)", outputList.get(2));
            assertEquals("No available rates for term: " + loanTermInYears + " years", outputList.get(3));

        }
    }
}
