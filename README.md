
wheelchimptests
===============
# # Initial set up
* Install and configure JDK 1.8+  
* Install and configure Apache [Maven 3.5.2+](http://maven.apache.org/)  
* Download and start latest [Selenium standalone server](http://www.seleniumhq.org/download/)  
* Download [TestNG plugin](http://testng.org/doc/download.html)

------------  

* Clone Repo 
https://github.com/suryawanshikapil/wheelchimptests

* Go to project location and run following command  
```
mvn clean install
```

* Import this project into IDE 

# # Project Structure 
**src/test/java**- contains test classes organized using TestNG annotations  
**src/test/resources** - contains TestNG xml files, API templates and XLS data-providers  
**src/main/java** - contains page object classes, API domains and additional utilities  
**src/main/resources** - contains l18n bundles, configuration properties files and MyBastis profiles if needed

# #Running Test
* Before running tests make sure you downloaded selenium standalone server jar file and started it by the following command:  

```
java -jar selenium-server-standalone-3.6.0.jar  

```

* Run test using command line  
 ```
 mvn clean test -Dsuite=wheelchimp-regression-tests  
 
 ```


