mvn package

aws lambda update-function-code --function-name giveIncrement --zip-file fileb:///vagrant/HMI_Lambda/target/lambda-java-example-1.0-SNAPSHOT.jar
