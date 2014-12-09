# FireWorks

A simple simulation of a rocket factory using [JMS](https://jms-spec.java.net). There are four different type of people responsible for the creation of a rocket:

- Supplier
- Worker
- Quality Tester
- Logistician

Suppliers can be created using the Graphical User Interface `FireWorks`. Workers, Quality Testers, and Logistician are all simple command line programs.

## Requirements

To compile and execute the programs contained in this repository you need the following software components:

- [WildlFly 8.2](http://www.wildfly.org)
- [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
- [Maven](http://maven.apache.org)

## Configuration

1. Add an application user

- Unix: `add-user.sh -a -u 'fireworks' -p 'fireworks' -g 'guest'`
- Windows: `add-user.bat  -a -u 'fireworks' -p 'fireworks' -g 'guest'`

2. Start the web server

- Unix: `standalone.sh -c standalone-full.xml`
- Windows: `standalone.bat -c standalone-full.xml`

3. Add the configuration data to the web server

- Unix: `jboss-cli.sh --connect --file=configure-jms.cli`
- Windows: `jboss-cli.bat --connect --file=configure-jms.cli`

If you want to remove the configuration data you can do so by issuing the following command while the server is running:

- Unix: `jboss-cli.sh --connect --file=remove-jms.cli`
- Windows: `jboss-cli.bat --connect --file=remove-jms.cli`

## Compilation

Use the following command to compile all programs contained in the repository:

```bash
mvn compile
```

## Execution

Before executing any of the following commands please make sure that WildFly is running and that you followed the steps outlined in the section configuration.

### FireWorks GUI

To start the graphical user interface you can use the following Maven command:

```bash
mvn exec:java -PFireWorks
```

To add suppliers use the button `New Order`. You can edit the different attributes for a supplier by double clicking the cell for the corresponding attribute. If you are happy with the current selection of suppliers you can use the button `Start Suppliers` to activate them.


###  Worker

To start a worker using the default ID specified in `pom.xml` use the following command:

```bash
mvn exec:java -PWorker
```

You can also specify the ID when you start a worker. The following command starts a worker with ID 3:

```bash
mvn exec:java -PWorker -Dworker.id=3
```

###  Quality Tester

The command line arguments for the quality tester are the same as the ones for the worker. To start a tester you can use the Maven profile `Tester`. The following command starts a quality tester using the default arguments specified in `pom.xml`.

```bash
mvn exec:java -PTester
```

###  Logistician

The logistician has the same command line arguments as the worker and tester. You can use the Maven profile `Logistic` to start a logistic worker:

```bash
mvn exec:java -PLogistic
```
