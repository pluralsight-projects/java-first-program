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
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.commons.util.ReflectionUtils.*;

public class Module4_Test {
    private static final String classToFind = "com.h2.MortgageCalculator";
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

    public static Optional<Class<?>> getMortgageClass() {
        return getClass(classToFind);
    }

    @Test
    public void m04_01_testMortgageCalculatorExists() {
        Optional<Class<?>> maybeClass = getMortgageClass();
        assertTrue(maybeClass.isPresent(), classToFind + " must be created.");
    }

    @Test
    public void m04_02_testExistenceOfPrivateFields() {
        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();
        final Map<String, Class<?>> expectedFieldsToClass = Map.of(
                "loanAmount", long.class,
                "termInYears", int.class,
                "annualRate", float.class,
                "monthlyPayment", double.class
        );

        final Field[] declaredFields = mortgageCalculator.getDeclaredFields();
        assertEquals(4, declaredFields.length, "4 fields (loanAmount, termInYears, annualRate, monthlyPayment) should be available in " + classToFind);


        final Map<String, Class<?>> actualFieldsToClass = new HashMap<>();

        for (final Field field : declaredFields) {
            assertTrue(expectedFieldsToClass.containsKey(field.getName()), field.getName() + " is not a valid field name. It should be among (loanAmount, termInYears, annualRate, monthlyPayment)");
            assertTrue(isPrivate(field), field.getName() + " should be declared 'private'");
            actualFieldsToClass.put(field.getName(), field.getType());
        }

        expectedFieldsToClass.forEach((key, value) -> assertEquals(value, actualFieldsToClass.get(key), key + " must be of type " + value));

    }

    @Test
    public void m4_03_testMortgageConstructorAndCorrectness() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();
        final Constructor<?>[] constructors = mortgageCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        final Constructor<?> constructor = constructors[0];
        assertTrue(isPublic(constructor), "constructor must be declared 'public'");
        assertEquals(3, constructor.getParameterCount(), "Constructor should have 3 parameters");

        Parameter[] parameters = constructor.getParameters();
        assertEquals(long.class, parameters[0].getType(), "Constructor's first parameter should be of type 'long'");
        assertEquals(int.class, parameters[1].getType(), "Constructor's second parameter should be of type 'int'");
        assertEquals(float.class, parameters[2].getType(), "Constructor's third parameter should be of type 'float'");

        final long loanAmount = 100L;
        final int termInYears = 20;
        final float annualRate = 2.65f;

        Object instance = constructor.newInstance(loanAmount, termInYears, annualRate);

