---
title: "Effective Java: Static Factory Method (Item 1)"
description: See the benefits that using static factory methods can bring to your Java API 
image: static-factory.jpg
layout: post
language: en
tags: [effective-java, bookclub]
author: mcruzdev
---

# Static Factory Method

### Acknowledgments

In our first QuarkusClub BookClub meeting, we discussed the first item from the Effective Java book.
I would like to express my gratitude to the community members who participated.

### Topic of the Second Chapter

Let's get to what matters. I'll provide a summary of Item 1, which discusses Static Factory Methods. 
The items in the second chapter aim to discuss the creation and destruction of objects created in our Java code.

## Item 1: Consider Static Factory Methods Instead of Constructors

As good Java developers, we typically create our objects using the `new` keyword, and sometimes not even that -
we delegate the lifecycle of our objects to CDI. But either way, it's almost impossible not to write `new` in your Java projects (_and sometimes not even that, you delegate it to AI 😝_).

### First Advantage

**Static Factory Methods have names, constructors don't!**

The book provides the example of the `BigInteger(int, int, Random)` constructor that returns a `BigInteger` that is probably a prime number.
It says it would be better expressed if there were a `BigInteger.probablePrime`.

If you, like me, don't always use this `BigInteger` constructor, 
you would probably use your IDE to navigate into the code and look at the documentation, or even the method implementation to understand what it does.

> Good names make your API more expressive and clarify the intention behind your design.

With Static Factory Method you clearly express what you want to do with your code.


### Second Advantage

**You always create a new instance with `new`, but Static Factory Methods give you more options!**

An example of this is the Java API method `Boolean.valueOf(boolean)`, 
the objects are already created, 
you just return something that already exists.


```java

public static final Boolean TRUE = new Boolean(true);

public static final Boolean FALSE = new Boolean(false);

public static Boolean valueOf(String s) {
    return parseBoolean(s) ? TRUE : FALSE;
}
```

This is super interesting when you don't want to keep creating that expensive object,
an example of this is our beloved `com.fasterxml.jackson.databind.ObjectMapper`,
normally you don't want to create one every time you need it,
you use one that already exists through a static factory method.

Classes that do this are called `instance-controlled`,
if you're experienced you've probably already noticed that this allows us to have a `Singleton`:

```java

public class Engine {
    
    private Engine() {} // 1
    
    private static Engine engine;
    
    public static Engine instance() {
        if (engine == null) { // 2
            engine = new Engine(); // 3
        }
        return engine;
    }
}
```

1. Only I have the power to create an `Engine` object
2. If it doesn't exist, I create it myself
3. If it exists, I just return it

### Third Advantage

**Static Factory Methods can return any subtype of the return type, constructors cannot!**

This one is quite simple and I'll have to use the famous OOP (Object Oriented Programming) example, 
let's think of a `Person` class, I could have `IndividualPerson` and `CorporatePerson`, just kidding, let's think of something different:


```java
public interface Writer {}

public class Writers {

    private Writers() {}

    public void writer(String message);

    private static class ConsoleWriter implements Writer { /* ... */ }

    private static class FileWriter implements Writer { /* ... */ }

    public static Writer consoleWriter() { /* ... */ }

    public static Writer fileWriter() { /* ... */ }
}
```

This way, I hide the implementations from my user and have a much more compact API, 
and another thing - we're basing our API on interfaces and not on implementations.

The book provides the example of the Java Collections Framework which has 45 utility implementations (this number may be different today) of its interfaces,
providing immutable, synchronized collections, etc.

Almost all of these implementations are exposed through static factory methods in a single non-instantiable class (`java.util.Collections`).

### Fourth Advantage

**The object returned from a Static Factory Method can vary depending on the arguments!**

This fourth advantage is very interesting when we want to hide things from the users of our APIs,
and once again our user will always rely on the abstraction and not the implementation.

Let's use the same example the book provides, let's use the `EnumSet` class API:

```java
// java 21
public abstract sealed class EnumSet<E extends Enum<E>> extends AbstractSet<E>
        implements Cloneable, java.io.Serializable permits JumboEnumSet, RegularEnumSet {} // 1

public static <E extends Enum<E>> EnumSet<E> noneOf(Class<E> elementType) {
    Enum<?>[] universe = getUniverse(elementType);
    if (universe == null)
        throw new ClassCastException(elementType + " not an enum");

    if (universe.length <= 64) // 2
        return new RegularEnumSet<>(elementType, universe);
    else
        return new JumboEnumSet<>(elementType, universe);
}
```

