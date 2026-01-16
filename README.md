# Web3j-unit [![Build Status](https://github.com/web3j/web3j-unit/actions/workflows/build.yml/badge.svg)](https://github.com/web3j/web3j-unit/actions/workflows/build.yml)

Web3j-unit is a [Junit 5](https://junit.org/junit5/docs/current/user-guide/) extension to streamline the creation of Ethereum contract tests.

Multiple Ethereum implementations are supported including Geth and Besu. To run tests built using Web3j-unit, **docker is required** on the host.

Instances of `Web3j`, `TransactionManager` and `GasProvider` are injected into the Junit runner.

You can find a sample [here](https://github.com/web3j/web3j-unitexample).

You can find an example using docker-compose [here](https://github.com/web3j/web3j-unit-docker-compose-example). This spins up VMWare Concord nodes using a docker-compose file. 

### Getting Started

1. Add dependency to gradle.

```groovy
   repositories {
      mavenCentral()
      maven { url "https://hyperledger.jfrog.io/artifactory/besu-maven/" }
      maven { url "https://artifacts.consensys.net/public/maven/maven/" }
      maven { url "https://splunk.jfrog.io/splunk/ext-releases-local" }
      maven { url "https://dl.cloudsmith.io/public/consensys/quorum-mainnet-launcher/maven/" }
   }

   implementation "org.web3j:core:4.14.0"
   testCompile "org.web3j:web3j-unit:4.14.0"
```

2. Create a new test with the `@EVMTest` annotation. An embedded EVM is used by default. To use Geth or Besu pass the node type into the annotation: `@EVMTest(NodeType.GETH)` or `@EVMTest(NodeType.BESU)`

```kotlin
@EVMTest
class GreeterTest {

}
```

3. Inject instance of `Web3j` `TransactionManager` and `ContractGasProvider` in your test method.

```kotlin
@EVMTest
class GreeterTest {

    @Test
    fun greeterDeploys(
        web3j: Web3j,
        transactionManager: TransactionManager,
        gasProvider: ContractGasProvider
    ) {}

}
```

4. Deploy your contract in the test.

```kotlin
@EVMTest
class GreeterTest {

    @Test
    fun greeterDeploys(
        web3j: Web3j,
        transactionManager: TransactionManager,
        gasProvider: ContractGasProvider
    ) {
        val greeter = Greeter.deploy(web3j, transactionManager, gasProvider, "Hello EVM").send()
        val greeting = greeter.greet().send()
        assertEquals("Hello EVM", greeting)
    }

}
```

5. Run the test!

### Using a custom docker-compose file

1. Add dependency to gradle.
   
```groovy
  repositories {
     mavenCentral()
  }

  implementation "org.web3j:core:5.0.1"
  testCompile "org.web3j:web3j-unit:5.0.1"
```

2. Create a new test with the `@EVMComposeTest` annotation.
By default, uses `test.yml` file in the project home, and runs `web3j` on service name `node1` exposing the port `8545`. 
Can be customised to use specific docker-compose file, service name and port by `@EVMComposeTest("src/test/resources/geth.yml", "ethnode1", 8080)`
Here, we connect to the service named `ethnode1` in the `src/test/resources/geth.yml` docker-compose file which exposes the port `8080` for `web3j` to connect to. 

```kotlin
@EVMComposeTest("src/test/resources/geth.yml", "ethnode1", 8080)
class GreeterTest {

}
```

3. Inject instance of `Web3j` `TransactionManager` and `ContractGasProvider` in your test method.

```kotlin
@EVMComposeTest("src/test/resources/geth.yml", "ethnode1", 8080)
class GreeterTest {

    @Test
    fun greeterDeploys(
        web3j: Web3j,
        transactionManager: TransactionManager,
        gasProvider: ContractGasProvider
    ) {}

}
```

4. Deploy your contract in the test.

```kotlin
@EVMComposeTest("src/test/resources/geth.yml", "ethnode1", 8080)
class GreeterTest {

    @Test
    fun greeterDeploys(
        web3j: Web3j,
        transactionManager: TransactionManager,
        gasProvider: ContractGasProvider
    ) {
        val greeter = Greeter.deploy(web3j, transactionManager, gasProvider, "Hello EVM").send()
        val greeting = greeter.greet().send()
        assertEquals("Hello EVM", greeting)
    }

}
```

5. Run the test!