        final Field[] fields = mortgageCalculator.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            switch (field.getName()) {
                case "loanAmount":
                    assertEquals(loanAmount, (long) field.get(instance), "loanAmount should have value of " + loanAmount);
                    break;
                case "termInYears":
                    assertEquals(termInYears, (int) field.get(instance), "termInYears should have value of " + termInYears);
                    break;
                case "annualRate":
                    assertEquals(annualRate, (float) field.get(instance), "annualRate should have value of " + annualRate);
                    break;
            }
        }

    }

    @Test
    public void m4_04_testExistenceOfNumberOfPayments() {
        final String methodName = "getNumberOfPayments";

        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();

        final Method[] methods = mortgageCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method sumOfCredits = filteredMethod.get(0);
        assertTrue(isPrivate(sumOfCredits), methodName + " must be declared as 'private'");
        assertEquals(int.class, sumOfCredits.getReturnType(), methodName + " method must return a value of type 'int'");
    }

    @Test
    public void m4_05_testNumberOfPaymentsCorrectness() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();
        final Constructor<?>[] constructors = mortgageCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        final Constructor<?> constructor = constructors[0];
        assertTrue(isPublic(constructor), "constructor must be declared 'public'");
        assertEquals(3, constructor.getParameterCount(), "Constructor should have 3 parameters");

        Parameter[] parameters = constructor.getParameters();
        assertEquals(long.class, parameters[0].getType(), "Constructor's first parameter should be of type 'long'");
        assertEquals(int.class, parameters[1].getType(), "Constructor's second parameter should be of type 'int'");
        assertEquals(float.class, parameters[2].getType(), "Constructor's third parameter should be of type 'float'");

        final long loanAmount = 100L;
        final int termInYears = 20;
        final float annualRate = 2.65f;

        Object instance = constructor.newInstance(loanAmount, termInYears, annualRate);

        final String methodName = "getNumberOfPayments";
        final List<Method> filteredMethod = Arrays.stream(mortgageCalculator.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");
        Method method = filteredMethod.get(0);
        final float result = (int) invokeMethod(method, instance);
        int expected = 12 * termInYears;
        assertEquals(expected, result, methodName + " should return " + expected + " number of payments for termInYears of " + termInYears);
    }

    @Test
    public void m4_06_testExistenceOfMonthlyInterestRate() {
        final String methodName = "getMonthlyInterestRate";

        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();

        final Method[] methods = mortgageCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method method = filteredMethod.get(0);
        assertTrue(isPrivate(method), methodName + " must be declared as 'private'");
        assertEquals(float.class, method.getReturnType(), methodName + " method must return a value of type 'float'");
    }

    @Test
    public void m4_07_testMonthlyInterestRateCorrectness() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();
        final Constructor<?>[] constructors = mortgageCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        final Constructor<?> constructor = constructors[0];
        assertTrue(isPublic(constructor), "constructor must be declared 'public'");
        assertEquals(3, constructor.getParameterCount(), "Constructor should have 3 parameters");

        Parameter[] parameters = constructor.getParameters();
        assertEquals(long.class, parameters[0].getType(), "Constructor's first parameter should be of type 'long'");
        assertEquals(int.class, parameters[1].getType(), "Constructor's second parameter should be of type 'int'");
        assertEquals(float.class, parameters[2].getType(), "Constructor's third parameter should be of type 'float'");

        final long loanAmount = 100L;
        final int termInYears = 20;
        final float annualRate = 3.74f;

        Object instance = constructor.newInstance(loanAmount, termInYears, annualRate);

        final String methodName = "getMonthlyInterestRate";
        final List<Method> filteredMethod = Arrays.stream(mortgageCalculator.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");
        Method method = filteredMethod.get(0);
        final float result = (float) invokeMethod(method, instance);
        float expected = (annualRate / 100) / 12;
        assertEquals(expected, result, methodName + " should return " + expected + " as monthly interest rate for a annualRate of " + annualRate);
    }

    @Test
    public void m4_08_testExistenceOfCalculateMonthlyPayment() {
        final String methodName = "calculateMonthlyPayment";

        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();

        final Method[] methods = mortgageCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method method = filteredMethod.get(0);
        assertTrue(isPublic(method), methodName + " must be declared as 'public'");
        assertEquals(void.class, method.getReturnType(), methodName + " method must return a value of type 'void'");
    }

    @Test
    public void m4_09_testCalculateMonthlyPaymentCorrectness() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();
        final Constructor<?>[] constructors = mortgageCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        final Constructor<?> constructor = constructors[0];
        assertTrue(isPublic(constructor), "constructor must be declared 'public'");
        assertEquals(3, constructor.getParameterCount(), "Constructor should have 3 parameters");

        Parameter[] parameters = constructor.getParameters();
        assertEquals(long.class, parameters[0].getType(), "Constructor's first parameter should be of type 'long'");
        assertEquals(int.class, parameters[1].getType(), "Constructor's second parameter should be of type 'int'");
        assertEquals(float.class, parameters[2].getType(), "Constructor's third parameter should be of type 'float'");

        final long loanAmount = 264000;
        final int termInYears = 30;
        final float annualRate = 3.74f;

        Object instance = constructor.newInstance(loanAmount, termInYears, annualRate);

        final String methodName = "calculateMonthlyPayment";
        final List<Method> filteredMethod = Arrays.stream(mortgageCalculator.getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");
        Method method = filteredMethod.get(0);
        invokeMethod(method, instance);
        double expected = 1221.1400903847025;

        String fieldName = "monthlyPayment";
        Field field = mortgageCalculator.getDeclaredField(fieldName);
        field.setAccessible(true);

        double actual = field.getDouble(instance);
        assertNotEquals(0.0, actual, fieldName + " should not be 0.0");

        boolean areAlmostSame = Math.abs(expected - actual) < 0.001;
        assertTrue(areAlmostSame, fieldName + " should be " + expected + " (or with a margin of +0.001), but was " + actual);
    }

    @Test
    public void m4_10_testExistenceOfToString() {
        final String methodName = "toString";

        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();

        final Method[] methods = mortgageCalculator.getDeclaredMethods();
        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());

        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final Method method = filteredMethod.get(0);
        assertTrue(isPublic(method), methodName + " must be declared as 'public'");
        assertEquals(String.class, method.getReturnType(), methodName + " method must return a value of type 'String'");
    }

    @Test
    public void m4_11_testToStringCorrectness() throws IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();
        final Constructor<?>[] constructors = mortgageCalculator.getDeclaredConstructors();

        assertEquals(1, constructors.length, classToFind + " should have 1 constructor");

        final Constructor<?> constructor = constructors[0];
        assertTrue(isPublic(constructor), "constructor must be declared 'public'");
        assertEquals(3, constructor.getParameterCount(), "Constructor should have 3 parameters");

        Parameter[] parameters = constructor.getParameters();
        assertEquals(long.class, parameters[0].getType(), "Constructor's first parameter should be of type 'long'");
        assertEquals(int.class, parameters[1].getType(), "Constructor's second parameter should be of type 'int'");
        assertEquals(float.class, parameters[2].getType(), "Constructor's third parameter should be of type 'float'");

        final long loanAmount = 264000;
        final int termInYears = 30;
        final float annualRate = 3.74f;

        Object instance = constructor.newInstance(loanAmount, termInYears, annualRate);
        {
            final String methodName = "calculateMonthlyPayment";
            final List<Method> filteredMethod = Arrays.stream(mortgageCalculator.getDeclaredMethods())
                    .filter(method -> method.getName().equals(methodName))
                    .collect(Collectors.toList());

            assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");
            Method method = filteredMethod.get(0);
            invokeMethod(method, instance);
        }

        {
            final String methodName = "toString";
            final List<Method> filteredMethod = Arrays.stream(mortgageCalculator.getDeclaredMethods())
                    .filter(method -> method.getName().equals(methodName))
                    .collect(Collectors.toList());

            assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");
            Method method = filteredMethod.get(0);
            final String result = (String) invokeMethod(method, instance);
            final String expected = "monthlyPayment: 1221.14";
            assertEquals(expected, result, methodName + " should return " + expected);
        }
    }

    @Test
    public void m4_12_testMainMethodExists() {
        final String methodName = "main";

        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();

        final Method[] methods = mortgageCalculator.getDeclaredMethods();
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
    public void m4_13_testMainMethodPrintsCorrectMortgageAmount() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final String methodName = "main";

        final Optional<Class<?>> maybeMortgageCalculator = getMortgageClass();
        assertTrue(maybeMortgageCalculator.isPresent(), classToFind + " must exist");
        final Class<?> mortgageCalculator = maybeMortgageCalculator.get();

        final Method[] methods = mortgageCalculator.getDeclaredMethods();

        final List<Method> filteredMethod = Arrays.stream(methods).filter(method -> method.getName().equals(methodName)).collect(Collectors.toList());
        assertEquals(1, filteredMethod.size(), classToFind + " should contain a method called '" + methodName + "'");

        final String loanAmount = "264000";
        final String termInYears = "30";
        final String annualRate = "3.74";

        Method method = mortgageCalculator.getMethod("main", String[].class);
        method.invoke(null, (Object) new String[]{loanAmount, termInYears, annualRate});

        assertEquals("monthlyPayment: 1221.14" + System.lineSeparator(), testOut.toString());
    }
}
