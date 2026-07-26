# Xsd-validator-UI

<img width="1842" height="1031" alt="image" src="https://github.com/user-attachments/assets/84de9bbf-92e6-4591-a10b-d748b58ce8bd" />

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
