---
title: "Effective Java: Static Factory Method (Item 1)"
description: Veja os benefícios que o uso do static factory method pode trazer para a sua API Java 
image: static-factory.jpg
layout: post
language: pt-BR
tags: [effective-java, bookclub]
author: mcruzdev
---

# Static Factory Method

### Agradecimentos

No nosso primeiro encontro do QuarkusClub BookClub falamos sobre o primeiro item do livro Effective Java.
E gostaria de deixar os meus agradecimentos aos membros da comunidade que participaram.

### Assunto do segundo capítulo

Vamos ao que interessa, vou deixar aqui um resumo bem legal do item 1 que fala sobre Static Factory Method. 
Os itens do segundo capítulo pretende falar sobre a criação e morte dos objetos criados no nosso código Java.

## Item 1: Considere usar Static Factory Method ao invés de construtores

Como bons desenvolvedores Java, normalmente criamos nossos objetos através da palavra reservada `new` e as vezes nem isso,
delegamos o ciclo de vida dos nossos objetos para o CDI, mas de qualquer é quase impossível você não escrever `new` nos seus projetos Java (_e as vezes nem isso, você delega pra IA 😝_).

### Primeira vantagem

**Static Factory Methods possuem nomes, construtores não!**

O livro traz o exemplo do construtor `BigInteger(int, int, Random)` que retorna um `BigInteger` que é provavelmente um número primo.
E diz que seria melhor expresso se houvesse um `BigInteger.probablePrime`.

Se você, assim como eu, não usa sempre esse construtor de `BigInteger`, 
provavelmente você ia usar a sua IDE para navegar para dentro do código e olhar a documentação, ou até mesmo a implementação do método para entender o que ele faz.

> Bons nomes tornam sua API mais expressiva e deixam clara a intenção por trás do seu design.


### Segunda vantagem

**Você sempre cria uma nova instância com `new`, já o Static Factory Method te dá mais opções!**

Um exemplo disso é o método da API do Java `Boolean.valueOf(boolean)`, 
os objetos já estão criados, 
você só retorna algo que já existe.


```java

public static final Boolean TRUE = new Boolean(true);

public static final Boolean FALSE = new Boolean(false);

public static Boolean valueOf(String s) {
    return parseBoolean(s) ? TRUE : FALSE;
}
```

Isso é super interessante pra quando você não quer ficar criando aquele objeto custoso de ser criado,
um exemplo disso é o nosso querido `com.fasterxml.jackson.databind.ObjectMapper`,
normalmente você não quer criar um toda vez que precisar,
você utiliza um que já existe através de um static factory method.

Classes que fazem isso são chamadas de `instance-controlled`,
se você é experiente já deve ter percebido que isso permite a gente ter um `Singleton`:

```java

public class Engine {
    
    private Engine() {} // 1
    
    private static Engine engine;
    
    public static Enginer instance() {
        if (engine == null) { // 2
            engine = newEngine(); // 3
        }
        return engine;
    }
}
```

1. Só eu tenho o poder de criar um objeto do tipo `Engine`
2. Se não existir eu mesmo crio
3. Se existir eu apenas retorno

### Terceira vantagem

**Static Factory Methods podem retornar qualquer sub-tipo do tipo de retorno, construtores não!**

Esse aqui é bem simples e eu vou ter que usar o famoso exemplo de OOP (Object Oriented Programming), 
vamos pensar numa classe `Pessoa`, eu posso ter `PessoaFisica` e `PessoaJuridica`, brincadeira vamos pensar em algo diferente:


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

Dessa forma, eu escondo do meu usuário as implementações e tenho uma API muitos mais compacta, 
e outra nós estamos baseando nossa API sobre interfaces e não sobre implementações.

O livro traz o exemplo do Java Collections Framework que possui 45 implementações (esse valor pode estar diferente hoje) utilitárias de suas interfaces,
fornecendo coleções imutáveis, sincronizadas, etc.

Quase todas essas implementações são expostas por meio de métodos de fábrica estáticos em uma única classe que não pode ser instanciada (`java.util.Collections`).

### Quarta vantagem

**O objeto retornado de um Static Factory Method pode variar dependendo dos argumentos!**

Essa quarta vantagem é bem interessante para quando a gente quer esconder coisas dos usuários das nossas APIs,
e mais uma vez o nosso usuário vai se basear sempre na abstração e não na implementação.

Vamos usar o mesmo exemplo que o livro traz pra gente, vamos usar a API da class `EnumSet`:

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

1. A class `EnumSet` é uma class `sealed` que permite apenas `JumboEnumSet` e `RegularEnumSet` de extender os seus comportamentos e atributos.
2. Se o enum passado para `noneOf` conter 64 elementos ou menos vai ser criado um `RegularEnumSet` caso contrário, será criado um `JumboEnumSet`.

Isso fica escondido da gente, você nem consegue criar um `JumboEnumSet` ele é `package-private` a API vai se virar para me dar o `EnumSet` adequado para o tamanho do meu enum.