1. The `EnumSet` class is a `sealed` class that only allows `JumboEnumSet` and `RegularEnumSet` to extend its behaviors and attributes.
2. If the enum passed to `noneOf` contains 64 elements or fewer, a `RegularEnumSet` will be created, otherwise a `JumboEnumSet` will be created.

This is hidden from us, you can't even create a `JumboEnumSet` - it's `package-private`. The API will figure out how to give me the appropriate `EnumSet` for the size of my enum.

### Fifth Advantage

**The class of the return type doesn't need to exist when the class containing the method is written!**

This one is a bit more complicated to understand, 
as it applies this advantage in combination with frameworks that allow decoupling of clients from implementations.

The book provides the [Service Provider Interface](https://www.baeldung.com/java-spi) as an example of a framework we can use, 
let's think that I only want to provide an interface to my users and allow them or another API to provide the implementation.

I declare my interface:

```java
package io.github.quarkusclub;

public interface InputCustomizer {
    void handle(Input input);
}
```

I declare my static factory method:

```java
public static List<InputCustomizer> inputCustomizers() {
    return ServiceLoader.load(InputCustomizer.class).stream().toList();
}
```

And my user or any other library can provide me with an implementation of `InputCustomizer` and customize the `Input`.

```java
package io.github.quarkusclub.effectivejava;

import io.github.quarkusclub.InputCustomizer;

public class AddOwnMetadataCustomizer implements InputCustomizer {
    
    public handle(Input input) {
        input.addMetadata("community", "quarkusclub");
    }
}
```

And inside **META-INF/services/io.github.quarkusclub.InputCustomizer** I specify my implementation:

```text

io.github.quarkusclub.effectivejava.AddOwnMetadataCustomizer
```

## Disadvantages

Not everything is perfect and we'll always have trade-offs. In the case of our first disadvantage, it's like a blessing in disguise.

### First Disadvantage

The classes where we write our Static Factory Methods don't have `public` or `protected` constructors that allow us to have subclasses.
The author says this is somewhat of a good thing in disguise, as it encourages people to use composition over inheritance.


### Second Disadvantage

The second disadvantage is that static factory methods are hard to find, 
he says that **javadoc** doesn't generate documentation for our static factory methods,
(this is something we need to validate nowadays), some Maven plugins like `maven-javadoc-plugin` generate this for us easily.

This disadvantage,
for example, 
can be very well mitigated by IDEs like [IntelliJ IDEA](https://www.jetbrains.com/pt-br/idea/) or VSCode that allow us to view a method's documentation 
during use.

The best part is that for this disadvantage, the book provides a list of names and when to use them:

### Common Naming Conventions for Static Factory Methods:

-   **`from`**: A type-conversion method that takes a single
    parameter and returns a corresponding instance of this type.

``` java
Date d = Date.from(instant);
```

-   **`of`**: An aggregation method that takes multiple parameters
    and returns an instance of the type that incorporates them.

``` java
Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);
```

-   **`valueOf`**: A more verbose alternative to `from` and `of`.

``` java
BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);
```

-   **`instance`** or **`getInstance`**: Returns an instance
    described by its parameters (if any), but doesn't
    necessarily always return the same value.

``` java
StackWalker luke = StackWalker.getInstance(options);
```

-   **`create`** or **`newInstance`**: Similar to `instance` or
    `getInstance`, except that the method guarantees that each call returns
    a new instance.

``` java
Object newArray = Array.newInstance(classObject, arrayLen);
```

-   **`getType`**: Similar to `getInstance`, but used when the
    **static factory method** is in a different class. `Type` represents the
    type of object returned by the **static factory method**.

``` java
FileStore fs = Files.getFileStore(path);
```

-   **`newType`**: Similar to `newInstance`, but used when the
    **static factory method** is in a different class. `Type` represents the
    type of object returned by the **static factory method**.

``` java
BufferedReader br = Files.newBufferedReader(path);
```

-   **`type`**: A more concise alternative to `getType` and `newType`.

``` java
List<Complaint> litany = Collections.list(legacyLitany);
```


## Final Considerations

As discussed in the last meeting, this item is one of the items with the most content and it's really worth looking at each detail closely.
Just in this chapter, if we dive deeper, we can see concepts like programming to interfaces rather than implementations, 
encapsulation and information hiding, etc.

This is the first of many items that will be documented on our blog. See you soon!
