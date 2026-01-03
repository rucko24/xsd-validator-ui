# Xsd-validator-UI

<img width="1852" height="991" alt="image" src="https://github.com/user-attachments/assets/bac00bb1-ac22-4f04-a863-ebca159f884a" />

## Starting in Development Mode

To start the application in development mode, import it into your IDE and run the `Application` class. 
You can also start the application from the command line by running: 

```bash
./mvnw
```

## Building for Production

To build the application in production mode, run:

```bash
./mvnw package
```

To build a Docker image, run:

```bash
docker build -t my-application:latest .
```

If you use commercial components, pass the license key as a build secret:

```bash
docker build --secret id=proKey,src=$HOME/.vaadin/proKey .
```

## Next Steps

The [Building Apps](https://vaadin.com/docs/v25/building-apps) guides contain hands-on advice for adding features to 
your application.
