
<h1>Executing The Digioh Test Automation On Local Machine</h1>

1. Make sure following softwares are installed on your machine :-
    * Chrome browser should be installed.
    * Java version >= 1.8 :- You can download and install java from :- https://www.oracle.com/in/java/technologies/javase-downloads.html
    To know whether java is installed on your machine or not, type following command in your terminal/command prompt:-
    `java -version`
    If it displays java version then it means its installed. If not then you can install it from the above url.
    * Maven is a famous dependancy management tool for Java. For Digioh Test Automation framework as well Maven is used.
    Hence, maven is required to be installed on your local machine.
    Depending upon your operating system, you can install maven from here:- https://maven.apache.org/install.html
    After following all the steps mentioned there, make sure on firing the command `mvn -v` maven version is shown
    * We are using Allure Report for visualizing the automation test results and reporting. So, allure also needs to be installed.
    Please refer this link for installing Allure:- https://docs.qameta.io/allure/

2. Clone the **digiohAutomation** repository on your machine.

3. From the terminal/command prompt, navigate to the directory:- *Digioh Auto* inside this repo

4. Fire following commands:-
    * `mvn clean`
    * `mvn clean test` :- This will start the test execution. You should see the chrome browser been launched and tests getting executed
    After the tests get executed you will see test results in the command prompt:-
    ```
    PASSED: basicBoxTest("/?HTTPS_PROTOCOL")
        Verifying basic digioh box appears successfully under different conditions controlled by the url path
    PASSED: emailFormSubmissionBlankEmail
        Verifying proper validation message is displayed when blank email is entered in the Email box
    PASSED: basicBoxTest("/?HOMEPAGE_PATH")
        Verifying basic digioh box appears successfully under different conditions controlled by the url path
    PASSED: basicBoxTest("/?CURRENT_PAGE_URL_CONTAINS")
        Verifying basic digioh box appears successfully under different conditions controlled by the url path
    PASSED: emailFormSubmissionSuccessful
    ```
    * After the execution completes, fire the below command to view the Allure report for better visualization of the test results:-
    `allure serve allure-results`
    This will open the allure report in the chrome browser. You can navigate to the different menu options present in the report tab and explore it. 
