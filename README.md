# Notes-Backend

This is the backend of my personal cloud storage project *Notes*, **a truly exquisite project**. 

The project is already running on a server. 

Backend architecture: Spring Boot + MySQL + Alibaba Cloud OSS, responsible for backend logic processing, file structure storage, and file storage respectively.

This website also supports online preview of PDF, Markdown, and Office files, which is of course implemented on the [Frontend](https://github.com/simon-hale/NotesFrontend "click to jump").

## Configuration

Create the local configuration file:

`cp application.properties.example application.properties`

Enter your non-sensitive configuration values in the configuration file. Then, set the required environment variables in your system:

```
export ALIYUN_ACCESS_KEY_ID=<ALIYUN_ACCESS_KEY_ID>
export ALIYUN_ACCESS_KEY_SECRET=<ALIYUN_ACCESS_KEY_SECRET>
export JWT_KEY=<JWT_KEY>
export DB_USERNAME=<DB_USERNAME>
export DB_PASSWORD=<DB_PASSWORD>
export SERVER_PORT=<SERVER_PORT>
```

Do not commit application.properties or any sensitive credentials to the repository.

## Run

Build the project with Maven:

`mvn clean package`

Run the generated JAR file:

`java -jar target/<jar-file>.jar`