### Quinta vantagem

**A classe do tipo de retorno, não precisa existir quando a classe que contém o método é escrita!**

Esse aqui é um pouco mais complicado pra entender, 
pois ele aplica essa vantabem em combinação com frameworks que permitem o desacoplamento de clientes de implementações.

O livro traz o [Service Provider Interface](https://www.baeldung.com/java-spi) como exemplo de framework que podemos usar, 
vamos pensar que eu quero apenas prover para os meus usuários uma interface e permitir que ele mesmo ou outra API forneça a implementação.

Eu declaro minha interface:

```java
package io.github.quarkusclub;

public interface InputCustomizer {
    void handle(Input input);
}
```

Declaro como o meu static factory method:

```java
public static List<InputCustomizer> inputCustomizers() {
    return ServiceLoader.load(InputCustomizer.class).stream().toList();
}
```

E o meu usuário ou qualquer outra biblioteca pode fornecer para mim uma implementação de `InputCustomizer` e customizar o `Input`.

```java
package io.github.quarkusclub.effectivejava;

import io.github.quarkusclub.InputCustomizer;

public class AddOwnMetadataCustomizer implements InputCustomizer {
    
    public handle(Input input) {
        input.addMetadata("community", "quarkusclub");
    }
}
```

E dentro de **META-INF/services/io.github.quarkusclub.InputCustomizer** eu informo a minha implementação:

```text

io.github.quarkusclub.effectivejava.AddOwnMetadataCustomizer
```

## Desvantagens

Nem tudo são flores e sempre vamos ter um trade-off, no caso da nossa primeira desvantagem é como um mal que vem para o bem.

### Primeira desvantagem

As classes que a gente vai escrever os nossos Static Factory Methods não possuem construtores `public` ou `protected` que nos permitem ter subclasses.
O autor diz que isso é algo como uma coisa boa disfarçada, isso ajuda pessoas a usarem composição á herança.


### Segunda desvantagem

A segunda desvantagem é que static factory methods são difíceis de serem encontrados, 
ele diz que o **javadoc** não gera a documentação para os nossos static factory methods,
(isso é algo que precisamos validar nos dias de hoje), alguns plugins maven como o `maven-javadoc-plugin` geram isso pra gente facilmente.

Essa desvantagem,
por exemplo, 
pode ser muito bem contornada por IDEs como o [IntelliJ IDEA](https://www.jetbrains.com/pt-br/idea/) ou VSCode que permitem a gente visualizar a documentação de um método, 
durante o uso.

A parte mais legal é que pra essa desvantagem, o livro da uma lista de nomes e quando utilizar pra gente:

### Convenções comuns de nomes para Static Factory Method:

-   **`from`**: Um método de conversão de tipo que recebe um único
    parâmetro e retorna uma instância correspondente deste tipo.

``` java
Date d = Date.from(instant);
```

-   **`of`**: Um método de agregação que recebe múltiplos parâmetros
    e retorna uma instância do tipo que os incorpora.

``` java
Set<Rank> faceCards = EnumSet.of(JACK, QUEEN, KING);
```

-   **`valueOf`**: Uma alternativa mais verbosa a `from` e `of`.

``` java
BigInteger prime = BigInteger.valueOf(Integer.MAX_VALUE);
```

-   **`instance`** ou **`getInstance`**: Retorna uma instância
    descrita por seus parâmetros (se houver), mas que não
    retorna necessariamente sempre o mesmo valor.

``` java
StackWalker luke = StackWalker.getInstance(options);
```

-   **`create`** ou **`newInstance`**: Semelhante a `instance` ou
    `getInstance`, exceto que o método garante que cada chamada retorna
    uma nova instância.

``` java
Object newArray = Array.newInstance(classObject, arrayLen);
```

-   **`getType`**: Semelhante a `getInstance`, mas usado quando o
    **static factory method** está em uma classe diferente. `Type` representa o
    tipo de objeto retornado pelo **static factory method**.

``` java
FileStore fs = Files.getFileStore(path);
```

-   **`newType`**: Semelhante a `newInstance`, mas usado quando o
    **static factory method** está em uma classe diferente. `Type` representa o
    tipo de objeto retornado pelo **static factory method**.

``` java
BufferedReader br = Files.newBufferedReader(path);
```

-   **`type`**: Uma alternativa mais concisa a `getType` e `newType`.

``` java
List<Complaint> litany = Collections.list(legacyLitany);
```


## Considerações finais

Como comentado no último encontro, esse item é um dos itens com mais conteúdo e vale muito a pena ver cada detalhe de perto.
Só nesse capítulo se a gente se aprofundar, podemos ver conceitos como, programar orientado a interface e não a implementação, 
encapsulamento e ocultação de informações, etc.

Esse é o primeiro de muitos itens que vão ser documentados no nosso blog, até daqui a pouco, valeu!

