package github.organisation.scanner;

import github.organisation.scanner.model.Person;
import github.organisation.scanner.model.Repository;
import github.organisation.scanner.repository.PersonRepository;
import github.organisation.scanner.repository.RepositoryRepository;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SparkSession;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public class ProcessGitHubOrganisation {

    public static String TOKEN = "your GitHub organisation token";

    public static String GITHUB = "https://api.github.com/";

    //Name of your organisation
    public static String ORGANISATION = "JavaBeginnerSchool";

    public static int PAGES = 100;

    public static void main(String[] args) {
        String masterUrl = "local[1]";
        if (args.length > 0) {
            masterUrl = args[0];
        } else if (args.length > 1) {
        }
        SparkConf conf = new SparkConf().setMaster(masterUrl).setAppName("GitHub organisation process");
        SparkSession spark = SparkSession
                .builder()
                .config(conf)
                .getOrCreate();
        SQLContext sqlContext = new SQLContext(spark);
        PersonRepository personRepository = new PersonRepository();
        List<Person> allUsers = personRepository.getAllPersons(ORGANISATION);
        RepositoryRepository repositoryRepository = new RepositoryRepository();
        List<Repository> allRepositories = repositoryRepository.getAllRepositories(ORGANISATION);
        Dataset<Row> peopleDf = spark.createDataFrame(allUsers, Person.class);
        peopleDf.printSchema();
        peopleDf.createOrReplaceTempView("people");
        Dataset<Row> repositoriesDf = sqlContext.createDataFrame(allRepositories, Repository.class);
        repositoriesDf.printSchema();
        repositoriesDf.createOrReplaceTempView("repositories");
        Dataset<Row> repos = sqlContext
                .sql("SELECT name, full_name, description, explode(collaborators) as collaborator FROM repositories");
        repos.createOrReplaceTempView("repositories");


        Dataset<Row> result = sqlContext.sql("SELECT * FROM people");
        result.show(60);


        repos = sqlContext
                .sql("SELECT repositories.name, repositories.full_name, repositories.description, people.login, people.name FROM repositories " +
                        "INNER JOIN people ON repositories.collaborator=people.id");
        repos.show(100);
        /*
        SELECT Orders.OrderID, Customers.CustomerName, Orders.OrderDate FROM Orders
        INNER JOIN Customers ON Orders.CustomerID=Customers.CustomerID;
         */

        result.coalesce(1).write().option("header", "true").csv(System.getProperty("user.dir") + "/data/" + System.currentTimeMillis());

    }

}
